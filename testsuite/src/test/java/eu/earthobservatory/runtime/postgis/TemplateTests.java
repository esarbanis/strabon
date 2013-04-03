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

import static org.junit.Assert.assertNull;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import eu.earthobservatory.runtime.postgis.Strabon;

/**
 * A template class with useful methods for the tests.  
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class TemplateTests {
	
	public static String databaseTemplateName = null;
	public static String defaultUser = null;
	public static String serverName = null;
	public static String username = null;
	public static String password = null;
	public static Integer port = null;
	
	public static Connection conn = null;
	public static String databaseName = null;
	
	public static Strabon beforeClass(String datasetFile) throws Exception
	{
		String url="";
		ArrayList<String> databases=new ArrayList<String>();
        PreparedStatement pst = null;
		
		// Read properties
		Properties properties = new Properties();
		InputStream propertiesStream =  TemplateTests.class.getResourceAsStream("/databases.properties");
		properties.load(propertiesStream);

		databaseTemplateName = properties.getProperty("postgis.databaseTemplateName");;
		defaultUser = properties.getProperty("postgis.defaultUser");
		serverName = properties.getProperty("postgis.serverName");
		username = properties.getProperty("postgis.username");
		password = properties.getProperty("postgis.password");
		port = Integer.parseInt(properties.getProperty("postgis.port"));

		//Connect to server and create the temp database
		url = "jdbc:postgresql://"+serverName+":"+port+"/"+defaultUser;
		conn = DriverManager.getConnection(url, username, password);
		assertNull(conn.getWarnings());
		
        pst = conn.prepareStatement("SELECT * FROM pg_catalog.pg_database");
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
        	databases.add(rs.getString(1));
        }
        rs.close();
        pst.close();
   
        databaseName="teststrabon"+(int)(Math.random()*10000);
        while(databases.contains(databaseName)){
        	databaseName+="0";
        }
        	
        
		pst = conn.prepareStatement("CREATE DATABASE "+databaseName+" TEMPLATE " + databaseTemplateName);
		pst.executeUpdate();
		pst.close();
		conn.close();

		url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		conn = DriverManager.getConnection(url, username, password);
		assertNull(conn.getWarnings());
		
	    Strabon strabon = new eu.earthobservatory.runtime.postgis.Strabon(databaseName, username, password, port, serverName, true);

	    if(datasetFile.endsWith(".nt"))
	    	strabon.storeInRepo(datasetFile, "NTRIPLES");
	    else if(datasetFile.endsWith(".nq"))
	    	strabon.storeInRepo(datasetFile, "NQUADS");
	    
		return strabon;
	}
	

	public static void afterClass(Strabon strabon) throws SQLException
	{
		strabon.close();
		
		//Drop the temp database
		conn.close();
		String url = "jdbc:postgresql://"+serverName+":"+port+"/"+defaultUser;
		conn = DriverManager.getConnection(url, username, password);
		assertNull(conn.getWarnings());
		
		PreparedStatement pst = conn.prepareStatement("DROP DATABASE "+databaseName);
		pst.executeUpdate();
		pst.close();
		conn.close();
	}
}
