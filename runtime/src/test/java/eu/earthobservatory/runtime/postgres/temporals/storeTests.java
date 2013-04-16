/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */


package eu.earthobservatory.runtime.postgres.temporals;


import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
//import org.openrdf.query.resultio.Format;
import eu.earthobservatory.utils.Format;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultWriterFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;


import eu.earthobservatory.runtime.generaldb.*;
import eu.earthobservatory.runtime.postgres.temporals.TemplateTests;


/**
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 */
public class storeTests {

	protected static Strabon strabon;
   //using a new database to to the stores	
	private static final String database = "store-test";
	public static java.sql.Connection conn = null;
	public static String jdbcDriver = null;
	public static String serverName = null;
	public static String username = null;
	public static String password = null;
	public static Integer port = null;
	
	
	
	
	protected static final String prefixes = 
		"PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n" +
		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n";

	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		// Read properties
					Properties properties = new Properties();
					InputStream propertiesStream =  SimpleTests.class.getResourceAsStream("/databases.properties");
					properties.load(propertiesStream);

					serverName = properties.getProperty("temporal.postgis.serverName");
					port = Integer.parseInt(properties.getProperty("temporal.postgis.port"));
					username = properties.getProperty("temporal.postgis.username");
					password = properties.getProperty("temporal.postgis.password");
		strabon = new eu.earthobservatory.runtime.postgis.Strabon(database, username, password, port, serverName, true);
		
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
	}
	
	
	
	@Test
	public void testStoreString() 
	{
		String text1 = "<http://example.org/itemOfString1> <http://example.org/id> \"String111\" \"[2005-11-19T12:41:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period> . \n" +
				"<http://example.org/itemOfString2> <http://example.org/id> \"String112\" \"[2005-11-19T12:42:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period>.";
		
		String text2 = "<http://example.org/itemOfString1> <http://example.org/id> \"String121\" \"[2002-11-19T12:41:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period> . \n" +
				"<http://example.org/itemOfString22> <http://example.org/id> \"String122\" \"[2002-11-19T12:42:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period>.";
		try {
			strabon.storeInRepo(text1, "NQUADS");
			strabon.storeInRepo(text2, "NQUADS");

		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasetFormatFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testStoreURL() throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault 
	{
		strabon.storeInRepo("http://manolee.di.uoa.gr/temporals/temporal-periods.nq", "NQUADS");

	}
	
	@Test
	public void testStoreFile() throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault 
	{
		strabon.storeInRepo("/temporal-periods.nq", "NQUADS");

	}
	
	@Test
	public void testStoreNQUADString() 
	{
		String text1 = "<http://example.org/itemOfString1> <http://example.org/id> \"String111\" \"[2005-11-19T12:41:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period> . \n" +
				"<http://example.org/itemOfString2> <http://example.org/id> \"String112\" \"[2005-11-19T12:42:00,2010-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period>.";
		
		String text2 = "<http://example.org/itemOfString31> <http://example.org/id> \"String3121\"  . \n" +
				"<http://example.org/itemOfString322> <http://example.org/id> \"String3122\" .";
		try {
			strabon.storeInRepo(text1, "NQUADS");
			strabon.storeInRepo(text2, "NQUADS");

		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasetFormatFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testStoreUCString() 
	{
		String text1 = "<http://example.org/itemOfString1> <http://example.org/id> \"String111\" \"[2005-11-19T12:41:00,2009-11-19T13:41:00]\"^^<http://strdf.di.uoa.gr/ontology#period> . \n" +
				"<http://example.org/itemOfString2> <http://example.org/id> \"String112\" \"[2010-11-19T12:42:00,UC]\"^^<http://strdf.di.uoa.gr/ontology#period>.";
		
		
		try {
			strabon.storeInRepo(text1, "NQUADS");

		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasetFormatFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testNQUADSFile() throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault 
	{
		strabon.storeInRepo("/triples-and-quads.nq", "NQUADS");

	}
	
}