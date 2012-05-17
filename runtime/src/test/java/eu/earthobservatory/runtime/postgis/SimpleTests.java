package eu.earthobservatory.runtime.postgis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author George Garbis
 */

public class SimpleTests {

	private static Strabon strabon;

	private static String jdbcDriver= "org.postgresql.Driver";  
	private static String serverName = "localhost";
	private static String username = "postgres";
	private static String password = "postgres";
	private static Integer port = 5432;
	private static java.sql.Connection conn = null;
	private static String databaseName = null; 

	protected static final String 
		STRDF_NS = "http://strdf.di.uoa.gr/ontology#",
		EX_NS = "http://example.org/",
		NOA_NS = "http://teleios.di.uoa.gr/ontologies/noaOntology.owl#";


	protected static final String prefixes = 
		"PREFIX rdf: <"+RDF.NAMESPACE+"> \n" +
		"PREFIX strdf: <"+STRDF_NS+"> \n" +
		"PREFIX xsd: <"+XMLSchema.NAMESPACE+"> \n" +
		"PREFIX ex: <"+EX_NS+"> \n" +
		"PREFIX noa: <"+NOA_NS+"> \n";
	
	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		// Create database
		Date date = new Date(0);
		databaseName = "strabon-test-"+date.getTime();
		Class.forName(jdbcDriver);
		String url = "jdbc:postgresql://"+serverName+":"+port+"/template1";
		conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement();
	    String sql = "CREATE DATABASE \""+databaseName+"\" WITH TEMPLATE = template_postgis";
	    stmt.executeUpdate(sql);
	    stmt.close();
		conn.close();
		
	    // Connect to database
		url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		System.out.println("open database");
		conn = DriverManager.getConnection(url, username, password);
	    
		strabon = new Strabon(databaseName,"postgres","postgres", 5432, "localhost", true);
		
		loadTestData();
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		strabon.close();
		
		conn.close();
		String url = "jdbc:postgresql://"+serverName+":"+port+"/template1";
		conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("DROP DATABASE \""+databaseName+"\"");
		conn.close();
	}
	
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@Before
//	public void before()
//		throws Exception
//	{
//		
//	}
//
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@After
//	public void after()
//		throws Exception
//	{
//		// Clean database
//		Statement stmt = conn.createStatement();
//		ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE " +
//						"table_schema='public' and table_name <> 'spatial_ref_sys' " +
//						"and table_name <> 'geometry_columns' and " +
//						"table_name <> 'geography_columns' and table_name <> 'locked'");
//		while (results.next()) {
//			String table_name = results.getString("table_name");
//			Statement stmt2 = conn.createStatement();
//			stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
//			stmt2.close();
//		}
//			
//		stmt.close();
//	}

	@Test
	public void testSimpleFilter() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT ?id "+ 
			"WHERE { "+
				"?s ex:id ?id . "+
				"?s ex:value ?value . "+
				"FILTER( ?id<2 ) . " +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id=\"1\"^^<http://www.w3.org/2001/XMLSchema#int>]"));
	}
	
	@Test
	public void testMatchLiteralWithWKTType() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
		String query = 
			prefixes+
			"SELECT ?s2 "+
			"WHERE { "+
			" ?s2 noa:hasGeometry \"POINT(1 0)\"^^strdf:WKT . "+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[s2=http://example.org/item1]"));
	}
	
//	@Test
//	public void testGroupConcatDistinct() {
//		StringBuilder query = new StringBuilder();
//		query.append(getNamespaceDeclarations());
//		query.append("SELECT (GROUP_CONCAT(DISTINCT ?l) AS ?concat)");
//		query.append("WHERE { ex:groupconcat-test ?p ?l . }");
//
//		TupleQuery tq = null;
//		try {
//			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
//		}
//		catch (RepositoryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		catch (MalformedQueryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		try {
//			TupleQueryResult result = tq.evaluate();
//			assertNotNull(result);
//
//			while (result.hasNext()) {
//				BindingSet bs = result.next();
//				assertNotNull(bs);
//
//				Value concat = bs.getValue("concat");
//
//				assertTrue(concat instanceof Literal);
//
//				String lexValue = ((Literal)concat).getLabel();
//
//				int occ = countCharOccurrences(lexValue, 'a');
//				assertEquals(1, occ);
//				occ = countCharOccurrences(lexValue, 'b');
//				assertEquals(1, occ);
//				occ = countCharOccurrences(lexValue, 'c');
//				assertEquals(1, occ);
//				occ = countCharOccurrences(lexValue, 'd');
//				assertEquals(1, occ);
//			}
//			result.close();
//		}
//		catch (QueryEvaluationException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//	}
//	
//	@Test 
//	public void testPropertyPathInTree() {
//		StringBuilder query = new StringBuilder();
//		query.append(getNamespaceDeclarations());
//		query.append(" SELECT ?node ?name ");
//		query.append(" FROM ex:tree-graph ");
//		query.append(" WHERE { ?node ex:hasParent+ ex:b . ?node ex:name ?name . }");
//
//		TupleQuery tq = null;
//		try {
//			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
//		}
//		catch (RepositoryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		catch (MalformedQueryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		try {
//			TupleQueryResult result = tq.evaluate();
//			assertNotNull(result);
//
//			while (result.hasNext()) {
//				BindingSet bs = result.next();
//				assertNotNull(bs);
//				
//				System.out.println(bs);
//				
//			}
//			result.close();
//		}
//		catch (QueryEvaluationException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//				
//	}
//
//	@Test
//	public void testGroupConcatNonDistinct() {
//		StringBuilder query = new StringBuilder();
//		query.append(getNamespaceDeclarations());
//		query.append("SELECT (GROUP_CONCAT(?l) AS ?concat)");
//		query.append("WHERE { ex:groupconcat-test ?p ?l . }");
//
//		TupleQuery tq = null;
//		try {
//			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
//		}
//		catch (RepositoryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		catch (MalformedQueryException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		try {
//			TupleQueryResult result = tq.evaluate();
//			assertNotNull(result);
//
//			while (result.hasNext()) {
//				BindingSet bs = result.next();
//				assertNotNull(bs);
//
//				Value concat = bs.getValue("concat");
//
//				assertTrue(concat instanceof Literal);
//
//				String lexValue = ((Literal)concat).getLabel();
//
//				int occ = countCharOccurrences(lexValue, 'a');
//				assertEquals(1, occ);
//				occ = countCharOccurrences(lexValue, 'b');
//				assertEquals(2, occ);
//				occ = countCharOccurrences(lexValue, 'c');
//				assertEquals(2, occ);
//				occ = countCharOccurrences(lexValue, 'd');
//				assertEquals(1, occ);
//			}
//			result.close();
//		}
//		catch (QueryEvaluationException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//	}
//
//	private int countCharOccurrences(String string, char ch) {
//		int count = 0;
//		for (int i = 0; i < string.length(); i++) {
//			if (string.charAt(i) == ch) {
//				count++;
//			}
//		}
//		return count;
//	}
//
//	/**
//	 * Get a set of useful namespace prefix declarations.
//	 * 
//	 * @return namespace prefix declarations for rdf, rdfs, dc, foaf and ex.
//	 */
//	protected String getNamespaceDeclarations() {
//		StringBuilder declarations = new StringBuilder();
//		declarations.append("PREFIX rdf: <" + RDF.NAMESPACE + "> \n");
//		declarations.append("PREFIX rdfs: <" + RDFS.NAMESPACE + "> \n");
//		declarations.append("PREFIX dc: <" + DC.NAMESPACE + "> \n");
//		declarations.append("PREFIX foaf: <" + FOAF.NAMESPACE + "> \n");
//		declarations.append("PREFIX ex: <" + EX_NS + "> \n");
//		declarations.append("\n");
//
//		return declarations.toString();
//	}
//
//	protected abstract Repository newRepository()
//		throws Exception;

	protected static void loadTestData()
		throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		URL src = SimpleTests.class.getResource("/simple-tests.ntriples");
		strabon.storeInRepo(src, "NTRIPLES");
	}
}
