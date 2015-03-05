/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.IOException;

public class HavingTests {
  public static Strabon strabon;


  protected static String jdbcDriver = "org.postgresql.Driver";
  protected static String serverName = "localhost";
  protected static String username = "postgres";
  protected static String password = "postgres";
  protected static Integer port = 5432;
  protected static java.sql.Connection conn = null;
  protected static String databaseName = null;

  // @BeforeClass
  // public abstract static void initialize() throws SQLException, ClassNotFoundException
  // {
  // strabon = new Strabon("cco2","postgres","p1r3as", 5432, "localhost", true);
  // }



  String prefixes = " PREFIX noa:<http://www.earthobservatory.eu/ontologies/noaOntology.owl#>"
      + " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
      + " PREFIX strdf:<http://strdf.di.uoa.gr/ontology#> ";



  String query1Having = prefixes + "SELECT ?burntArea ?baGeo " + "WHERE { "
      + "?burntArea a noa:BurntArea; " + "strdf:hasGeometry ?baGeo. "
      + "?urbanArea a noa:UrbanArea; " + "strdf:hasGeometry ?uaGeo; "
      + "FILTER(strdf:mbbIntersects(?baGeo,?uaGeo)). " + "} " + "GROUP BY ?burntArea ?baGeo "
      + "HAVING (strdf:area(strdf:union(?uaGeo)) > 8) ";

  String query2HavingA = prefixes + "SELECT ?burntArea ?baGeo (strdf:extent(?uaGeo) AS ?totalMBB) "
      + "WHERE { " + "?burntArea a noa:BurntArea; " + "strdf:hasGeometry ?baGeo. "
      + "?urbanArea a noa:UrbanArea; " + "strdf:hasGeometry ?uaGeo; "
      + "FILTER(strdf:mbbIntersects(?baGeo,?uaGeo)). " + "} " + "GROUP BY ?burntArea ?baGeo "
      + "HAVING (strdf:area(strdf:extent(?uaGeo)) > 8) ";

  String query2HavingB =
      prefixes
          + "SELECT ?burntArea ?baGeo (strdf:difference(strdf:extent(?uaGeo),strdf:union(?uaGeo)) AS ?MBBdiff) "
          + "WHERE { " + "?burntArea a noa:BurntArea; " + "strdf:hasGeometry ?baGeo. "
          + "?urbanArea a noa:UrbanArea; " + "strdf:hasGeometry ?uaGeo; "
          + "FILTER(strdf:mbbIntersects(?baGeo,?uaGeo)). " + "} " + "GROUP BY ?burntArea ?baGeo "
          + "HAVING (strdf:area(strdf:extent(?uaGeo)) > 8) ";

  String query2HavingC = prefixes + "SELECT ?burntArea (strdf:extent(?uaGeo) AS ?totalMBB) "
      + "WHERE { " + "?burntArea a noa:BurntArea; " + "strdf:hasGeometry ?baGeo. "
      + "?urbanArea a noa:UrbanArea; " + "strdf:hasGeometry ?uaGeo; "
      + "FILTER(strdf:mbbIntersects(?baGeo,?uaGeo)). " + "} " + "GROUP BY ?burntArea "
      + "HAVING (strdf:area(strdf:extent(?uaGeo)) < 8) ";

  @Test
  public void testQuery1Having() throws MalformedQueryException, QueryEvaluationException,
      TupleQueryResultHandlerException, IOException {
    strabon.query(query1Having, strabon.getSailRepoConnection());

  }

  @Test
  public void testQuery2HavingA() throws MalformedQueryException, QueryEvaluationException,
      TupleQueryResultHandlerException, IOException {
    strabon.query(query2HavingA, strabon.getSailRepoConnection());

  }

  @Test
  public void testQuery2HavingB() throws MalformedQueryException, QueryEvaluationException,
      TupleQueryResultHandlerException, IOException {
    strabon.query(query2HavingB, strabon.getSailRepoConnection());

  }

  @Test
  public void testQuery2HavingC() throws MalformedQueryException, QueryEvaluationException,
      TupleQueryResultHandlerException, IOException {
    strabon.query(query2HavingC, strabon.getSailRepoConnection());

  }


}
