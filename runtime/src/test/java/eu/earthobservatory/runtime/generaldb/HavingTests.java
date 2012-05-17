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

public class HavingTests {
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
	


	String prefixes =
			" PREFIX noa:<http://www.earthobservatory.eu/ontologies/noaOntology.owl#>"+
					" PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					" PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> ";




	String query1Having = prefixes + 
			"SELECT ?burntArea ?baGeo "+ 
			"WHERE { "+ 
			"?burntArea a noa:BurntArea; "+ 
			"strdf:hasGeometry ?baGeo. "+ 
			"?urbanArea a noa:UrbanArea; "+ 
			"strdf:hasGeometry ?uaGeo; "+ 
			"FILTER(strdf:anyInteract(?baGeo,?uaGeo)). "+
			"} "+
			"GROUP BY ?burntArea ?baGeo "+
			"HAVING (strdf:area(strdf:union(?uaGeo)) > 8) ";

	String query2HavingA = prefixes + 
	"SELECT ?burntArea ?baGeo (strdf:extent(?uaGeo) AS ?totalMBB) "+ 
	"WHERE { "+ 
	"?burntArea a noa:BurntArea; "+ 
	"strdf:hasGeometry ?baGeo. "+ 
	"?urbanArea a noa:UrbanArea; "+ 
	"strdf:hasGeometry ?uaGeo; "+ 
	"FILTER(strdf:anyInteract(?baGeo,?uaGeo)). "+
	"} "+
	"GROUP BY ?burntArea ?baGeo "+
	"HAVING (strdf:area(strdf:extent(?uaGeo)) > 8) ";
	
	String query2HavingB = prefixes + 
	"SELECT ?burntArea ?baGeo (strdf:difference(strdf:extent(?uaGeo),strdf:union(?uaGeo)) AS ?MBBdiff) "+ 
	"WHERE { "+ 
	"?burntArea a noa:BurntArea; "+ 
	"strdf:hasGeometry ?baGeo. "+ 
	"?urbanArea a noa:UrbanArea; "+ 
	"strdf:hasGeometry ?uaGeo; "+ 
	"FILTER(strdf:anyInteract(?baGeo,?uaGeo)). "+
	"} "+
	"GROUP BY ?burntArea ?baGeo "+
	"HAVING (strdf:area(strdf:extent(?uaGeo)) > 8) ";
	
	String query2HavingC = prefixes + 
	"SELECT ?burntArea (strdf:extent(?uaGeo) AS ?totalMBB) "+ 
	"WHERE { "+ 
	"?burntArea a noa:BurntArea; "+ 
	"strdf:hasGeometry ?baGeo. "+ 
	"?urbanArea a noa:UrbanArea; "+ 
	"strdf:hasGeometry ?uaGeo; "+ 
	"FILTER(strdf:anyInteract(?baGeo,?uaGeo)). "+
	"} "+
	"GROUP BY ?burntArea "+
	"HAVING (strdf:area(strdf:extent(?uaGeo)) < 8) ";
	
	@Test
	public void testQuery1Having() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query1Having,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery2HavingA() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2HavingA,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery2HavingB() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2HavingB,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery2HavingC() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2HavingC,strabon.getSailRepoConnection());

	}
	
	protected static void loadTestData()
			throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
		{
			URL src = SimpleTests.class.getResource("/simple-tests.ntriples");
			strabon.storeInRepo(src, "NTRIPLES");
		}

}
