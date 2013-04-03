/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgis;

import static org.junit.Assert.assertTrue;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import eu.earthobservatory.runtime.postgis.Strabon;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr
 */
public class TestStore{
	
	private static Strabon strabon;
	
	private static final String datasetFile = "/TestStore.nt";
	private static final String queryFile = "/TestStore.rq";
	private static final String resultsFile = "/TestStore.sr";

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		strabon = TemplateTests.beforeClass(datasetFile);
	}
	
	@Test
	public void test() throws IOException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException
	{
		BufferedReader queryReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(queryFile)));
		BufferedReader resultsReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resultsFile)));
		String query="";
		
		while (queryReader.ready()) 
		{
			query+=queryReader.readLine()+"\n";
		}

		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		Iterator<String> iterator = bindings.iterator();
		while(iterator.hasNext() && resultsReader.ready())
		{
			String binding = iterator.next();
			//System.out.println(binding);
			binding=binding.replaceAll("[[A-Z][a-z][0-9]]*=", "?=");
			assertTrue(resultsReader.readLine().equals(binding));
			//System.out.println(binding);
		}

	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
}
