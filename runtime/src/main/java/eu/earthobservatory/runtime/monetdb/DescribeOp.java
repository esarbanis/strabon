package eu.earthobservatory.runtime.monetdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DescribeOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.DescribeOp.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 7) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <QUERY> <OUTPUT>");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <QUERY>      is the stSPARQL query to evaluate.");
			System.err.println("             <OUTPUT>      is the stSPARQL query to evaluate.");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String queryString = args[5];
		String outFile = args[6];

		Strabon strabon = null;
		try { 
			strabon = new Strabon(db, user, passwd, port, host, true);
			strabon.describe(queryString, strabon.getSailRepoConnection(), outFile);
			
		} catch (Exception e) {
			logger.error("[Strabon.DescribeOp] Error during execution of DESCRIBE query.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}
}
