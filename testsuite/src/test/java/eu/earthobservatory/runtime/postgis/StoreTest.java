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
 * A simple store test. It tests if the input dataset is stored and retrieved correctly.
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr
 */
public class StoreTest
{	
	private final String datasetFile="/"+this.getClass().getSimpleName()+".nt";
	private final String queryFile="/"+this.getClass().getSimpleName()+".rq";
	private final String resultsFile="/"+this.getClass().getSimpleName()+".sr";

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
