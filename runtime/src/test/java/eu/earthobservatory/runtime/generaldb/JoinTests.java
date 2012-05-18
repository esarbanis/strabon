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

public class JoinTests {

	public static Strabon strabon;

	protected static String jdbcDriver= "org.postgresql.Driver";  
	protected static String serverName = "localhost";
	protected static String username = "postgres";
	protected static String password = "postgres";
	protected static Integer port = 5432;
	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 
	

//	@BeforeClass
//	public static void initialize() throws SQLException, ClassNotFoundException
//	{
//		strabon = new Strabon("join-tests","postgres","p1r3as", 5432, "localhost", true);
//
//	}



	String query5a = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{"+
		"?place a <http://linkedgeodata.org/ontology/NaturalWood> . "+
		"?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\". "+	
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem . "+	
		"?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"FILTER(strdf:anyInteract(?sensorsGeo,?areaGeo) && strdf:anyInteract(?sensorsGeo,?placeGeo)) . "+ 
		"}";

	String query5b = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{ "+
		"?place a <http://linkedgeodata.org/ontology/NaturalWood> . "+ 
		"?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem . "+	
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\". "+	
		"FILTER(strdf:anyInteract(?sensorsGeo,?areaGeo)) . "+
		"FILTER(strdf:anyInteract(?sensorsGeo,?placeGeo)). }";

	String query5c = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{ "+
		"?place a <http://linkedgeodata.org/ontology/NaturalWood> . "+ 
		"?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\". "+	
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem . "+	
		"?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"FILTER(strdf:anyInteract(?sensorsGeo,?areaGeo)) . "+
		"FILTER(strdf:anyInteract(?sensorsGeo,?placeGeo)). }";

	String query9a =
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> " +
		"SELECT ?populatedArea ?sensor ?portArea " +
		"WHERE " +
		"{ " +
		"?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . " +
		" ?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem . " +	
		" ?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . " +
		" ?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . " +
		" ?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . " +
		" ?portArea <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasLandUse>  <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#portAreas> . " +	
		" ?portArea a <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#Area> . " +
		" ?portArea <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry> ?seaGeo . " +
		" ?populatedArea <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/PopulatedPlace> . " +
		" ?populatedAreaGeoNames <http://www.w3.org/2002/07/owl#sameAs> ?populatedArea .			 " +
		" ?populatedAreaGeoNames <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo .  " +
		"  FILTER( strdf:anyInteract(?areaGeo,?sensorsGeo) ) . " +
		" FILTER( strdf:anyInteract(?seaGeo,?sensorsGeo) ) . " +
		"}";

	String query3way = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{ "+
		"?place a <http://linkedgeodata.org/ontology/NaturalWood> . "+ 
		"?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\". "+	
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem . "+	
		"?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"FILTER(strdf:anyInteract(?sensorsGeo,strdf:union(?areaGeo,?placeGeo)) " +
		"&& strdf:anyInteract(?sensorsGeo,?placeGeo) && ?place = ?spaceRegion) . "+
		"}";

	String query5_3filters = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{ "+
		"	?place a <http://linkedgeodata.org/ontology/NaturalWood> .  "+
		"	?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"	?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"	?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem .	 "+
		"	?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"	?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\".	 "+
		"	FILTER(strdf:anyInteract(?sensorsGeo,?areaGeo)) .  "+
		"FILTER(strdf:anyInteract(?sensorsGeo,?placeGeo)).  "+
		"      FILTER(strdf:anyInteract(?areaGeo,?placeGeo)).}";
	
	String query5_properties = 
		"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
		"SELECT ?sensor ?place WHERE "+
		"{ "+
		"	?place a <http://linkedgeodata.org/ontology/NaturalWood> .  "+
		"	?place <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?placeGeo. "+
		"	?sensorSystem <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> ?sensor . "+
		"	?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedSystem> ?sensorSystem .	 "+
		"	?sensorsDeployment <http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform> ?sensorPlatform . "+
		"?sensorPlatform <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?spaceRegion . "+
		"	?spaceRegion <http://dbpedia.org/property/hasGeometry> ?sensorsGeo . "+
		"?areaOfInterest <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography> ?areaGeo . "+
		"?areaOfInterest <http://www.geonames.org/ontology#name>  \"London\".	 "+
		"	FILTER(strdf:anyInteract(?sensorsGeo,?areaGeo)) .  "+
		"FILTER(strdf:dimension(?sensorsGeo) = strdf:dimension(?placeGeo)).  }";

	@Test
	public void testQuery5a() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5a,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery5b() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5b,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery5c() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5c,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery9a() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query9a,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery3way() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query3way,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery5_3filters() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5_3filters,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery5_properties() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5_properties,strabon.getSailRepoConnection());

	}
}
