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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;

public class SPARQL11Tests {
	public static Strabon strabon;

	public String 	FOAF_NS = "http://xmlns.com/foaf/0.1/",
	DT_NS = "http://example.org/datatype#",
	NS_NS = "http://example.org/ns#",
	DEF_NS = "http://example.org#",
	ORG_NS = "http://example.com/org#",
	DC_NS = "http://purl.org/dc/elements/1.1/",
	DC10_NS = "http://purl.org/dc/elements/1.0/",
	DC11_NS = "http://purl.org/dc/elements/1.1/";

	protected String prefixes = 
		"PREFIX rdf: <"+RDF.NAMESPACE+"> \n" +
		"PREFIX xsd: <"+XMLSchema.NAMESPACE+"> \n" +
		"PREFIX foaf: <"+FOAF_NS+"> \n" +
		"PREFIX dt: <"+DT_NS+"> "+
		"PREFIX ns: <"+NS_NS+"> \n" +
		"PREFIX def: <"+DEF_NS+"> \n" +
		"PREFIX org: <"+ORG_NS+"> \n" +
		"PREFIX dc: <"+DC_NS+"> \n" +
		"PREFIX dc10: <"+DC10_NS+"> \n" +
		"PREFIX dc11: <"+DC11_NS+"> \n";
	
//	@BeforeClass
//	public static void initialize() throws Exception
//	{
//		strabon = new eu.earthobservatory.runtime.postgis.Strabon("test", "postgres", "postgres", 5432, "localhost", true);
//
//	}

	@Test
	public void testRetrieveEveryEntry() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{		
		String queryString = prefixes+
			"SELECT ?s ?p ?o \n" +
			"WHERE \n" +
			"{ ?s ?p ?o . } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results =  (ArrayList <String>)strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(62, results.size());
	}
	
	@Test
	public void testMultipleMatches() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{		
		String queryString = prefixes+
			"SELECT DISTINCT ?name ?mbox \n" +
			"WHERE \n" +
			"{ ?x foaf:name ?name . \n" +
			" ?x foaf:mbox ?mbox ." +
			" } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList <String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(11, results.size());
	}
	
	@Test
	public void testMatchingLiterals() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{				
		String queryString = prefixes+
		"SELECT ?v WHERE { ?v ns:p ?s . FILTER(str(?s) = \"cat\") } \n";
//		"SELECT ?v WHERE { ?v ns:p \"cat\" } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList <String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[v=http://example.org#x]"));
	}
	
	@Test
	public void testMatchingLiteralsWithLanguage() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?v WHERE { ?v ns:p \"cat\"@en } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList <String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[v=http://example.org#x]"));
	}
	
	@Test
	public void testMatchingLiteralsWithNumericTypes() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?v WHERE { ?v ns:p 42 } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList <String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[v=http://example.org#y]"));
	}
	
	@Test
	public void testMatchingLiteralWithArbitraryTypes() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?v WHERE { ?v ?p \"abc\"^^dt:specialDatatype } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList <String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[v=http://example.org#z]"));
	}

	@Test
	public void testBind() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?name  \n" +
			"WHERE  { \n" +
			" ?P foaf:givenName ?G . \n" +
			" ?P foaf:surname ?S . \n" +
			" BIND( CONCAT(?G, \" \", ?S) AS ?name ) . \n"+
			"} \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[name=\"John Doe\"]"));
	}

	
	@Test
	public void testConcat() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ( CONCAT(?G, \" \", ?S) AS ?name ) \n" +
			"WHERE  { ?P foaf:givenName ?G ; foaf:surname ?S } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[name=\"John Doe\"]"));
	}
	
	@Test
	public void testOptional() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?name ?mbox \n"+
			"WHERE  { ?x foaf:name  ?name . \n"+
			"	OPTIONAL { ?x  foaf:mbox  ?mbox } \n"+
       		"} \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>)strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(13, results.size());
		assertTrue(-1<results.indexOf("[name=\"Alice\";mbox=mailto:alice@example.com]"));
		assertTrue(-1<results.indexOf("[name=\"Alice\";mbox=mailto:alice@work.example]"));
		assertTrue(-1<results.indexOf("[name=\"Carol Baz\"]"));
	}
	
	@Test
	public void testOptionalFilter() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT  ?title ?price \n"+
			"WHERE   { ?x dc:title ?title . \n"+
			"	OPTIONAL { ?x ns:price ?price . FILTER (?price < 30) } \n"+
        	"}\n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(4, results.size());
		assertTrue(-1<results.indexOf("[title=\"SPARQL Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL Protocol Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL (updated)\"]"));
		assertTrue(-1<results.indexOf("[title=\"The Semantic Web\";price=\"23\"^^<http://www.w3.org/2001/XMLSchema#integer>]"));
	}
	
	@Test
	public void testTwoOptionals() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?name ?mbox ?hpage \n"+
			"WHERE  { ?x foaf:name  ?name \n"+
			"	OPTIONAL { ?x foaf:mbox ?mbox } . \n"+
			"	OPTIONAL { ?x foaf:homepage ?hpage } . \n"+
       		"} ORDER BY ?name \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>)strabon.query(queryString, strabon.getSailRepoConnection());
		
		assertEquals(13, results.size());
		assertTrue(-1<results.indexOf("[hpage=http://work.example.org/alice/;name=\"A. Foo\";mbox=mailto:alice@example.com]"));
		assertTrue(-1<results.indexOf("[name=\"Bob\";mbox=mailto:bob@work.example]"));
		assertTrue(-1<results.indexOf("[name=\"C. Baz\"]"));
	}

	
	@Test
	public void testFilterNotExists() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?person \n"+
			"WHERE { \n"+
			"	?person rdf:type  foaf:Person . \n"+
			"	FILTER NOT EXISTS { ?person foaf:name ?name } . \n"+
			"}    \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(0, results.size()); // TODO add such an item
	}
	
	@Test
	public void testFilterExists() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?person \n"+
			"WHERE { \n"+
			"	?person rdf:type  foaf:Person . \n"+
			"	FILTER EXISTS { ?person foaf:name ?name } \n"+
			"} \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(2, results.size());
		assertTrue(-1<results.indexOf("[person=http://example.org#bob]"));
		assertTrue(-1<results.indexOf("[person=http://example.org#alice]"));
	}
	
	@Test
	public void testMinus() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT DISTINCT ?s \n"+
			"WHERE { \n"+
			"	?s ?p ?o . \n"+
			"MINUS { \n"+
			"	?s foaf:givenName \"Bob\" . \n"+
			"} } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(19, results.size());
	}
	
	@Test
	public void testQuerySumGroupByHaving() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT (SUM(?lprice) AS ?totalPrice) \n"+
			"WHERE { \n"+
			"	?org def:affiliates ?auth . \n"+
			"	?auth def:writesBook ?book . \n"+
			"	?book def:price ?lprice . \n"+
			"} \n" 
			+"GROUP BY ?org HAVING (SUM(?lprice) > 10) \n"
			;
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[totalPrice=\"21\"^^<http://www.w3.org/2001/XMLSchema#integer>]"));
	}

	@Test
	public void testRegex() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT  ?title \n"+
			"WHERE   { ?x dc:title ?title \n"+
			"	FILTER regex(?title, \"^SPARQL\") \n"+ 
        	"} ORDER BY ?title\n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(3, results.size());
		assertTrue(-1<results.indexOf("[title=\"SPARQL Protocol Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL (updated)\"]"));
	}
	
	@Test
	public void testRegexI() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT  ?title \n"+
			"WHERE   { ?x dc:title ?title . \n"+
			"	FILTER regex(?title, \"web\", \"i\" ) ."+ 
        	"} \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(1, results.size());
		assertTrue(-1<results.indexOf("[title=\"The Semantic Web\"]"));
	}
	
	@Test
	public void testUnionWithCommonObject() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?title \n"+
			"WHERE  { { ?book dc10:title  ?title } UNION { ?book dc11:title  ?title } } \n";
		
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(6, results.size());
		assertTrue(-1<results.indexOf("[title=\"SPARQL Query Language Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"The Semantic Web\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL Protocol Tutorial\"]"));
		assertTrue(-1<results.indexOf("[title=\"SPARQL (updated)\"]"));
	}
	
	@Test
	public void testUnionWithDifferentObject() throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{	
		String queryString = prefixes+
			"SELECT ?x ?y \n"+
			"WHERE  { { ?book dc10:title  ?x } UNION { ?book dc11:title  ?y } } \n";
	
		@SuppressWarnings("unchecked")
		ArrayList<String> results = (ArrayList<String>) strabon.query(queryString, strabon.getSailRepoConnection());
		assertEquals(6, results.size());
		assertTrue(-1<results.indexOf("[x=\"SPARQL Query Language Tutorial\"]"));
		assertTrue(-1<results.indexOf("[x=\"SPARQL\"]"));
		assertTrue(-1<results.indexOf("[y=\"SPARQL Tutorial\"]"));
		assertTrue(-1<results.indexOf("[y=\"The Semantic Web\"]"));
		assertTrue(-1<results.indexOf("[y=\"SPARQL Protocol Tutorial\"]"));
		assertTrue(-1<results.indexOf("[y=\"SPARQL (updated)\"]"));
	}

}