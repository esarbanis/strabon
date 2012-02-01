package eu.earthobservatory.runtime.postgis;


public class QueryOp {

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
			System.err.println("             <QUERY>      is the stSPARQL query to evaluate.");
			System.err.println("             [<FORMAT>]     is the format of your results (XML)");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String queryString = args[5];
		String resultsFormat = "";
		if ( args.length == 7 ) {
			resultsFormat = args[6];
		}




		Strabon strabon = new Strabon(db, user, passwd, port, host, true);

		strabon.query(queryString, resultsFormat, strabon.getSailRepoConnection());

		strabon.close();
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
