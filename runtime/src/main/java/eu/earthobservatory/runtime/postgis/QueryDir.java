package eu.earthobservatory.runtime.postgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;


public class QueryDir {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 7) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <PATH> [<FORMAT>]");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <PATH>       is the path containing all stSPARQL queries to evaluate.");
			System.err.println("             <EXTENSION>  is the extention of the files that contain the stSPARQL queries. (e.g., '.rq')");
			System.err.println("             [<FORMAT>]   is the format of your results (XML)");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];
		String path = args[5];
		final String extension = args[6];
		String resultsFormat = "";
		if ( args.length == 8 ) {
			resultsFormat = args[7];
		}

		Strabon strabon = new Strabon(db, user, passwd, port, host, true);

		File dir = new File(path);

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(extension);
			}
		};

		String[] children = dir.list(filter);
		if (children != null) {
			for (int i=0; i<children.length; i++) {
				String filename = children[i];
				try {
					System.out.println("Evaluating query from '" + path + System.getProperty("file.separator") + filename  +"'.");
					String queryString = readFile(path + System.getProperty("file.separator") + filename);
					System.out.println("Evaluating stSPARQL query: \n"+queryString+"\n");
					strabon.query(queryString, resultsFormat, strabon.getSailRepoConnection());
				} catch (IOException e) {
					System.err.println("IOException while reading " + filename);
				}
			}
		}
		
		strabon.close();
	}

	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String line  = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		return stringBuilder.toString();
	}

	/*
	private static void query(String queryString, SailRepositoryConnection con) throws MalformedQueryException, RepositoryException, QueryEvaluationException, IOException, ClassNotFoundException, TupleQueryResultHandlerException {
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

		System.out.println(queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		System.out.println("-------------------------------------------");
		System.out.println("-                RESULTS                  -");
		System.out.println("-------------------------------------------");

		tupleQuery.evaluate(new SPARQLResultsXMLWriter(System.out));

		List<String> bindingNames = result.getBindingNames();
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();			
			System.out.println(bindingSet.toString());
		}
		System.out.println("-------------------------------------------");
		System.out.flush();
	}
	 */
}
