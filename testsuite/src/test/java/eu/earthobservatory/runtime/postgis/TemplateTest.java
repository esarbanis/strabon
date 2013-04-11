/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A template test. It: 
 * 1) creates a database
 * 2) stores a dataset
 * 3) poses a query
 * 4) checks if the results of the query are the expected 
 * 5) drops the database
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr
 */
public abstract class TemplateTest
{	
	private String datasetFile;
	private String queryFile;
	private String resultsFile;

	public TemplateTest(String datasetFile, String queryFile, String resultsFile)
	{
		this.datasetFile = datasetFile;
		this.queryFile = queryFile;
		this.resultsFile = resultsFile;
	}

	public TemplateTest()
	{
		String testname=this.getClass().getSimpleName();
		
		this.datasetFile="/"+testname+"/"+testname+".nt";
		this.queryFile="/"+testname+"/"+testname+".rq";
		this.resultsFile="/"+testname+"/"+testname+".sr";
	}

	@Before
	public void before() throws Exception
	{
		Utils.createdb();
		Utils.storeDataset(datasetFile);
	}
	
	@Test
	public void test() throws Exception
	{
		Utils.testQuery(queryFile, resultsFile);
	}
	
	@After
	public void after() throws Exception
	{
		Utils.dropdb();
	}
}
