/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.monetdb;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.Strabon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * A set of simple tests on SPARQL query functionality
 */
public class TemplateTests extends eu.earthobservatory.runtime.generaldb.SimpleTests {

  @BeforeClass
  public static Strabon beforeClass(String inputfile) throws Exception {
    // Read properties
    Properties properties = new Properties();
    InputStream propertiesStream = TemplateTests.class.getResourceAsStream("/databases.properties");
    properties.load(propertiesStream);

    serverName = properties.getProperty("monetdb.serverName");
    databaseName = properties.getProperty("monetdb.databaseName");
    port = Integer.parseInt(properties.getProperty("monetdb.port"));
    username = properties.getProperty("monetdb.username");
    password = properties.getProperty("monetdb.password");

    // Connect to database
    Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
    String url = "jdbc:monetdb://" + serverName + ":" + port + "/" + databaseName;
    conn = DriverManager.getConnection(url, username, password);

    // // Clean database
    Statement stmt = conn.createStatement();
    ResultSet results =
        stmt.executeQuery("SELECT name FROM tables WHERE system=false AND name <> 'locked'");
    while (results.next()) {
      String table_name = results.getString("name");
      Statement stmt2 = conn.createStatement();
      stmt2.executeUpdate("DROP TABLE \"" + table_name + "\"");
      stmt2.close();
    }
    stmt.close();

    Strabon strabon =
        new eu.earthobservatory.runtime.monetdb.Strabon(databaseName, username, password, port,
            serverName, true);

    TemplateTests.loadTestData(inputfile, strabon);

    // This is a workaround for http://bug.strabon.di.uoa.gr/ticket/1
    strabon.close();
    strabon =
        new eu.earthobservatory.runtime.monetdb.Strabon(databaseName, username, password, port,
            serverName, true);
    //

    return strabon;
  }

  @AfterClass
  public static void afterClass(Strabon strabon) throws SQLException {
    strabon.close();
  }

  protected static void loadTestData(String inputfile, Strabon strabon) throws RDFParseException,
      RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault {
    strabon.storeInRepo(inputfile, "NTRIPLES", false);
  }

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
// */
// @After
// public void after()
// throws Exception
// {
// // Clean database
// Statement stmt = conn.createStatement();
// ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE " +
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

