/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import org.openrdf.query.algebra.QueryModelNode;

/**
 * An SQL expression.
 */
public interface GeneralDBSqlExpr extends QueryModelNode {

  public abstract GeneralDBSqlExpr clone();
}
