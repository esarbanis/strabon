/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

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
	private ArrayList<String> queryFile;
	private ArrayList<String> resultsFile;

	public TemplateTest(String datasetFile, ArrayList<String> queryFile, ArrayList<String> resultsFile)
	{
		this.datasetFile = datasetFile;
		this.queryFile = queryFile;
		this.resultsFile = resultsFile;
	}

	public TemplateTest()
	{
		queryFile=new ArrayList<String>();
		resultsFile=new ArrayList<String>();
		
		String testname=this.getClass().getSimpleName();
		String testpackage=this.getClass().getPackage().getName().substring(this.getClass().getPackage().getName().lastIndexOf('.')+1);
		File testfolder = new File(this.getClass().getResource("/"+testpackage+"/"+testname+"/").getPath());
		
		String[] files = testfolder.list();
		
		for(String file : files)
		{
			if(file.endsWith(".nt") || file.endsWith(".nq"))
			{
				this.datasetFile="/"+testpackage+"/"+testname+"/"+file;
			}
			else if(file.endsWith(".rq"))
			{
				this.queryFile.add("/"+testpackage+"/"+testname+"/"+file);
				this.resultsFile.add("/"+testpackage+"/"+testname+"/"+file.substring(0, file.length()-3)+".srx");
			}
		}
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
		Iterator<String> queryFileIterator = this.queryFile.iterator();
		Iterator<String> resultsFileIterator = this.resultsFile.iterator();
		
		while(queryFileIterator.hasNext() && resultsFileIterator.hasNext())
		{
			Utils.testQuery(queryFileIterator.next(), resultsFileIterator.next());
		}
	}
	
	@After
	public void after() throws Exception
	{
		Utils.dropdb();
	}
}
