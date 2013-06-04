package eu.earthobservatory.runtime.sqlite;

import eu.earthobservatory.utils.Format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.sqlite.QueryOp.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <QUERY> ");
			System.err.println("             <DATABASE>   is the spatially enabled sqlite database that Strabon will use as a backend, ");
			System.err.println("             <QUERY>      is the stSPARQL query to evaluate.");
			System.err.println("             <DELET_LOCK> is true when deletion of \"locked\" table should be enforced (e.g., when Strabon has been ungracefully shutdown).");
			System.err.println("             [<FORMAT>]   is the format of your results (default: XML)");
			System.exit(0);
		}

	//	String host = args[0];
	//	Integer port = new Integer(args[1]);
		String db = args[0];
		String libspatial = args[1];
		String regex = args[2];		
		String queryString = args[3];
		boolean forceDelete = Boolean.valueOf(args[4]);
		String resultsFormat = "";
		if ( args.length == 6 ) {
			resultsFormat = args[5];
		}

		Strabon strabon = null;
		try {
			strabon = new Strabon(db, libspatial, regex, forceDelete);
			strabon.query(queryString, Format.fromString(resultsFormat), strabon.getSailRepoConnection(), System.out);
			
		} catch (Exception e) {
			logger.error("[Strabon.QueryOp] Error during execution of SPARQL query.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}
}
