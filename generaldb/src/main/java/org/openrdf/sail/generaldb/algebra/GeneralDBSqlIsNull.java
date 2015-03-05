/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * The SQL IS NULL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlIsNull extends UnaryGeneralDBOperator {

  public GeneralDBSqlIsNull(GeneralDBSqlExpr arg) {
    super(arg);
  }

  @Override
  public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor) throws X {
    visitor.meet(this);
  }

}
