package eu.earthobservatory.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.chainsaw.Main;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;


import eu.earthobservatory.runtime.postgis.Strabon;

public class ExperimentsReal {
	
	private static Strabon strabon = null;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 6) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.ExperimentReal <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <LOG_PATH>");
			System.err.println("       where <HOST>         is the postgis database host to connect to");
			System.err.println("             <PORT>         is the port to connect to on the database host");
			System.err.println("             <DATABASE>     is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>     is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>     is the password to use when connecting to the database");
			System.err.println("             <LOG_PATH>     where to store the logs");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];
		String logPath = args[5];

		String[] queries = {
				//Q1: members of Congress after Clinton.
				"select distinct ?name2 where  { ?x1  <http://xmlns.com/foaf/0.1/name> \"William Clinton\" .  ?x1 <http://www.rdfabout.com/rdf/schema/politico/hasRole> ?term1 ?t1 .   ?x2  <http://xmlns.com/foaf/0.1/name> ?name2 .  ?x2 <http://www.rdfabout.com/rdf/schema/politico/hasRole> ?term2 ?t2 .   FILTER(<http://strdf.di.uoa.gr/ontology#afterPeriod>(?t2, ?t1)) } ",
				//Q2: members of Congress, that there isn't another member of Congress after them.
				"select distinct ?name where { ?x1 <http://www.rdfabout.com/rdf/schema/politico/hasRole> ?term1 ?t1 .  ?x1  <http://xmlns.com/foaf/0.1/name> ?name . optional {?x2 <http://www.rdfabout.com/rdf/schema/politico/hasRole> ?term2 ?t2 . FILTER(<http://strdf.di.uoa.gr/ontology#afterPeriod>(?t2, ?t1)) .  } FILTER(!bound(?x2)) } ",
				//Q3: members of Congress in [1-1-1863, 1-1-1864].
				"select distinct ?name where { ?x1 <http://www.rdfabout.com/rdf/schema/politico/hasRole> ?term1 ?t1 .  ?x1  <http://xmlns.com/foaf/0.1/name> ?name .  FILTER(<http://strdf.di.uoa.gr/ontology#PeriodContains>(?t1, \"[1-1-1863 00:00:00, 1-1-1864 00:00:00]\"^^<http://strdf.di.uoa.gr/ontology#validTime>)) } " ,
		};
		
		int repetitions = 5;

		try {
			for (int qno = 0; qno < queries.length; qno++) {

				String queryString = queries[qno];

				long coldtime = 0;
				long warmtime = 0;
				long results = 0;

				// cold runs
				long[][] coldruns = new long[repetitions][4];
				for (int i = 0; i < repetitions; i++) {
					strabon = new Strabon(db, user, passwd, port, host, true);
					coldruns[i] = (long[])run("cold", queryString, i);
					strabon.close();
					strabon = null;
					
					URL script = Main.class.getResource("/pg_restart_clear_caches.sh");
					String restart_script = script.toString().substring(5);
					
					Process p = Runtime.getRuntime().exec(restart_script);
					p.waitFor();
					System.gc();
					Thread.sleep(5000);
				}

				//Strabon strabon = new Strabon(db, user, passwd, port, host, true, cachepath);
				strabon = new Strabon(db, user, passwd, port, host, true);

				// warm runs
				long[][] warmruns = new long[repetitions][4];
				// warm up caches
				long[] onerun = (long[])run("warm", queryString, 0);
				results = onerun[3];

				for (int i = 0; i < repetitions; i++) {
					warmruns[i] = (long[])run("warm", queryString, i);
				}

				strabon.close();
				strabon = null;

				// sort results
				SortedSet<Long> coldtimes = new TreeSet<Long>();
				for (int i = 0; i < repetitions; i++) {
					coldtimes.add(coldruns[i][2]);
				}

				SortedSet<Long> warmtimes = new TreeSet<Long>();
				for (int i = 0; i < repetitions; i++) {
					warmtimes.add(warmruns[i][2]);
				}

				// calculate median
				int i = 0;
				for (Long long1 : coldtimes) {
					i++;
					if ((repetitions % 2 == 1) && ((repetitions/2+1) == i)) {
						coldtime = long1;
					} else if ((repetitions % 2 == 0) && ((repetitions/2) == i)) { 
						coldtime = long1;
					} else if ((repetitions % 2 == 0) && ((repetitions/2+1) == i)) {	
						coldtime = coldtime + long1;
						coldtime = coldtime / 2;
					}
				}

				i = 0;
				for (Long long1 : warmtimes) {
					i++;
					if ((repetitions % 2 == 1) && ((repetitions/2+1) == i)) {
						warmtime = long1;
					} else if ((repetitions % 2 == 0) && ((repetitions/2) == i)) {
						warmtime = long1;
					} else if ((repetitions % 2 == 0) && ((repetitions/2+1) == i)) {
						warmtime = warmtime + long1;
						warmtime = warmtime / 2;
					}
				}

				// print logs
				FileWriter fstream;
				BufferedWriter out;
				try {

					fstream = new FileWriter(logPath + "/warm_key_q" + (qno+1),
							true);
					out = new BufferedWriter(fstream);
					out.write("GARBAGE " + results + " " + warmtime + "\n");
					out.close();

					fstream = new FileWriter(logPath + "/warm_key_q" + (qno+1)
							+ "_long", true);
					out = new BufferedWriter(fstream);
					for (int j = 0; j < repetitions; j++) {
						out.write("GARBAGE " + results + " " + warmruns[j][0]
								+ " " + warmruns[j][1] + " " + warmruns[j][2]
										+ "\n");
					}
					out.close();

					fstream = new FileWriter(logPath + "/cold_key_q" + (qno+1),
							true);
					out = new BufferedWriter(fstream);
					out.write("GARBAGE " + results + " " + coldtime + "\n");
					out.close();

					fstream = new FileWriter(logPath + "/cold_key_q" + (qno+1)
							+ "_long", true);
					out = new BufferedWriter(fstream);
					for (int j = 0; j < repetitions; j++) {
						out.write("GARBAGE " + results + " " + coldruns[j][0]
								+ " " + coldruns[j][1] + " " + coldruns[j][2]
										+ "\n");
					}
					out.close();
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				}
			}

		} catch (MalformedQueryException e) {
			System.out.println("Please use a well-formed query");
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (TupleQueryResultHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static long[] run(String cacheState, String query, int repetition) throws Exception {

		System.out.println("Executing query. Caches: " + cacheState + ". Repetition: " + repetition);

		long[] resp = (long[])strabon.query(query, org.openrdf.query.resultio.Format.EXP, null);
		
//		Statement st;
//		System.out.println("Evaluating query (naive).");
//		long t1 = System.nanoTime();
//		TupleQuery tupleQuery = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
//		TupleQueryResult result = tupleQuery.evaluate();
//		long t2 = System.nanoTime();
//		int results = 0;
//		while(result.hasNext()) {
//			String rrrr = result.next().toString();
//			results++;
//		}
//		long t3 = System.nanoTime();
//		System.out.println("Evaluated query (naive). results="+results);
//		resp = new long[]{t2-t1, t3-t2, t3-t1,results};
		
		return resp;

	}
}
