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

public class MeaningfulAggregateTests {
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

	

	String prefixes =
		" PREFIX noa:<http://www.earthobservatory.eu/ontologies/noaOntology.owl#>"+
		" PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
		" PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> ";

	
	/**
	 * May seem like this query has a spatial dimension, 
	 * but since the spatial literal is not involved in any
	 * spatial operations, it is evaluated as a plain literal
	 */
	String query1NotSpatial = prefixes +
			"SELECT ( COUNT(?hotspot) AS ?count) ?geo "+
			"WHERE " +
			"{ " +
			"	?hotspot a noa:Hotspot;" +
			"			 strdf:hasGeometry ?geo. " +
			"}"+
			"GROUP BY ?geo";
		
	/**
	 * Current results: 6 - 4 - 2
	 */
	String query1Spatial = prefixes +
			"SELECT ( COUNT(?hotspot) AS ?count) ?geo "+
			"WHERE " +
			"{ " +
			"	?hotspot a noa:Hotspot;" +
			"			 strdf:hasGeometry ?geo. " +
			"   ?urbanArea a noa:UrbanArea; " +
			" 			   strdf:hasGeometry ?uaGeo."+
			"	FILTER(strdf:disjoint(?geo,?uaGeo)) "+	
			"}"+
			"GROUP BY ?geo";
	
	
	/**
	 * Functionality seems ok
	 */
	String query2simple = prefixes + 
			"SELECT ?burntArea ?baGeo " +
			"(strdf:union(?uaGeo) AS ?urban) "+
			"WHERE { " +
			" ?burntArea a noa:BurntArea; " +
			" 		     strdf:hasGeometry ?baGeo. " +
			" ?urbanArea a noa:UrbanArea; " +
			"			 strdf:hasGeometry ?uaGeo. " +
			" FILTER(strdf:mbbIntersects(?baGeo,?uaGeo))" +
			" } " +
			" GROUP BY ?burntArea ?baGeo "+ 
			" ";
	
	String query2complexA = prefixes + 
			"SELECT ?burntArea " +
//			"?baGeo " +
			"(strdf:difference(?baGeo, strdf:union(?uaGeo)) AS ?urbanPiece) "+
			" WHERE { " +
			" ?burntArea a noa:BurntArea; " +
			" 		     strdf:hasGeometry ?baGeo. " +
			" ?urbanArea a noa:UrbanArea; " +
			"			 strdf:hasGeometry ?uaGeo. " +
			" FILTER(strdf:mbbIntersects(?baGeo,?uaGeo))" +
			" } " +
			" GROUP BY ?burntArea" +
//			" ?baGeo "+ 
			" ";
	
	String query2complexB = prefixes + 
			"SELECT ?burntArea ?baGeo " +
			"(strdf:difference(strdf:union(?uaGeo) , ?baGeo) AS ?urbanPiece) "+
			" WHERE { " +
			" ?burntArea a noa:BurntArea; " +
			" 		     strdf:hasGeometry ?baGeo. " +
			" ?urbanArea a noa:UrbanArea; " +
			"			 strdf:hasGeometry ?uaGeo. " +
			" FILTER(strdf:mbbIntersects(?baGeo,?uaGeo))" +
			" } " +
			" GROUP BY ?burntArea ?baGeo "+ 
			" ";
	
	String query2complexC = prefixes + 
			"SELECT ?burntArea ?baGeo " +
			"(strdf:buffer(strdf:union(?uaGeo) , 5) AS ?urbanPiece) "+
			" WHERE { " +
			" ?burntArea a noa:BurntArea; " +
			" 		     strdf:hasGeometry ?baGeo. " +
			" ?urbanArea a noa:UrbanArea; " +
			"			 strdf:hasGeometry ?uaGeo. " +
			" FILTER(strdf:mbbIntersects(?baGeo,?uaGeo))" +
			" } " +
			" GROUP BY ?burntArea ?baGeo "+ 
			" ";
	
	String query2complexD = prefixes + 
	"SELECT " +
	" ?burntArea " +
//	" ?baGeo " +
//	" (strdf:union(?uaGeo) AS ?urban) " +
	" (COUNT(?uaGeo) AS ?urbanNo) "+
	"WHERE { " +
	" ?burntArea a noa:BurntArea; " +
	" 		     strdf:hasGeometry ?baGeo. " +
	" ?urbanArea a noa:UrbanArea; " +
	"			 strdf:hasGeometry ?uaGeo. " +
	" FILTER(strdf:mbbIntersects(?baGeo,?uaGeo))" +
	" } " +
	" GROUP BY ?burntArea ?baGeo "+ 
	" HAVING (COUNT(?uaGeo) > 1 )"+
	" "
	;
	
	@Test
	public void testQuery1NotSpatial() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query1NotSpatial,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery1Spatial() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query1Spatial,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2simple,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery2complexA() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2complexA,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery2complexB() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2complexB,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery2complexC() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2complexC,strabon.getSailRepoConnection());

	}
	@Test
	public void testQuery2complexD() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2complexD,strabon.getSailRepoConnection());

	}

}
