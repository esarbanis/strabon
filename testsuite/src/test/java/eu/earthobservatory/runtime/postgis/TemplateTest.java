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

import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.postgis.Strabon;

/**
 * A template class with useful methods for the tests.
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class TemplateTest
{	
	public static String databaseTemplateName = null;
	public static String defaultUser = null;
	public static String serverName = null;
	public static String username = null;
	public static String password = null;
	public static Integer port = null;
	
	public static Connection conn = null;
	public static String databaseName = null;
	
	private static Strabon strabon = null;
	
	public static void createdb() throws Exception
	{
		String url="";
		ArrayList<String> databases=new ArrayList<String>();
        PreparedStatement pst = null;
		
		//Read properties
		Properties properties = new Properties();
		InputStream propertiesStream =  TemplateTest.class.getResourceAsStream("/databases.properties");
		properties.load(propertiesStream);

		databaseTemplateName = properties.getProperty("postgis.databaseTemplateName");
		defaultUser = properties.getProperty("postgis.defaultUser");
		serverName = properties.getProperty("postgis.serverName");
		username = properties.getProperty("postgis.username");
		password = properties.getProperty("postgis.password");
		port = Integer.parseInt(properties.getProperty("postgis.port"));

		//Connect to server and create the temp database
		url = "jdbc:postgresql://"+serverName+":"+port+"/"+defaultUser;
		conn = DriverManager.getConnection(url, username, password);
		
        pst = conn.prepareStatement("SELECT * FROM pg_catalog.pg_database");
        ResultSet rs = pst.executeQuery();

        while (rs.next())
        {
        	databases.add(rs.getString(1));
        }
        rs.close();
        pst.close();
   
        databaseName="teststrabon"+(int)(Math.random()*10000);
        while(databases.contains(databaseName))
        {
        	databaseName+="0";
        }
    
		pst = conn.prepareStatement("CREATE DATABASE "+databaseName+" TEMPLATE " + databaseTemplateName);
		pst.executeUpdate();
		pst.close();
		conn.close();

		url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		conn = DriverManager.getConnection(url, username, password);
		
	    strabon = new Strabon(databaseName, username, password, port, serverName, true);
	}
	
	public static void storeDataset(String datasetFile) throws RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
	    if(datasetFile.endsWith(".nt"))
	    	strabon.storeInRepo(datasetFile, "NTRIPLES");
	    else if(datasetFile.endsWith(".nq"))
	    	strabon.storeInRepo(datasetFile, "NQUADS");
	}
	
	public static void testQuery(String queryFile, String resultsFile) throws IOException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException
	{
		BufferedReader queryReader = new BufferedReader(new InputStreamReader(TemplateTest.class.getResourceAsStream(queryFile)));
		BufferedReader resultsReader = new BufferedReader(new InputStreamReader(TemplateTest.class.getResourceAsStream(resultsFile)));
		String query="";
		ArrayList<String> actualResults = new ArrayList<String>();
		ArrayList<String> expectedResults = new ArrayList<String>();
		
		while (queryReader.ready())
		{
			query+=queryReader.readLine()+"\n";
		}
		actualResults = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		while (resultsReader.ready())
		{
			expectedResults.add(resultsReader.readLine());
		}
		
		//Check if the actual result set is the same as the expected one
		assertEquals("Actual result set is not the same as the expected one.", expectedResults.size(), actualResults.size());
		Iterator<String> expectedResultsIterator = expectedResults.iterator();
		Iterator<String> actualResultsIterator = actualResults.iterator();
		
		while(expectedResultsIterator.hasNext() && actualResultsIterator.hasNext())
		{
			String eResults = expectedResultsIterator.next();
			String aResults = actualResultsIterator.next();	
			
			//Replace all the names of the variables with "?"
			aResults = aResults.replaceAll("[[A-Z][a-z][0-9]]*=", "?=");
			assertEquals("Actual result set is not the same as the expected one.", aResults, eResults);
		}
	}
	
	public static void dropdb() throws SQLException
	{
		strabon.close();
		
		//Drop the temp database
		conn.close();
		String url = "jdbc:postgresql://"+serverName+":"+port+"/"+defaultUser;
		conn = DriverManager.getConnection(url, username, password);
		
		PreparedStatement pst = conn.prepareStatement("DROP DATABASE "+databaseName);
		pst.executeUpdate();
		pst.close();
		conn.close();
	}
}
