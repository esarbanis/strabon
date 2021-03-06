/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBBNodeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.*;

/**
 * BNode expression factory - creates a {@link GeneralDBBNodeColumn} or a
 * {@link GeneralDBStringValue} of the BNode ID.
 */
public class GeneralDBBNodeExprFactory extends
    QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

  protected GeneralDBSqlExpr result;

  public GeneralDBSqlExpr createBNodeExpr(ValueExpr expr) throws UnsupportedRdbmsOperatorException {
    result = null;
    if (expr == null)
      return new GeneralDBSqlNull();
    expr.visit(this);
    if (result == null)
      return new GeneralDBSqlNull();
    return result;
  }

  @Override
  public void meet(Datatype node) {
    result = sqlNull();
  }

  @Override
  public void meet(Lang node) throws UnsupportedRdbmsOperatorException {
    result = sqlNull();
  }

  @Override
  public void meet(MathExpr node) throws UnsupportedRdbmsOperatorException {
    result = sqlNull();
  }

  @Override
  public void meet(Str node) {
    result = sqlNull();
  }

  @Override
  public void meet(ValueConstant vc) {
    result = valueOf(vc.getValue());
  }

  @Override
  public void meet(Var var) {
    if (var.getValue() == null) {
      result = new GeneralDBBNodeColumn(var);
    } else {
      result = valueOf(var.getValue());
    }
  }

  @Override
  protected void meetNode(QueryModelNode arg) throws UnsupportedRdbmsOperatorException {
    throw unsupported(arg);
  }

  private GeneralDBSqlExpr valueOf(Value value) {
    if (value instanceof BNode)
      return str(((BNode) value).stringValue());
    return sqlNull();
  }
}
