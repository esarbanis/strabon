package eu.earthobservatory.runtime.generaldb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

public class SpatialTests {
	public static Strabon strabon;
	
	protected static String jdbcDriver= "org.postgresql.Driver";  
	protected static String serverName = "localhost";
	protected static String username = "postgres";
	protected static String password = "postgres";
	protected static Integer port = 5432;
	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 

	public String 	STRDF_NS = "http://strdf.di.uoa.gr/ontology#",
					EX_NS = "http://example.org/",
					NOA_NS = "http://teleios.di.uoa.gr/ontologies/noaOntology.owl#",
					GEOF_NS ="http://www.opengis.net/def/queryLanguage/OGC-GeoSPARQL/1.0/function/";
	
//	@BeforeClass
//	public static void initialize() throws SQLException, ClassNotFoundException
//	{
//		strabon = new Strabon("spatial-tests","postgres","postgres", 5432, "localhost", true);
//	}


	protected String prefixes = 
		"PREFIX rdf: <"+RDF.NAMESPACE+"> \n" +
		"PREFIX strdf: <"+STRDF_NS+"> \n" +
		"PREFIX xsd: <"+XMLSchema.NAMESPACE+"> \n" +
		"PREFIX ex: <"+EX_NS+"> \n" +
		"PREFIX noa: <"+NOA_NS+"> \n" +
		"PREFIX geof: <"+GEOF_NS+"> \n";
	
	// -- Spatial Relationships -- //
	
	@Test
	public void testStrdfAnyInteract() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) < str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:anyInteract(?g1, ?g2 ) ) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")	
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(4, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfContains() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:contains(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfCovers() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:covers(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfCoveredBy() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:coveredBy(?g1, ?g2 )) . \n"+
			"}";

		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}

	@Test
	public void testStrdfDisjoint() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( strdf:disjoint(?g1, ?g2 )) . \n"+
			"}";

		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(40, bindings.size());
		// too many results
		//assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}

	@Test
	public void testStrdfEquals() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( strdf:equals(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
		}

	@Test
	public void testStrdfInside() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:inside(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}

	@Test
	public void testStrdfOverlap() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:overlap(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}

	@Test
	public void testStrdfTouch() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) < str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-touches(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfRelate() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( strdf:relate(?g1, ?g2, \"T*F**F***\" )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));	
	}
	
//	TODO these tests run only in postgis
//	@Test
//	public void testStrdfLeft() throws MalformedQueryException, QueryEvaluationException	
//	@Test
//	public void testStrdfRight() throws MalformedQueryException, QueryEvaluationException
//	@Test
//	public void testStrdfAbove() throws MalformedQueryException, QueryEvaluationException	
//	@Test
//	public void testStrdfBelow() throws MalformedQueryException, QueryEvaluationException
	
	// -- Spatial Constructs -- //
	@Test
	public void testStrdfUnion() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:union(?g, \"POLYGON((5 3, 10 3, 10 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((5 3, 3 3, 3 8, 5 8, 10 8, 10 3, 5 3));http://www.opengis.net/def/crs/EPSG/0/4326\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}
	
	@Test
	public void testStrdfEnvelopeBuffer() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:envelope(strdf:buffer(?g, \"0.5\"^^xsd:float)) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
				
		if ( -1<bindings.indexOf("[ret=\"POLYGON ((7.5 0.5, 7.5 2.5, 9.5 2.5, 9.5 0.5, 7.5 0.5))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]") ||
			 -1<bindings.indexOf("[ret=\"POLYGON ((7.542893218813453 0.5428932188134517, 9.457106781186548 0.5428932188134517, 9.457106781186548 2.4571067811865483, 7.542893218813453 2.4571067811865483, 7.542893218813453 0.5428932188134517))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]")
		)
			assertTrue(true);
		else
			fail();
	}

	@Test
	public void testStrdfConvexHull() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:convexHull(\"GEOMETRYCOLLECTION( MULTILINESTRING((100 190,10 8),(150 10, 20 30)), MULTIPOINT(50 5, 150 30, 50 10, 10 10) )\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((50 5, 10 8, 10 10, 100 190, 150 30, 150 10, 50 5))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}
	
	@Test
	public void testStrdfBoundary() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:boundary(?g) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"LINESTRING (8 1, 9 1, 9 2, 8 2, 8 1)\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));	}

	@Test
	public void testStrdfIntersection() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:intersection(?g, \"POLYGON((5 3, 10 3, 10 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((10 3, 5 3, 5 8, 10 8, 10 3))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}
	
	@Test
	public void testStrdfDifference() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:difference(?g, \"POLYGON((5 3, 10 3, 10 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((5 3, 3 3, 3 8, 5 8, 5 3))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}

	@Test
	public void testStrdfSymDifference() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
	String query = 
		prefixes+
		"SELECT DISTINCT ( strdf:symDifference(?g, \"POLYGON((5 3, 12 3, 12 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
		"WHERE { \n" +
		" ?s ex:id ?id . \n"+
		" ?s ex:geometry ?g . \n" +
		" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
		"}";
	
	@SuppressWarnings("unchecked")
	ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
			
	assertEquals(1, bindings.size());
	assertTrue(-1<bindings.indexOf("[ret=\"MULTIPOLYGON (((5 3, 3 3, 3 8, 5 8, 5 3)), ((10 3, 10 8, 12 8, 12 3, 10 3)))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}

//	// -- Spatial Metric Functions -- //

	@Test
	public void testStrdfDistance() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:distance(?g, \"POINT( 10 1 )\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"1.0\"^^<http://www.w3.org/2001/XMLSchema#double>]"));
	}

	@Test
	public void testStrdfArea() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:area(?g) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"1.0\"^^<http://www.w3.org/2001/XMLSchema#double>]"));
	}

	@Test
	public void testStrdfDimension() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT (strdf:dimension(?g) AS ?ret2) " +
							"(strdf:dimension(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:dimension(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		// TODO monetdb returns as dimension only 3 (because of GEOS lib)
		if ( -1<bindings.indexOf("[ret2=\"2\"^^<http://www.w3.org/2001/XMLSchema#int>;ret1=\"1\"^^<http://www.w3.org/2001/XMLSchema#int>;ret0=\"0\"^^<http://www.w3.org/2001/XMLSchema#int>]") ||
				-1<bindings.indexOf("[ret2=\"3\"^^<http://www.w3.org/2001/XMLSchema#int>;ret1=\"3\"^^<http://www.w3.org/2001/XMLSchema#int>;ret0=\"3\"^^<http://www.w3.org/2001/XMLSchema#int>]") )
			assertTrue(true);
		else
			fail();
		
	}
	
	@Test
	public void testStrdfGeometryType() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT (strdf:geometryType(?g) AS ?ret2) " +
							"(strdf:geometryType(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:geometryType(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(1, bindings.size());
		// TODO different results between dbms in strdf:geometryType
		if ( -1<bindings.indexOf("[ret2=\"3\";ret1=\"1\";ret0=\"0\"]") ||
			 -1<bindings.indexOf("[ret2=\"ST_Polygon\";ret1=\"ST_LineString\";ret0=\"ST_Point\"]")	
			)
			assertTrue(true);
		else
			fail();
	}
	
	@Test
	public void testStrdfAsText() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT (strdf:asText(?g) AS ?ret2) " +
							"(strdf:asText(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:asText(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(1, bindings.size());
		if ( -1<bindings.indexOf("[ret2=\"POLYGON((8 1,9 1,9 2,8 2,8 1))\";ret1=\"LINESTRING(8 1,9 1,9 2,8 2,8 1)\";ret0=\"POINT(0 0)\"]") ||
				-1<bindings.indexOf("[ret2=\"POLYGON ((8.0000000000000000 1.0000000000000000, 9.0000000000000000 1.0000000000000000, 9.0000000000000000 2.0000000000000000, 8.0000000000000000 2.0000000000000000, 8.0000000000000000 1.0000000000000000))\";ret1=\"LINESTRING (8.0000000000000000 1.0000000000000000, 9.0000000000000000 1.0000000000000000, 9.0000000000000000 2.0000000000000000, 8.0000000000000000 2.0000000000000000, 8.0000000000000000 1.0000000000000000)\";ret0=\"POINT (0.0000000000000000 0.0000000000000000)\"]")
			)
			assertTrue(true);
		else
			fail();
		
	}
	
	@Test
	public void testStrdfSrid() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT (strdf:srid(?g) AS ?ret2) " +
							"(strdf:srid(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:srid(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		// TODO test srid uri arguments
		assertEquals(1, bindings.size());		
		if 	( -1<bindings.indexOf("[ret2=\"4326\"^^<http://www.w3.org/2001/XMLSchema#int>;ret1=\"4326\"^^<http://www.w3.org/2001/XMLSchema#int>;ret0=\"4326\"^^<http://www.w3.org/2001/XMLSchema#int>]") ||
				-1<bindings.indexOf("[ret2=\"0\"^^<http://www.w3.org/2001/XMLSchema#int>;ret1=\"0\"^^<http://www.w3.org/2001/XMLSchema#int>;ret0=\"0\"^^<http://www.w3.org/2001/XMLSchema#int>]")
			) 
			assertTrue(true);
		else
			fail();
	}
	
	@Test
	public void testStrdfIsEmpty() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query =
			prefixes+
			"SELECT DISTINCT (strdf:isEmpty(?g) AS ?ret2) " +
							"(strdf:isEmpty(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:isEmpty(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
	
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());		
		assertTrue(-1<bindings.indexOf("[ret2=\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>;ret1=\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>;ret0=\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>]"));
	}
	
	@Test
	public void testStrdfIsSimple() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query =
			prefixes+
			"SELECT DISTINCT (strdf:isSimple(?g) AS ?ret2) " +
							"(strdf:isSimple(strdf:boundary(?g)) AS ?ret1) " +
							"(strdf:isSimple(\"POINT(0 0)\"^^strdf:WKT) AS ?ret0) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
	
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
	
		assertEquals(1, bindings.size());		
		assertTrue(-1<bindings.indexOf("[ret2=\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>;ret1=\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>;ret0=\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>]"));
	}
	
	// -- GEOSPARQL -- //
	
	// -- Non-topological -- //
	
	@Test
	public void testGeofConvexHull() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( geof:convexHull(\"GEOMETRYCOLLECTION( MULTILINESTRING((100 190,10 8),(150 10, 20 30)), MULTIPOINT(50 5, 150 30, 50 10, 10 10) )\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((50 5, 10 8, 10 10, 100 190, 150 30, 150 10, 50 5))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}
	
	@Test
	public void testGeofIntersection() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( geof:intersection(?g, \"POLYGON((5 3, 10 3, 10 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((10 3, 5 3, 5 8, 10 8, 10 3))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}
	
	@Test
	public void testGeofDifference() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ 
		String query = 
			prefixes+
			"SELECT DISTINCT ( geof:difference(?g, \"POLYGON((5 3, 10 3, 10 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((5 3, 3 3, 3 8, 5 8, 5 3))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}

	@Test
	public void testGeofSymDifference() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
	String query = 
		prefixes+
		"SELECT DISTINCT ( geof:symmetricDifference(?g, \"POLYGON((5 3, 12 3, 12 8, 5 8, 5 3))\"^^strdf:WKT) AS ?ret ) \n"+
		"WHERE { \n" +
		" ?s ex:id ?id . \n"+
		" ?s ex:geometry ?g . \n" +
		" FILTER( str(?id) = \"Z\"^^xsd:string ) . \n" +
		"}";
	
	@SuppressWarnings("unchecked")
	ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
			
	assertEquals(1, bindings.size());
	assertTrue(-1<bindings.indexOf("[ret=\"MULTIPOLYGON (((5 3, 3 3, 3 8, 5 8, 5 3)), ((10 3, 10 8, 12 8, 12 3, 10 3)))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
	}

	@Test
	public void testGeofEnvelope() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ( strdf:envelope(strdf:buffer(?g, \"0.5\"^^xsd:float)) AS ?ret ) \n"+
			"WHERE { \n" +
			" ?s ex:id ?id . \n"+
			" ?s ex:geometry ?g . \n" +
			" FILTER( str(?id) = \"C\"^^xsd:string ) . \n" +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
				
		if ( -1<bindings.indexOf("[ret=\"POLYGON ((7.5 0.5, 7.5 2.5, 9.5 2.5, 9.5 0.5, 7.5 0.5))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]") ||
			 -1<bindings.indexOf("[ret=\"POLYGON ((7.542893218813453 0.5428932188134517, 9.457106781186548 0.5428932188134517, 9.457106781186548 2.4571067811865483, 7.542893218813453 2.4571067811865483, 7.542893218813453 0.5428932188134517))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]")
		)
			assertTrue(true);
		else
			fail();
	}
	
//	@Test
//	public void testGeofEnvelopeBuffer() throws MalformedQueryException, QueryEvaluationException
//	{ // TODO 3 arguments
//		String query = 
//			prefixes+
//			"SELECT DISTINCT ( geof:envelope(geof:buffer(?g, \"1.0\"^^xsd:float)) AS ?ret ) \n"+
//			"WHERE { \n" +
//			" ?s noa:hasGeometry ?g . \n"+
//			" ?s ex:id ?id . \n"+
//			"}";
//		
//		ArrayList<String> bindings = strabon.query(query,strabon.getSailRepoConnection());
//				
//		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((0 -1, 0 1, 2 1, 2 -1, 0 -1))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
//		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((1 -1, 1 1, 3 1, 3 -1, 1 -1))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
//		assertTrue(-1<bindings.indexOf("[ret=\"POLYGON ((2 -1, 2 1, 4 1, 4 -1, 2 -1))\"^^<http://strdf.di.uoa.gr/ontology#WKT>]"));
//	}
	
	//Simple Features - 8 functions - all with 2 arguments + boolean
	
	@Test
	public void testGeofSfEquals() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( geof:sf-equals(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}
	
	@Test
	public void testGeofSfDisjoint() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( geof:sf-disjoint(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(40, bindings.size());
		// too many results :)
//		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}
	
	@Test
	public void testGeofSfIntesects() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) < str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-intersects(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(4, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofSfTouches() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) < str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-touches(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofSfCrosses() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-crosses(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(4, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofSfWithin() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-within(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofSfContains() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-contains(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofSfOverlaps() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:sf-overlaps(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	//Egenhofer - 8 functions - all with 2 arguments + boolean
	
	@Test
	public void testGeofEhEquals() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( geof:eh-equals(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}
	
	@Test
	public void testGeofEhDisjoint() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( geof:eh-disjoint(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(40, bindings.size());
		// too many results :)
//		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}
	
	@Test
	public void testGeofEhMeet() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-meet(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"G\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofEhOverlap() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-overlap(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofEhCovers() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-covers(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofEhCoveredBy() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-coveredBy(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofEhInside() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-inside(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testGeofEhContains() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:eh-contains(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	//RCC8 - 8 functions - all with 2 arguments + boolean

	@Test
	public void testGeofRcc8Dc() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?s1 ?s2 \n"+
			"WHERE { \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" FILTER( str(?s1) < str(?s2) ) . \n"+
			" FILTER( geof:rcc8-dc(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(40, bindings.size());
		// too many results :)
//		assertTrue(-1<bindings.indexOf("[s2=http://example.org/pol11;s1=http://example.org/pol1]"));
	}
	
	@Test
	public void testGeofRcc8Ec() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:rcc8-po(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(2, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"B\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}

	@Test
	public void testGeofRcc8Tppi() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:rcc8-tppi(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}

	@Test
	public void testGeofRcc8Tpp() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:rcc8-tpp(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"F\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	

	@Test
	public void testGeofRcc8Ntpp() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:rcc8-ntpp(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	@Test
	public void testGeofRcc8Ntppi() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		String query = 
			prefixes+
			"SELECT DISTINCT ?id1 ?id2 \n"+
			"WHERE { \n" +
			" ?s1 ex:id ?id1 . \n"+
			" ?s2 ex:id ?id2 . \n"+
			" FILTER( str(?id1) != str(?id2) ) . \n"+
			" ?s2 ex:geometry ?g2 . \n" +
			" ?s1 ex:geometry ?g1 . \n"+
			" FILTER( geof:rcc8-ntpp(?g1, ?g2 )) . \n"+
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id2=\"Z\"^^<http://www.w3.org/2001/XMLSchema#string>;id1=\"E\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
}
