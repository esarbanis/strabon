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
import java.util.ArrayList;

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
 * A set of simple tests on temporal selection functionality 
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class TemporalSelectionTests {

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
	public void testSPOTQuery() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT * "+ 
			"WHERE { "+
				"?s ?p ?o ?t. "+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(utils.queryRewriting(query),strabon.getSailRepoConnection());
		assertEquals(8, bindings.size());
	}


	@Test
	public void testSPOQuery() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, QueryEvaluationException
	{
	
		String query = 
			prefixes+
			"SELECT * "+ 
			"WHERE { "+
				"?s ?p ?o. "+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(utils.queryRewriting(query),strabon.getSailRepoConnection());
		assertEquals(9, bindings.size());
	}
}
