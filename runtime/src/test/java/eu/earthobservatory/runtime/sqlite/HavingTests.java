/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.sqlite;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;


public class HavingTests extends eu.earthobservatory.runtime.generaldb.HavingTests {
	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		strabon = TemplateTests.beforeClass("/having-tests-srid.nt");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}

}
