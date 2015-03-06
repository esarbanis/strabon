/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgis;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.SimpleTests;
import eu.earthobservatory.runtime.generaldb.Strabon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.assertNull;

/**
 * A set of simple tests on SPARQL query functionality
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

  @BeforeClass
  public static Strabon beforeClass(String inputFile) throws Exception {
    String url = "";
    ArrayList<String> databases = new ArrayList<String>();
    PreparedStatement pst = null;

    // Read properties
    Properties properties = new Properties();
    InputStream propertiesStream = SimpleTests.class.getResourceAsStream("/databases.properties");
    properties.load(propertiesStream);

    databaseTemplateName = properties.getProperty("postgis.databaseTemplateName");;
    defaultUser = properties.getProperty("postgis.defaultUser");
    serverName = properties.getProperty("postgis.serverName");
    username = properties.getProperty("postgis.username");
    password = properties.getProperty("postgis.password");
    port = Integer.parseInt(properties.getProperty("postgis.port"));

    // Connect to server and create the temp database
    url = "jdbc:postgresql://" + serverName + ":" + port + "/" + defaultUser;
    conn = DriverManager.getConnection(url, username, password);
    assertNull(conn.getWarnings());

    pst = conn.prepareStatement("SELECT * FROM pg_catalog.pg_database");
    ResultSet rs = pst.executeQuery();

    while (rs.next()) {
      databases.add(rs.getString(1));
    }
    rs.close();
    pst.close();

    databaseName = "teststrabon" + (int) (Math.random() * 10000);
    while (databases.contains(databaseName)) {
      databaseName += "0";
    }


    pst =
        conn.prepareStatement("CREATE DATABASE " + databaseName + " TEMPLATE "
            + databaseTemplateName);
    pst.executeUpdate();
    pst.close();
    conn.close();

    url = "jdbc:postgresql://" + serverName + ":" + port + "/" + databaseName;
    conn = DriverManager.getConnection(url, username, password);
    assertNull(conn.getWarnings());

    Strabon strabon =
        new eu.earthobservatory.runtime.postgis.Strabon(databaseName, username, password, port,
            serverName, true);

    loadTestData(inputFile, strabon);

    return strabon;
  }


  @AfterClass
  public static void afterClass(Strabon strabon) throws SQLException {
    strabon.close();

    // Drop the temp database
    conn.close();
    String url = "jdbc:postgresql://" + serverName + ":" + port + "/" + defaultUser;
    conn = DriverManager.getConnection(url, username, password);
    assertNull(conn.getWarnings());

    PreparedStatement pst = conn.prepareStatement("DROP DATABASE " + databaseName);
    pst.executeUpdate();
    pst.close();
    conn.close();
  }

  protected static void loadTestData(String inputfile, Strabon strabon) throws RDFParseException,
      RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault {
    strabon.storeInRepo(inputfile, "NTRIPLES", false);
  }


  // Clean database
  // Statement stmt = conn.createStatement();
  // ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE "
  // +
  // "table_schema='public' AND table_name <> 'spatial_ref_sys' " +
  // "AND table_name <> 'geometry_columns' AND table_name <> 'geography_columns' " +
  // "AND table_name <> 'raster_columns' AND table_name <> 'raster_overviews' " +
  // "AND table_name <> 'locked'"
  // );
  // while (results.next()) {
  // String table_name = results.getString("table_name");
  // Statement stmt2 = conn.createStatement();
  // stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
  // stmt2.close();
  // }
  // stmt.close();

  // /**
  // * @throws java.lang.Exception
  // */
  // @Before
  // public void before()
  // throws Exception
  // {
  //
  // }
  //
  // /**
  // * @throws java.lang.Exception
  // */
  // @After
  // public void after()
  // throws Exception
  // {
  // // Clean database
  // Statement stmt = conn.createStatement();
  // ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE "
  // +
  // "table_schema='public' and table_name <> 'spatial_ref_sys' " +
  // "and table_name <> 'geometry_columns' and " +
  // "table_name <> 'geography_columns' and table_name <> 'locked'");
  // while (results.next()) {
  // String table_name = results.getString("table_name");
  // Statement stmt2 = conn.createStatement();
  // stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
  // stmt2.close();
  // }
  //
  // stmt.close();
  // }
}
