/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgres.temporals;



import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;


import eu.earthobservatory.runtime.generaldb.*;
import eu.earthobservatory.runtime.postgres.temporals.TemplateTests;


/**
 * A set of simple tests on query rewriting functionality 
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class QueryRewritingTests {

	protected static Strabon strabon;
	
	protected static final String prefixes = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
		"PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n" +
		"PREFIX ex: <http://example.org/> \n" +
		"PREFIX xs: <http://www.w3.org/2001/XMLSchema#> \n" +
		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n";

	
	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		strabon = TemplateTests.beforeClass("/temporal-selection-tests.nq","NQUADS");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
	
	
	
	@Test
	public void testQueryRewriting1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT ?geo1 "+ 
			"WHERE { "+
				"?x strdf:hasGeometry ?geo1 . "+
				"?y strdf:hasGeometry ?geo2 . "+
				"FILTER(strdf:intersects(?geo1, ?geo2))."+
				"}";
		
		String [] querySplit=utils.queryRewriting(query).split("GRAPH");		
		assertEquals(1, querySplit.length);
	}
	

	@Test
	public void testQueryRewriting2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"select ?geo ?s1 ?s2 " +
			"where {" +
			"?s1  ?p1 ?o1 ?t1 ." +
			" ?s2  ?p2 ?o2 ?t2 ." +
			"?x strdf:hasGeometry ?geo1 ." +
			"?y strdf:hasGeometry ?geo2." +
			"FILTER(strdf:intersects(?geo1,?geo2))." +
			"   FILTER(strdf:afterPeriod(?t1,?t2))}";	
		
		String [] querySplit=utils.queryRewriting(query).split("GRAPH");		
		assertEquals(3, querySplit.length);
	}


	@Test
	public void testQueryRewriting3() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"select ?s1 " +
			"where { " +
			"?s1 ?p1 ?o1 ?t1 ." +
			"?s2 ?p2 ?o2 ?t2 ." +
			"?x1 <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry>    ?geo1. " +
			"?x2 <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry>    ?geo2. " +
			"FILTER(strdf:disjoint(?geo1,?geo2))" +
			"FILTER(strdf:afterPeriod(?t1, ?t2)).";
		
		String [] querySplit=utils.queryRewriting(query).split("GRAPH");		
		assertEquals(3, querySplit.length);
	}
	
	@Test
	public void testQueryRewriting4() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"select distinct ?s1 ?s2 " +
			"where {" +
			"?s1 ?p1 ?ro1 ?o1 ." +
			"#?s2 ?p2 ?ro2 ?o2 . \n" +
			"FILTER(strdf:PeriodOverlaps(?o1, \"[2012-11-19 12:41:00+02, 2012-11-19 13:41:00.000001+02]\"^^<http://strdf.di.uoa.gr/ontology#validTime> ))" +
			"}";
		
		String [] querySplit=utils.queryRewriting(query).split("GRAPH");
		assertEquals(2, querySplit.length);
	}
	
	
	@Test
	public void testQueryRewriting5() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"select distinct ?s1 ?s2 " +
			"where {" +
			"?s1 ?p1 ?ro1 ?o1 . " +
			"FILTER(strdf:PeriodOverlaps(?o1, \"[2012-11-19 12:41:00+02, 2012-11-19 13:41:00.000001+02]\"^^<http://strdf.di.uoa.gr/ontology#validTime>)) " +
			"#FILTER(strdf:PeriodOverlaps(?o1, \"Now\"^^<http://strdf.di.uoa.gr/ontology#validTime> )) \n " +
			"}";
		
		String [] querySplit=utils.queryRewriting(query).split("GRAPH");
		assertEquals(2, querySplit.length);
	}
}
