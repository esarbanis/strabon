/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import org.openrdf.sail.generaldb.schema.ValueTable;

import java.sql.Types;

/**
 * Optimises prepared insert statements for PostgreSQL and overrides the DOUBLE column type.
 */
public class GeneralDBSqlValueTable extends ValueTable {

  @Override
  public String sql(int type, int length) {
    switch (type) {
      case Types.DOUBLE:
        return "double precision";
      default:
        return super.sql(type, length);
    }
  }

}
