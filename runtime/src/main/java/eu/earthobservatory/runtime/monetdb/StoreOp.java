package eu.earthobservatory.runtime.monetdb;

import java.io.File;


public class StoreOp {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 6) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <QUERY> ");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <FILE>       is the file to be stored");
			System.err.println("             [<FORMAT>]   is the format of the file (NTRIPLES)");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String src = args[5];
		String format = "NTRIPLES";
		if ( args.length == 7 ) {
			format = args[6];
		}

		Strabon strabon = new Strabon(db, user, passwd, port, host, true);
		
		File file = new File (src);
		strabon.storeInRepo(file, format);
//		strabon.storeInRepo(file, null, null, fileRDFFormat);

		strabon.close();
	}

}
