/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.h2.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlRegexBuilder;

/**
 * Facilitates the building of a regular expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class H2SqlRegexBuilder extends GeneralDBSqlRegexBuilder {

  public H2SqlRegexBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
    super(where, factory);
  }

  @Override
  protected void appendRegExp(GeneralDBSqlExprBuilder where) {
    appendValue(where);
    where.append(" ~ ");
    appendPattern(where);
  }

}
