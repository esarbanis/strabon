/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.h2.config;

import org.openrdf.sail.h2.H2Store;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.generaldb.GeneralDBStore;
import org.openrdf.sail.generaldb.config.GeneralDBStoreConfig;
import org.openrdf.sail.generaldb.config.GeneralDBStoreFactory;

/**
 * A {@link org.openrdf.sail.config.SailFactory} that creates {@link org.openrdf.sail.generaldb.GeneralDBStore}s based on RDF configuration data.
 * 
 * @author James Leigh
 */
public class H2StoreFactory extends GeneralDBStoreFactory {

  public GeneralDBStore getSail(SailImplConfig config) throws SailConfigException {
    if (!SAIL_TYPE.equals(config.getType())) {
      throw new SailConfigException("Invalid Sail type: " + config.getType());
    }

    if (config instanceof GeneralDBStoreConfig) {
      GeneralDBStoreConfig rdbmsConfig = (GeneralDBStoreConfig) config;

      String jdbcDriver = rdbmsConfig.getJdbcDriver();
      String url = rdbmsConfig.getUrl();
      String user = rdbmsConfig.getUser();
      String password = rdbmsConfig.getPassword();

      GeneralDBStore store = new H2Store(jdbcDriver, url, user, password);

      store.setMaxNumberOfTripleTables(rdbmsConfig.getMaxTripleTables());

      return store;
    }

    throw new IllegalArgumentException(
        "Supplied config objects should be an RdbmsStoreConfig, is: " + config.getClass());
  }

}
