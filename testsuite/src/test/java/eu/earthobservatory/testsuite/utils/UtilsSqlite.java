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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.sqlite.SQLiteConfig;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.sqlite.Strabon;
import eu.earthobservatory.utils.Format;

/**
 * A class with useful methods for the tests.
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 */
public class UtilsSqlite
{
	private static final String dbPropertiesFile=File.separator+"databases.properties";
	private static final String prefixesFile=File.separator+"prefixes";
	
	private static String databaseTemplateName = null;
	private static String serverName = null;
	private static String pcre = null;
	private static String libspatial = null;
	
	private static Connection conn = null;
	private static String databaseName = null;
	
	private static Strabon strabon = null;
	
	public static void createdb() throws Exception
	{
		String url="";
		ArrayList<String> databases=new ArrayList<String>();
        PreparedStatement pst = null;
        
        Properties properties = new Properties();
		InputStream propertiesStream =  Utils.class.getResourceAsStream(dbPropertiesFile);
		properties.load(propertiesStream);

		if((pcre = System.getProperty("sqlite.pcre"))==null)
		{
			pcre = properties.getProperty("sqlite.pcre");
		}
		
		if((libspatial = System.getProperty("sqlite.libspatialite"))==null)
		{
			libspatial = properties.getProperty("sqlite.libspatialite");
		}
				
		//Connect to server and create the temp database
        databaseName ="/tmp/"+ (int)(Math.random()*10000)+".db" ;
        System.out.println(databaseName);
        
        //TODO check if the database db already exists
        //while(databases.contains(db))
        //{
        //	db+="0";
        //}
     
		strabon = new eu.earthobservatory.runtime.sqlite.Strabon(databaseName, libspatial, pcre, false);
	}
	
	public static void storeDataset(String datasetFile, Boolean inference) throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
	    if(datasetFile.endsWith(".nt"))
	    	strabon.storeInRepo(datasetFile, "NTRIPLES", inference);
	    else if(datasetFile.endsWith(".nq"))
	    	strabon.storeInRepo(datasetFile, "NQUADS", inference);
	}
	
	
	public static void testQuery(String queryFile, String resultsFile, boolean orderOn) throws IOException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, URISyntaxException, QueryResultParseException, UnsupportedQueryResultFormatException
	{
		ByteArrayOutputStream resultsStream = new ByteArrayOutputStream();
		String query = FileUtils.readFileToString(new File(UtilsSqlite.class.getResource(prefixesFile).toURI()))+"\n"+FileUtils.readFileToString(new File(UtilsSqlite.class.getResource(queryFile).toURI()));
		
		//Pose the query
		strabon.query(query, Format.XML, strabon.getSailRepoConnection(), resultsStream);
		
		//Check if the results of the query are the expected
		compareResults(queryFile, orderOn, 
					   QueryResultIO.parse(UtilsSqlite.class.getResourceAsStream(resultsFile), TupleQueryResultFormat.SPARQL),
					   QueryResultIO.parse((new ByteArrayInputStream(resultsStream.toByteArray())), TupleQueryResultFormat.SPARQL));
	}
	
	protected static void compareResults(String queryFile, boolean orderOn, 
															 TupleQueryResult expectedResults, 
															 TupleQueryResult actualResults) throws QueryEvaluationException {
		
		List<String> eBindingNames = expectedResults.getBindingNames();
		List<String> aBindingNames = actualResults.getBindingNames();
		
		assertTrue("Results are not the expected. QueryFile: " + queryFile, aBindingNames.containsAll(aBindingNames) && eBindingNames.containsAll(aBindingNames));		
		
		//Sort each binding's values
		List<String> eBindingList = new ArrayList<String>();
		List<String> aBindingList = new ArrayList<String>();

		while(expectedResults.hasNext() && actualResults.hasNext())
		{
			BindingSet eBinding = expectedResults.next();
			BindingSet aBinding = actualResults.next();
			
			String eBindingValues="";
			String aBindingValues="";
			for(String bindingName : eBindingNames)
			{
				eBindingValues+=eBinding.getValue(bindingName).stringValue();
				aBindingValues+=aBinding.getValue(bindingName).stringValue();
			}
			
			eBindingList.add(eBindingValues);
			aBindingList.add(aBindingValues);
		}
		
		assertFalse("Results are not the expected. QueryFile: "+queryFile, expectedResults.hasNext() || actualResults.hasNext());
		
		if(!orderOn)
		{
			//Sort bindings alphabetically
			Collections.sort(eBindingList);
			Collections.sort(aBindingList);
		}
		//Check bindings one by one
		Iterator<String> eBindingListIterator = eBindingList.iterator();
		Iterator<String> aBindingListIterator = aBindingList.iterator();

		while(eBindingListIterator.hasNext() && aBindingListIterator.hasNext())
		{
			assertEquals("Results are not the expected. QueryFile: "+queryFile,eBindingListIterator.next(), aBindingListIterator.next() );
		}
		
		actualResults.close();
		expectedResults.close();
	}
	
	public static void dropdb() throws SQLException
	{
		strabon.close();
		
		//Drop the temp database
		//delete the database file
		/*File file = new File(databaseName);
 
    	if(file.delete()){
    		System.out.println(file.getName() + " is deleted!");
    	}else{
    		System.out.println("Delete operation is failed.");
    	}*/
		conn.close();
	}
}
