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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class SimpleTests {

	protected static Strabon strabon;

	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 

	protected static String jdbcDriver = null;
	protected static String serverName = null;
	protected static String username = null;
	protected static String password = null;
	protected static Integer port = null;
	
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
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[s2=http://example.org/item1]"));
	}
	
	@Test
	public void testComplexFilter() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT ?s ?id "+ 
			"WHERE { "+
				"?s ex:id ?id . "+
				"FILTER( ?id>20 || ?id<2 ) . " +
			"}";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
				
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id=\"1\"^^<http://www.w3.org/2001/XMLSchema#int>;s=http://example.org/item1]"));
	}
	
	@Test
	public void testNotExistingPredicate() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT ?s ?o  "+ 
			"WHERE { "+
				"?s ex:notExisting ?o . "+	
			"}";
	
		@SuppressWarnings("unchecked")
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());

		assertEquals(0, bindings.size());
	}

}
