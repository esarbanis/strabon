/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.h2.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class H2SqlBracketBuilder extends H2SqlExprBuilder implements
    GeneralDBSqlBracketBuilder {

  private H2SqlExprBuilder where;

  private String closing = ")";

  public H2SqlBracketBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
    super(factory);
    this.where = (H2SqlExprBuilder) where;
    append("(");
  }

  public String getClosing() {
    return closing;
  }

  public void setClosing(String closing) {
    this.closing = closing;
  }

  public H2SqlExprBuilder close() {
    append(closing);
    where.append(toSql());
    where.addParameters(getParameters());
    return where;
  }
}
