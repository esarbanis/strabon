package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.generaldb.Strabon;
import eu.earthobservatory.runtime.postgis.SimpleTests;

public class AggregateTests {
	public static Strabon strabon;
	
	
	protected static String jdbcDriver= "org.postgresql.Driver";  
	protected static String serverName = "localhost";
	protected static String username = "postgres";
	protected static String password = "postgres";
	protected static Integer port = 5432;
	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 

//	@BeforeClass
//	public abstract static void initialize() throws SQLException, ClassNotFoundException
//	{
//		strabon = new Strabon("cco2","postgres","p1r3as", 5432, "localhost", true);
//	}
	
	protected static void loadTestData()
			throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
		{
			URL src = SimpleTests.class.getResource("/simple-tests.ntriples");
			strabon.storeInRepo(src, "NTRIPLES");
		}

	@AfterClass
	public static void afterclass() throws SQLException
	{
strabon.close();
		
		conn.close();
		String url = "jdbc:postgresql://"+serverName+":"+port+"/template1";
		conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("DROP DATABASE \""+databaseName+"\"");
		conn.close();

	}

	String prefixes = 
		"PREFIX lgdo:<http://linkedgeodata.org/ontology/> "+
		"PREFIX geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> "+
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "+
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> ";

	String queryGroupBy1 = 
		prefixes+
		"SELECT (?placegeo AS ?xxx)  (AVG(?placegeo) AS ?av)  (strdf:union(?placegeo) AS ?united) "+
		"WHERE {  "+
		"?place  "+
		"rdfs:label ?placename ; "+ 	
		"geo:geometry ?placegeo ; "+
		"a ?type.  "+
		"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+ 
		"} "+
		"GROUP BY ?placegeo (STR(?place) AS ?str) strdf:union(?placegeo,?placegeo) "+
		"HAVING (AVG(?str) < 1) "+
		"ORDER BY ?placegeo strdf:union(?placegeo,?placegeo) "+
		"";

	String queryGroupBy2 = 
		prefixes+
		"SELECT  (strdf:union(strdf:intersection(?placegeo,?placegeo2)) AS ?united) "+
		"WHERE {  "+
		"?place  "+
		"rdfs:label ?placename ; "+ 	
		"geo:geometry ?placegeo ; "+
		"geo:geometry ?placegeo2 ; "+
		"a ?type.  "+
		"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+ 
		"} "+
		"GROUP BY ?placegeo ?placegeo2";

	String queryGroupBy3 = 
		prefixes+
		"SELECT  (strdf:union(strdf:intersection(?placegeo,?placegeo2)) AS ?united) "+
		"WHERE {  "+
		"?place  "+
		"rdfs:label ?placename ; 	 "+
		"geo:geometry ?placegeo ; "+
		"geo:geometry ?placegeo2 ; "+
		"a ?type.  "+
		"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+ 
		"} "+
		"GROUP BY ?placegeo ?placegeo2";
		

	String queryOrder1 =
		prefixes+
		"SELECT ?placegeo ?place (strdf:union(?placegeo,?placegeo) AS ?koko) (strdf:union(?placegeo,?placegeo) AS ?kiki) "+ 
		"WHERE { "+ 
		"?place "+ 
		"rdfs:label ?placename ; "+ 	
		"geo:geometry ?placegeo ; "+
		"a ?type. "+ 
		"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+ 
		"} "+
		"ORDER BY strdf:union(?placegeo,?placegeo) ?placegeo ?place";
		

	String queryOrder2 =
		prefixes+
		"SELECT (?place AS ?xx) (strdf:union(?placegeo,?placegeo) AS ?koko) ?place "+
		"WHERE {  "+
		"?place  "+
		"rdfs:label ?placename ; "+ 	
		"geo:geometry ?placegeo ; "+
		"a ?type.  "+
		"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+ 
		"} "+
		"ORDER BY ?placegeo ";
		

	
	@Test
	public void testQueryGroupBy1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryGroupBy1,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryGroupBy2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryGroupBy2,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryGroupBy3() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryGroupBy3,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryOrder1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryOrder1,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryOrder2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryOrder2,strabon.getSailRepoConnection());

	}
}
