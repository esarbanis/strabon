/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.monetdb;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A set of simple tests on SPARQL query functionality
 */
public class GeneralTests extends eu.earthobservatory.runtime.generaldb.GeneralTests {

  @BeforeClass
  public static void beforeClass() throws Exception {
    strabon = TemplateTests.beforeClass("/more-tests.nt");
  }

  @AfterClass
  public static void afterClass() throws SQLException {
    TemplateTests.afterClass(strabon);
  }

  @Test
  public void testQuerySpatialProperties() throws MalformedQueryException,
      QueryEvaluationException, TupleQueryResultHandlerException, IOException {
    strabon.query(querySpatialPropertiesMonetDB, strabon.getSailRepoConnection());

  }

  @Test
  public void testQuerySpatialPropertiesConst() throws MalformedQueryException,
      QueryEvaluationException, TupleQueryResultHandlerException, IOException {
    strabon.query(querySpatialPropertiesConstMonetDB, strabon.getSailRepoConnection());

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
