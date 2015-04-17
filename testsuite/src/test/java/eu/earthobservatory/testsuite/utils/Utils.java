/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.utils;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.testdb.Strabon;
import eu.earthobservatory.utils.Format;
import org.apache.commons.io.FileUtils;
import org.openrdf.query.*;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * A class with useful methods for the tests.
 */
public class Utils {
  private static final String dbPropertiesFile = "/databases.properties";
  private static final String prefixesFile = File.separator + "prefixes";

  private static String databaseTemplateName = null;
  private static String serverName = null;
  private static String username = null;
  private static String password = null;
  private static String port = null;

  private static Connection conn = null;
  private static String databaseName = null;

  private static Strabon strabon = null;
  private static String dbType;

  public static void createdb() throws Exception {
    // Read properties
    readProperties();

    // Connect to server and create the temp database
    String url = buildJdbcUrl();
    conn = acquireConnection(url);

    createDatabasesIfNeeded();

    strabon =
        new Strabon(databaseName, username, password, Integer.parseInt(port), serverName, true);
  }

  private static void createDatabasesIfNeeded() throws SQLException {
    if("h2".equals(dbType)) {
//      loadSpatialExpressions();
      conn.close();
      return ;
    }

    ArrayList<String> databases = new ArrayList<String>();
    PreparedStatement pst = conn.prepareStatement("SELECT * FROM pg_catalog.pg_database");
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
  }

  private static void loadSpatialExpressions() throws SQLException {
    Scanner s = new Scanner(Utils.class.getResourceAsStream("/jaspa.sql"));
    s.useDelimiter("(;(\r)?\n)|(--\n)");
    Statement st = null;
    try
    {
      st = conn.createStatement();
      while (s.hasNext())
      {
        String line = s.next();
        if (line.startsWith("/*!") && line.endsWith("*/"))
        {
          int i = line.indexOf(' ');
          line = line.substring(i + 1, line.length() - " */".length());
        }

        if (line.trim().length() > 0)
        {
          st.execute(line);
        }
      }
    }
    finally
    {
      if (st != null) st.close();
    }
  }

  private static Connection acquireConnection(String url) throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  private static String buildJdbcUrl() {
    return JdbcUrlBuilder.forDb(dbType).host(serverName).port(port).databaseName(
        databaseName).build();
  }

  private static void readProperties() throws IOException {
    Properties properties = new Properties();
    InputStream propertiesStream = Utils.class.getResourceAsStream(dbPropertiesFile);
    properties.load(propertiesStream);

    dbType = properties.getProperty("db.type");
    databaseTemplateName = properties.getProperty(dbType +".databaseTemplateName");
    serverName = properties.getProperty(dbType +".serverName");
    username = properties.getProperty(dbType +".username");
    password = properties.getProperty(dbType +".password");
    port = properties.getProperty(dbType +".port");
    databaseName = properties.getProperty(dbType+".databaseName");
  }

  public static void storeDataset(String datasetFile, Boolean inference) throws RDFParseException,
      RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault {
    if (datasetFile.endsWith(".nt"))
      strabon.storeInRepo(datasetFile, "NTRIPLES", inference);
    else if (datasetFile.endsWith(".nq"))
      strabon.storeInRepo(datasetFile, "NQUADS", inference);
  }


  public static void testQuery(String queryFile, String resultsFile, boolean orderOn)
      throws IOException, MalformedQueryException, QueryEvaluationException,
      TupleQueryResultHandlerException, URISyntaxException, QueryResultParseException,
      UnsupportedQueryResultFormatException {
    ByteArrayOutputStream resultsStream = new ByteArrayOutputStream();
    String query =
        FileUtils.readFileToString(new File(Utils.class.getResource(prefixesFile).toURI())) + "\n"
            + FileUtils.readFileToString(new File(Utils.class.getResource(queryFile).toURI()));

    // Pose the query
    strabon.query(query, Format.XML, strabon.getSailRepoConnection(), resultsStream);

    // Check if the results of the query are the expected
    compareResults(queryFile, orderOn, QueryResultIO.parse(
        Utils.class.getResourceAsStream(resultsFile), TupleQueryResultFormat.SPARQL),
        QueryResultIO.parse((new ByteArrayInputStream(resultsStream.toByteArray())),
            TupleQueryResultFormat.SPARQL));
  }

  protected static void compareResults(String queryFile, boolean orderOn,
      TupleQueryResult expectedResults, TupleQueryResult actualResults)
      throws QueryEvaluationException {

    List<String> eBindingNames = expectedResults.getBindingNames();
    List<String> aBindingNames = actualResults.getBindingNames();

    assertTrue("Results are not the expected. QueryFile: " + queryFile,
        aBindingNames.containsAll(aBindingNames) && eBindingNames.containsAll(aBindingNames));

    // Sort each binding's values
    List<String> eBindingList = new ArrayList<String>();
    List<String> aBindingList = new ArrayList<String>();

    while (expectedResults.hasNext() && actualResults.hasNext()) {
      BindingSet eBinding = expectedResults.next();
      BindingSet aBinding = actualResults.next();

      String eBindingValues = "";
      String aBindingValues = "";
      for (String bindingName : eBindingNames) {
        eBindingValues += eBinding.getValue(bindingName).stringValue();
        aBindingValues += aBinding.getValue(bindingName).stringValue();
      }

      eBindingList.add(eBindingValues);
      aBindingList.add(aBindingValues);
    }

    assertFalse("Results are not the expected. QueryFile: " + queryFile, expectedResults.hasNext()
        || actualResults.hasNext());

    if (!orderOn) {
      // Sort bindings alphabetically
      Collections.sort(eBindingList);
      Collections.sort(aBindingList);
    }
    // Check bindings one by one
    Iterator<String> eBindingListIterator = eBindingList.iterator();
    Iterator<String> aBindingListIterator = aBindingList.iterator();

    while (eBindingListIterator.hasNext() && aBindingListIterator.hasNext()) {
      assertEquals("Results are not the expected. QueryFile: " + queryFile,
          eBindingListIterator.next(), aBindingListIterator.next());
    }

    actualResults.close();
    expectedResults.close();
  }

  public static void dropdb() throws SQLException {
    strabon.close();

    // Drop the temp database
    conn.close();
    String url = buildJdbcUrl();
    conn = acquireConnection(url);

    PreparedStatement pst = conn.prepareStatement("DROP DATABASE " + databaseName);
    pst.executeUpdate();
    pst.close();
    conn.close();
  }
}
