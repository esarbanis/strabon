/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBValueColumnBase;

/**
 * Represents a variable's URI value in an SQL expression.
 */
public class GeneralDBURIColumn extends GeneralDBValueColumnBase {

  public GeneralDBURIColumn(Var var) {
    super(var);
  }

  public GeneralDBURIColumn(GeneralDBColumnVar var) {
    super(var);
  }

  @Override
  public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor) throws X {
    visitor.meet(this);
  }

}
