/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.monetdb;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class AggregateTests extends eu.earthobservatory.runtime.generaldb.AggregateTests {

	@BeforeClass
	public static void beforeClass(String inputfile) throws Exception
	{
		 strabon = TemplateTests.beforeClass("/aggregate-tests-srid.nt");

	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
	

}
