/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * A template test. It: 1) creates a database 2) stores a dataset 3) poses a query 4) checks if the
 * results of the query are the expected If you use ORDER BY do NOT use this class. 5) drops the
 * database
 */
public abstract class TemplateTest {
  protected ArrayList<String> queries = new ArrayList<String>();
  protected ArrayList<String> results = new ArrayList<String>();
  protected Boolean inference;
  protected Boolean orderResults;

  private TestFileLoader testFileLoader;

  public TemplateTest() {
    testFileLoader = new TestFileLoader(this.getClass(), queries, results);

    try {
      testFileLoader.loadFolder();
    } catch (URISyntaxException e) {
      e.printStackTrace();
      System.exit(1);
    }

    inference = false;
    orderResults = false;
  }

  @Before
  public void before() throws Exception {
    Utils.createdb();
    Utils.storeDataset(testFileLoader.loadedDataSet(), inference);
  }

  @Test
  public void test() throws Exception {
    Iterator<String> queryFileIterator = queries.iterator();
    Iterator<String> resultsFileIterator = results.iterator();

    while (queryFileIterator.hasNext() && resultsFileIterator.hasNext()) {
      Utils.testQuery(queryFileIterator.next(), resultsFileIterator.next(), orderResults);
    }
  }

  @After
  public void after() throws Exception {
    Utils.dropdb();
  }
}
