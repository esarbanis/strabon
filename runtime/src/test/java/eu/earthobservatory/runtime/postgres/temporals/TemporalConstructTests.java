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
 * A set of simple tests on temporal selection functionality 
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
///import org.openrdf.query.resultio.Format;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultWriterFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.utils.Format;


import eu.earthobservatory.runtime.generaldb.*;
import eu.earthobservatory.runtime.postgres.temporals.TemplateTests;


/**
 * A set of simple tests on query rewriting functionality 
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 * 
 * This class includes  a set of junit tests that test the temporal constructs that appear in
 * the select clause of the query
 * 
 */

public class TemporalConstructTests {

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
	public void testIntersection() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct (strdf:period_intersect(?t1, ?t2) AS ?intersection) "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:PeriodOverlaps(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		for(String result: bindings)
		{
			System.out.println(result.toString());
		}
		assertEquals(5, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(bindings.contains("intersection=\"[2012-11-19 12:41:00+02, 2012-11-19 13:41:00.000001+02)\""));
		assertTrue(bindings.contains("intersection=\"[2012-11-19 13:41:00+02, 2012-11-19 13:41:00.000001+02)\""));
		assertTrue(bindings.contains("intersection=\"[2012-11-19 10:41:00+02, 2012-11-19 11:41:00.000001+02)\""));
		assertTrue(bindings.contains("intersection=\"[2012-11-19 13:41:00+02, 2012-11-19 14:41:00.000001+02)\""));
		assertTrue(bindings.contains("intersection=\"[2012-11-19 14:41:00.000001+02, 2012-11-19 15:41:00.000001+02)\""));
}
	
	@Test
	public void testUnion() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct (strdf:period_union(?t1, ?t2) AS ?union) "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:PeriodOverlaps(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		for(String result: bindings)
		{
			System.out.println(result.toString());
		}
		assertEquals(3, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(bindings.contains("union=\"[2012-11-19 12:41:00+02, 2012-11-19 13:41:00.000001+02)\""));
		assertTrue(bindings.contains("union=\"[2012-11-19 12:41:00+02, 2012-11-19 14:41:00.000001+02)\""));
		assertTrue(bindings.contains("union=\"[2012-11-19 10:41:00+02, 2012-11-19 15:41:00.000001+02)\"")); 
}
	
	@Test
	public void testMinus() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT distinct (strdf:period_minus(?t1, ?t2) AS ?minus) "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				"?x2 ?y2 ?z2 ?t2 . "+
				"FILTER(strdf:during(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		for(String result: bindings)
		{
			System.out.println(result.toString());
		}
		assertEquals(4, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(bindings.contains("minus=\"[2012-11-19 13:41:00.000001+02, 2012-11-19 13:41:00.000001+02)\""));
		assertTrue(bindings.contains("minus=\"[2012-11-19 11:41:00.000001+02, 2012-11-19 11:41:00.000001+02)\""));
		assertTrue(bindings.contains("minus=\"[2012-11-19 14:41:00.000001+02, 2012-11-19 14:41:00.000001+02)\""));
		assertTrue(bindings.contains("minus=\"[2012-11-19 15:41:00.000001+02, 2012-11-19 15:41:00.000001+02)\""));
}
	
	@Test
	public void testStart() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT  distinct (strdf:period_start(?t1, ?t2) AS ?start) "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				//"FILTER(strdf:during(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		for(String result: bindings)
		{
			System.out.println(result.toString());
		}
		assertEquals(7, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(bindings.contains("2012-01-19T12:41:00+02"));
		assertTrue(bindings.contains("start=\"2012-11-19T12:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T10:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-01-19T10:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T13:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-01-19T11:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T14:41:00.000001+02\""));
}
	
	@Test
	public void testEnd() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT  distinct (strdf:period_end(?t1, ?t2) AS ?end) "+ 
			"WHERE { "+
				"?x1 ?y1 ?z1 ?t1 . "+
				//"FILTER(strdf:during(?t1, ?t2) && str(?x1) != str(?x2))."+
				"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(strabon.queryRewriting(query),strabon.getSailRepoConnection());
		
		for(String result: bindings)
		{
			System.out.println(result.toString());
		}
		assertEquals(7, bindings.size());
		//assertTrue(-1 < bindings.indexOf(""));
		assertTrue(bindings.contains("2012-01-19T12:41:00+02"));
		assertTrue(bindings.contains("start=\"2012-11-19T12:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-01-19T10:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T13:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-01-19T11:41:00+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T14:41:00.000001+02\""));
		assertTrue(bindings.contains("start=\"2012-11-19T15:41:00.000001+02\""));

}
}