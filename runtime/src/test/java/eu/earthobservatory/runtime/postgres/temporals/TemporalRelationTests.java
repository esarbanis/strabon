/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */

/**
 * 
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 * 
 * This class includes  a set of junit tests that test the temporal relations between periods
 */

package eu.earthobservatory.runtime.postgres.temporals;


import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
//import org.openrdf.query.resultio.Format;
import eu.earthobservatory.utils.Format;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultWriterFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;


import eu.earthobservatory.runtime.generaldb.*;
import eu.earthobservatory.runtime.postgres.temporals.TemplateTests;


/**
 * A set of simple tests on query rewriting functionality 
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 */
public class TemporalRelationTests {

	protected static Strabon strabon;
	
	protected static final String prefixes = 
		"PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n" +
		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n";

	
	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		strabon = TemplateTests.beforeClass("/temporal-periods.nq","NQUADS");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
	
	
	
	@Test
	public void testAfter() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:after(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertEquals(25, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item8]"));
		
		

	}
	
	@Test
	public void testBefore() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:before(?t1, ?t2) && ?x2!=?x1)."+
				"}";
		
		//TupleQueryResult result = (TupleQueryResult) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		//System.out.println(bindings.toString());
		
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertEquals(25, bindings.size());
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item5]"));
	
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item6]"));
	}
	
	@Test
	public void testOverright() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:overright(?t1, ?t2) && ?x1 != ?x2)."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		assertEquals(31, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
	}
	
	@Test
	public void testOverleft() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:overleft(?t1, ?t2) && ?x1 != ?x2)."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		assertEquals(14, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item8]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item7]"));
	}
	
	@Test
	public void testPeriodOverlaps() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:PeriodOverlaps(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
	
		assertEquals(6, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item2]"));
		
	}
	
	@Test
	public void testPeriodIntersects() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:PeriodIntersects(?t1, ?t2) && str(?x1) < str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
	
		assertEquals(7, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item2]"));
		
	}
	
	@Test
	public void testDuring() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:during(?t1, ?t2) && ?x1 != ?x2)."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
	
		assertEquals(8, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item1;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item8]"));
		
	}
	@Test
	public void testAdjacent() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:adjacent(?t1, ?t2) && str(?x1) < str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		assertEquals(1, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item7]"));
	}
	
	@Test
	public void testMeets() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:meets(?t1, ?t2) && ?x1 != ?x2)."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		assertEquals(3, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item2]"));
	}
	
	@Test
	public void testStarts() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:starts(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());

		assertEquals(1, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		for(String result: bindings)
		/*{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		
	}
	
	@Test
	public void testFinishes() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:finishes(?t1, ?t2) && str(?x1) > str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		

		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item8]"));
	}
	
	@Test
	public void testEqualsPeriod() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:equalsPeriod(?t1, ?t2) && str(?x1) < str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		//assertEquals(3, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
	}
	
	@Test
	public void testNequalsPeriod() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct ?x1 ?x2 "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:nequalsPeriod(?t1, ?t2) && str(?x1) < str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		assertEquals(28, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		/*for(String result: bindings)
		{
			System.out.println(result.toString());
		}*/
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item2;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item3;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item5;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item6;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item4;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item7]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item4]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item6]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item3]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item2]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item7;x1=http://example.org/item1]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item5]"));
		assertTrue(-1 < bindings.indexOf("[x2=http://example.org/item8;x1=http://example.org/item2]"));

}
}
	