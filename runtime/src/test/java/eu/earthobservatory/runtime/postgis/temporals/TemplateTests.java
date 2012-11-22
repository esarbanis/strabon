/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgis.temporals;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.SimpleTests;
import eu.earthobservatory.runtime.generaldb.Strabon;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class TemplateTests {
	
	public static java.sql.Connection conn = null;
	public static String databaseName = null;
	public static String jdbcDriver = null;
	public static String serverName = null;
	public static String username = null;
	public static String password = null;
	public static Integer port = null;
	
	@BeforeClass
	public static Strabon beforeClass(String inputFile, String format)
		throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		// Read properties
		Properties properties = new Properties();
		InputStream propertiesStream =  SimpleTests.class.getResourceAsStream("/databases.properties");
		properties.load(propertiesStream);

		serverName = properties.getProperty("temporal.postgis.serverName");
		databaseName = properties.getProperty("temporal.postgis.databaseName");
		port = Integer.parseInt(properties.getProperty("temporal.postgis.port"));
		username = properties.getProperty("temporal.postgis.username");
		password = properties.getProperty("temporal.postgis.password");
		
		// Connect to database
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		conn = DriverManager.getConnection(url, username, password);
				
//		// Clean database
		Statement stmt = conn.createStatement();
		ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE " +
						"table_schema='public' AND table_name <> 'spatial_ref_sys' " +
						"AND table_name <> 'geometry_columns' AND " +
						"table_name <> 'geography_columns' AND table_name <> 'locked'");
		while (results.next()) {
			String table_name = results.getString("table_name");
			Statement stmt2 = conn.createStatement();
			stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
			stmt2.close();
		}
		stmt.close();
		
	    Strabon strabon = new eu.earthobservatory.runtime.postgis.Strabon(databaseName, username, password, port, serverName, true);
		
		loadTestData(inputFile, strabon, format);
		
		return strabon;
	}
	
	@AfterClass
	public static void afterClass(Strabon strabon) throws SQLException
	{
		strabon.close();
	}
	
	protected static void loadTestData(String inputfile, Strabon strabon, String format)
		throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		strabon.storeInRepo(inputfile, format);
	}
}
