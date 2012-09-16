/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;

import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

abstract public class GeneralTests {
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
	//		strabon = new Strabon("cco2","postgres","p1r3as", 5432, "localhost", true);
	//
	//	}



	
	

	protected String prefixes = 
			"PREFIX lgdo:<http://linkedgeodata.org/ontology/> "+
					"PREFIX geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> "+
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "+
					"PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> "+
					"PREFIX geonames:<http://www.geonames.org/ontology/>";

	protected String query1 = 
			prefixes+
			"SELECT ?place ?placegeo "+ 
			"WHERE { "+
			"?place "+
			"rdfs:label ?placename ; "+	
			"geo:geometry ?placegeo ; "+
			"a ?type. "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)) "+
			"}";

	protected String query2 = 
			prefixes+
			"SELECT ?place (?placegeo AS ?tt1) (strdf:union(?placegeo,?placegeo) AS ?constr) "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)) "+
			"}";

	protected String query3 = 
			prefixes+
			"SELECT ?place (?placegeo AS ?constr) "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)) "+
			"}";

	protected String query4 =
			prefixes+
			"SELECT ?place ?placegeo "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo) " +
			"&& strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)) "+
			"}";

	protected String query5 =
			prefixes+
			"SELECT ?place ?placegeo "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)). "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)). "+
			"}";

	protected String query6 =
			prefixes+
			"SELECT (strdf:union(?placegeo,strdf:union(?placegeo,?placegeo)) AS ?constr1) " +
			"?place (?placegeo AS ?tt1) (strdf:union(?placegeo,?placegeo) AS ?constr2) "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo)) "+
			"}";

	protected String query7 = 
			prefixes+
			"SELECT ?place (?placegeo AS ?tt1) (strdf:union(?placegeo,?placegeo) AS ?constr) "+
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"   geo:geometry ?placegeo ; "+
			"   rdfs:label ?placename . "+
			"} ";

	protected String query8 = 
			prefixes+
			"SELECT ?place "+  
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(?placegeo,?placegeo)) "+
			"}";

	protected String queryBufferVar =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1)  "+
			"WHERE {  ?x lgdo:bufferExtent ?ext .  "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(?placegeo,?ext),?placegeo)) "+
			"}";

	protected String queryBufferConst =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1)  "+
			"WHERE {  ?x lgdo:bufferExtent ?ext .  "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(?placegeo,2),?placegeo)) "+
			"}";

	protected String queryBufferConst2 =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1)  "+
			"WHERE {  ?x lgdo:bufferExtent ?ext .  "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(\"POINT(23.72873 37.97205)\"^^<http://strdf.di.uoa.gr/ontology#WKT>,0.0572),?placegeo)) "+
			"}";

	protected String queryBufferConstInSelect =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1) (strdf:buffer(?placegeo,2.5) AS ?buf) "+
			"WHERE {  ?x lgdo:bufferExtent ?ext .  "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(?placegeo,2.5),?placegeo)) "+
			"}";


	protected String queryBufferSelectFilterA =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1) (strdf:buffer(?placegeo,?ext) AS ?buf) "+
			"WHERE { ?x lgdo:bufferExtent ?ext . "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(?placegeo,2),?placegeo)) "+
			"}";

	protected String queryBufferSelectFilterB =
			prefixes+
			"SELECT  ?place (?placegeo AS ?tt1) (strdf:buffer(?placegeo,3) AS ?buf) "+
			"WHERE { ?x lgdo:bufferExtent ?ext . "+ 
			"?place "+
			"	a ?type ; "+
			"	geo:geometry ?placegeo ; "+
			"	rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:buffer(?placegeo,?ext),?placegeo)) "+
			"}";

	protected String queryEnvelopeConvexHull = 
			prefixes+
			"SELECT ?place (?placegeo AS ?tt1) (strdf:envelope(?placegeo) AS ?constr)  (strdf:convexHull(?placegeo) AS ?constr2) "+ 
			"WHERE { "+
			"?place "+
			"	a ?type ; "+
			"   geo:geometry ?placegeo ; "+
			"   rdfs:label ?placename . "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,strdf:envelope(?placegeo)),?placegeo) && strdf:anyInteract(strdf:convexHull(?placegeo),?placegeo)) "+
			"}";

	protected String queryMetrics1 =
			prefixes+
			"SELECT ?place ?placegeo  ( strdf:area(?placegeo) AS ?area) "+
			"( strdf:envelope(?placegeo) AS ?mbb) "+
			"WHERE {   ?x lgdo:bufferExtent ?ext .  "+
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(2*strdf:distance(?placegeo,?placegeo) + strdf:area(?placegeo) - 3 + ?ext  "+
			"< ?ext + 8 - strdf:distance(?placegeo,?placegeo)) "+
			"}";

	protected String queryMetricsLightOptimized = 
			prefixes+
			"SELECT ?place ?placegeo  ( 33*strdf:area(?placegeo) + 114 AS ?area) ( strdf:envelope(?placegeo) AS ?mbb) "+
			"WHERE {   ?x lgdo:bufferExtent ?ext . "+ 
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(2*strdf:distance(?placegeo,?placegeo) < 8) "+
			"}";

	//Metrics with only one var must not be optimized
	protected String queryMetricsAreaMustNotBeOptimized =
			prefixes+
			"SELECT ?place ?placegeo  ( 33*strdf:area(?placegeo) + 114 AS ?area) ( strdf:envelope(?placegeo) AS ?mbb) "+
			"WHERE {   ?x lgdo:bufferExtent ?ext . "+ 
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(2*strdf:area(?placegeo) < 8) "+
			"}";

	protected String queryMetricsMathInSelect =
			prefixes+
			"SELECT ?place ?placegeo  ( 33*strdf:area(?placegeo) + 114 AS ?area) ( strdf:envelope(?placegeo) AS ?mbb) "+
			"WHERE {   ?x lgdo:bufferExtent ?ext . "+ 
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(2*strdf:distance(?placegeo,?placegeo) + strdf:area(?placegeo) - 3 + ?ext " +
			"< ?ext + 8 - strdf:distance(?placegeo,?placegeo)) "+
			"}";

	protected String querySpatialPropertiesMonetDB = 
			prefixes+
			"SELECT ?place ?placegeo (strdf:dimension(?placegeo) + 2 AS ?dim) (strdf:srid(?placegeo) AS ?srid) "+
			"WHERE { "+
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo) && strdf:isSimple(?placegeo) "+ 
//			"&& strdf:dimension(?placegeo) - 1 < 3" +
			") "+
			"}";
	
	protected String querySpatialPropertiesPostGIS = 
		prefixes+
		"SELECT ?place ?placegeo (strdf:dimension(?placegeo) + 2 AS ?dim) (strdf:srid(?placegeo) AS ?srid) "+
		"WHERE { "+
		"?place "+
		"	rdfs:label ?placename ; "+	
		"	geo:geometry ?placegeo ; "+
		"	a ?type. "+
		"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo) && strdf:isSimple(?placegeo) "+ 
		"&& strdf:dimension(?placegeo) - 1 < 3) "+
		"}";

	protected String querySpatialPropertiesConstMonetDB = 
			prefixes+
			"SELECT ?place ?placegeo (strdf:srid(?placegeo) AS ?srid) (strdf:dimension(?placegeo) + 2 AS ?dim)  "+
			"WHERE { "+
			"?place "+
			"	rdfs:label ?placename ; "+	
			"	geo:geometry ?placegeo ; "+
			"	a ?type. "+
			"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo) && strdf:isSimple(?placegeo) "+ 
//			"&& strdf:dimension(\"POINT(0 0)\") - 1 < 3" +
			") "+
			"}";

	protected String querySpatialPropertiesConstPostGIS = 
		prefixes+
		"SELECT ?place ?placegeo (strdf:srid(?placegeo) AS ?srid) (strdf:dimension(?placegeo) + 2 AS ?dim)  "+
		"WHERE { "+
		"?place "+
		"	rdfs:label ?placename ; "+	
		"	geo:geometry ?placegeo ; "+
		"	a ?type. "+
		"FILTER(strdf:anyInteract(strdf:union(?placegeo,?placegeo),?placegeo) && strdf:isSimple(?placegeo) "+ 
		"&& strdf:dimension(\"POINT(0 0)\") - 1 < 3) "+
		"}";
	
	protected String queryRelate = 
			prefixes+
			"SELECT ?place ?placegeo "+
			"WHERE { "+
			"?place "+
			"rdfs:label ?placename ; "+	
			"geo:geometry ?placegeo ; "+
			"a ?type. "+
			"FILTER(strdf:relate(?placegeo,?placegeo,\"0FFFFF212\")) "+
			"}";

	//	protected String queryThematicUnion = 
	//			prefixes+
	//			"SELECT ?place "+
	//			"WHERE { "+
	//			"?place "+
	//			"rdfs:label ?placename ; "+	
	//			"geo:geometry ?placegeo. "+ 
	//			"{?place a lgdo:Pier . } " +
	//			"UNION" +
	//			"{?place a lgdo:Harbour . } }";

	
	/**
	 * The following three queries test potential bugs noticed during the evaluation process for www2012.
	 * No issue seems to be present.
	 */
	protected String queryThematicUnion = 
			prefixes+
			"SELECT ?place2 ?placename "+
			" WHERE { "+
			" ?place2 rdfs:label ?placename. "+	
			" ?place2 strdf:hasGeometry ?placegeo.  " +
			"{" +
			" ?place2 a <http://linkedgeodata.org/ontology#Harbour> ." +
			" } " +
			"UNION" +
			"{" +
			" ?place2 a <http://linkedgeodata.org/ontology#Pier> ." +
			" } " +
			"<http://www.geonames.org/ontology/Athens> strdf:hasGeometry ?ar . " +
			"FILTER(strdf:disjoint(?placegeo,?ar))" +
			"}";

	protected String query6_real = prefixes+
			"SELECT ?capital ?pierLabel "+
			"WHERE  "+
			"{ "+
			"?country a <http://dbpedia.org/resource/Country> . "+
			"?country <http://dbpedia.org/property/capital> ?capital_label . "+
			"?capital <http://www.w3.org/2000/01/rdf-schema#label> ?capital_label . "+
			"?capitalGeoNames <http://www.w3.org/2002/07/owl#sameAs> ?capital . "+
			"?capitalGeoNames strdf:hasGeometry ?capitalGeo . "+
			"OPTIONAL "+ 
			"{ "+ 
			" ?pier a <http://linkedgeodata.org/ontology#Pier> . " +
			" ?pier rdfs:label ?pierLabel . "+
			" ?pier strdf:hasGeometry ?pierGeo . "+
			" FILTER(strdf:distance(?capitalGeo,?pierGeo) < 3.7) "+
			"} "+
			"} ";

	protected String query6_real_union = prefixes+
			"SELECT ?capital ?pierLabel "+
			"WHERE  "+
			"{ "+
			"?country a <http://dbpedia.org/resource/Country> . "+
			"?country <http://dbpedia.org/property/capital> ?capital_label . "+
			"?capital <http://www.w3.org/2000/01/rdf-schema#label> ?capital_label . "+
			"?capitalGeoNames <http://www.w3.org/2002/07/owl#sameAs> ?capital . "+
			"?capitalGeoNames strdf:hasGeometry ?capitalGeo . "+
			"OPTIONAL "+ 
			"{ "+ 
			" { " +
			"?pier a <http://linkedgeodata.org/ontology#Pier> . " +
			"}" +
			" UNION" +
			" { " +
			"?pier a <http://linkedgeodata.org/ontology#Harbour> . " +
			"}" +
			" ?pier rdfs:label ?pierLabel . "+
			" ?pier strdf:hasGeometry ?pierGeo . "+
			" FILTER(strdf:distance(?capitalGeo,?pierGeo) < 3.7) "+
			"} "+
			"} ";

	protected String queryNegation = prefixes+
			"SELECT ?capital ?pier "+
			"WHERE  "+
			"{ "+
			" ?country a <http://dbpedia.org/resource/Country> . "+
			" ?country <http://dbpedia.org/property/capital> ?capital_label . "+
			" ?capital <http://www.w3.org/2000/01/rdf-schema#label> ?capital_label . "+
			" ?capitalGeoNames <http://www.w3.org/2002/07/owl#sameAs> ?capital . "+
			" ?capitalGeoNames strdf:hasGeometry ?capitalGeo . "+
			"OPTIONAL "+
			"{ "+ 
			" ?pier a <http://linkedgeodata.org/ontology#Harbour> . " +
			" ?pier rdfs:label ?pierLabel . "+
			" ?pier strdf:hasGeometry ?pierGeo . "+
			" FILTER(strdf:distance(?capitalGeo,?pierGeo) < 3.7) "+
			"} . " +
			"FILTER (!bound(?pier))" +
			"} "+
			"";
	
	//Representative of functionality. If I remove !bound, 3 more results appear
	protected String queryNegation2 = prefixes+
			"SELECT ?s ?pier "+
			"WHERE  "+
			"{ "+
			" ?s strdf:hasGeometry ?geo1 . "+
			"OPTIONAL "+
			"{ "+ 
			" ?pier a <http://linkedgeodata.org/ontology#Harbour> . " +
			" ?pier rdfs:label ?pierLabel . "+
			" ?pier strdf:hasGeometry ?pierGeo . "+
			" FILTER(strdf:distance(?geo1,?pierGeo) < 0.6) "+
			"} . " +
			"FILTER (!bound(?pier))" +
			"} "+
			"";
	
	
	protected String queryNotExists = 
		prefixes+
		"SELECT ?capital ?pierLabel "+
		"WHERE  "+
		"{ "+
		"?country a <http://dbpedia.org/resource/Country> . "+
		"?country <http://dbpedia.org/property/capital> ?capital_label . "+
		"?capital <http://www.w3.org/2000/01/rdf-schema#label> ?capital_label . "+
		"?capitalGeoNames <http://www.w3.org/2002/07/owl#sameAs> ?capital . "+
		"?capitalGeoNames strdf:hasGeometry ?capitalGeo . "+
		" ?pier a <http://linkedgeodata.org/ontology#Pier> . " +
		" ?pier rdfs:label ?pierLabel . "+
		" ?pier strdf:hasGeometry ?pierGeo . "+
//		" FILTER NOT EXISTS { ?pier rdfs:label \"Kikirikou\". " +
//		"FILTER(strdf:distance(?capitalGeo,?pierGeo) > 3.7) " +
		" FILTER NOT EXISTS { ?pier rdfs:label ?label2. FILTER(?label2 != ?pierLabel)" +
		"}"+
		"} ";
	
	//TODO At some point I should also add a test where the 3rd argument of strdf:relate is a variable

	@Test
	public void testQuery1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query1,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery3() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query3,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery4() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query4,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery5() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query5,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery6() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query6,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery7() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query7,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery8() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query8,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferVar() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferVar,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferConst() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferConst,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferConst2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferConst2,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferConstInSelect() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferConstInSelect,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferSelectFilterA() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferSelectFilterA,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryBufferSelectFilterB() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferSelectFilterB,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryEnvelopeConvexHull() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryBufferSelectFilterB,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryMetrics1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryMetrics1,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryMetricsLightOptimized() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryMetricsLightOptimized,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryMetricsAreaMustNotBeOptimized() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryMetricsLightOptimized,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryMetricsMathInSelect() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryMetricsMathInSelect,strabon.getSailRepoConnection());

	}

	@Test
	// MonetDB has not function Dimension
	abstract public void testQuerySpatialProperties() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException ;

	@Test
	// MonetDB has not function Dimension
	abstract public void testQuerySpatialPropertiesConst() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException ;
	
	@Test
	public void testQueryRelate() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryRelate,strabon.getSailRepoConnection());

	}

	@Test
	public void testQueryThematicUnion() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryThematicUnion,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery6_real() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query6_real,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery6_real_union() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query6_real_union,strabon.getSailRepoConnection());

	}
	@Test
	public void testQueryNegation() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryNegation,strabon.getSailRepoConnection());

	}	
	@Test
	public void testQueryNegation2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(queryNegation2,strabon.getSailRepoConnection());

	}	

//		@Test
//		public void testPrintDataset() throws MalformedQueryException
//		{
//				String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
//				strabon.query(query,strabon.getSailRepoConnection());
//	
//		}
}
