package eu.earthobservatory.runtime.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.postgis.StoreOp.class); 
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			help();
			System.exit(1);
		}

		String db = args[0];	
		String libspatial = args[1];
		String regex = args[2];
		String src = args[3];
		String format = "NTRIPLES";
		String graph = null;
		
		for (int i = 4; i < args.length; i += 2) {
			if (args[i].equals("-f")) {
				if (i + 1 >= args.length) {
					System.err.println("Option \"-f\" requires an argument.");
					help();
					System.exit(1);
					
				} else {
					format = args[i+1];
				}
			} else if (args[i].equals("-g")) {
				if (i + 1 >= args.length) {
					System.err.println("Option \"-g\" requires an argument.");
					help();
					System.exit(1);
					
				} else {
					graph = args[i+1];
				}
				
			} else {
				System.err.println("Unknown argument \"" + args[i] + "\".");
				help();
				System.exit(1);
			}
		}

		Strabon strabon = null;
		try {
			strabon = new Strabon(db, libspatial, regex, false);
			if (graph == null) {
				strabon.storeInRepo(src, format);
				
			} else {
				strabon.storeInRepo(src, null, graph, format);
			}
			
		} catch (Exception e) {
			logger.error("[Strabon.StoreOp] Error during store.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}

	private static void help() {
		System.err.println("Usage: eu.earthobservatory.runtime.postgis.StoreOp <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <FILE> [-f <FORMAT>] [-g <NAMED_GRAPH>]");	
		System.err.println("             <DATABASE>   		 is the spatially enabled postgis database that Strabon will use as a backend, ");
		System.err.println("             [-f <FORMAT>] 		 is the format of the file (default: NTRIPLES)");
		System.err.println("             [-g <NAMED_GRAPH>]  is the URI of the named graph to store the input file (default: default graph)");
	}
}
