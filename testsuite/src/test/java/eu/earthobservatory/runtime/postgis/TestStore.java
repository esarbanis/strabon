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

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.Strabon;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr
 */
public class TestStore{
	
	private static Strabon strabon;


	@BeforeClass
	public static void beforeClass() throws Exception
	{
		strabon = TemplateTests.beforeClass();
	}
	
	@Test
	public void test() throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		strabon.storeInRepo("/"+this.getClass().getSimpleName()+".nt", "NTRIPLES");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
}
