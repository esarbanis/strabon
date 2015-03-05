/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.h2.evaluation;

import org.openrdf.sail.generaldb.evaluation.*;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public class H2QueryBuilderFactory extends GeneralDBQueryBuilderFactory {

  @Override
  public GeneralDBQueryBuilder createQueryBuilder() {
    GeneralDBQueryBuilder query = new H2QueryBuilder(createSqlQueryBuilder());
    query.setValueFactory(vf);
    query.setUsingHashTable(usingHashTable);
    return query;
  }

  @Override
  public GeneralDBSqlExprBuilder createSqlExprBuilder() {
    return new H2SqlExprBuilder(this);
  }

  @Override
  public GeneralDBSqlRegexBuilder createSqlRegexBuilder(GeneralDBSqlExprBuilder where) {
    return new H2SqlRegexBuilder(where, this);
  }

  @Override
  public GeneralDBSqlBracketBuilder createSqlBracketBuilder(GeneralDBSqlExprBuilder where) {
    return new H2SqlBracketBuilder(where, this);
  }

  @Override
  public GeneralDBSqlCastBuilder createSqlCastBuilder(GeneralDBSqlExprBuilder where, int type) {
    return new H2SqlCastBuilder(where, this, type);
  }

}
