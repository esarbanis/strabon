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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A simple store test. It tests if the input dataset is stored and retrieved correctly.
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr
 */
public class TestStore
{	
	private static final String datasetFile = "/TestStore.nt";
	private static final String queryFile = "/TestStore.rq";
	private static final String resultsFile = "/TestStore.sr";

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		TemplateTest.createdb();
		TemplateTest.storeDataset(datasetFile);
	}
	
	@Test
	public void test() throws Exception
	{
		TemplateTest.testQuery(queryFile, resultsFile);
	}
	
	@AfterClass
	public static void afterClass() throws Exception
	{
		TemplateTest.dropdb();
	}
}
