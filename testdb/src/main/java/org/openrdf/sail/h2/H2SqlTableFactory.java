/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.h2;

import org.openrdf.sail.generaldb.GeneralDBSqlTableFactory;
import org.openrdf.sail.rdbms.schema.RdbmsTable;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 * 
 */
public class H2SqlTableFactory extends GeneralDBSqlTableFactory {

  @Override
  protected RdbmsTable newTable(String name) {
    return new H2SqlTable(name);
  }
}
