/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBDatatypeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBTrueValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

import static org.openrdf.model.vocabulary.XMLSchema.*;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.*;

/**
 * Creates a datatype SQL expression.
 */
public class GeneralDBDatatypeExprFactory extends
    QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

  protected GeneralDBSqlExpr result;

  public GeneralDBSqlExpr createDatatypeExpr(ValueExpr expr)
      throws UnsupportedRdbmsOperatorException {
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
    boolean divide = node.getOperator().equals(MathExpr.MathOp.DIVIDE);
    ValueExpr left = node.getLeftArg();
    ValueExpr right = node.getRightArg();
    GeneralDBSqlCase sqlCase = new GeneralDBSqlCase();
    sqlCase.when(in(str(DOUBLE), type(left), type(right)), str(DOUBLE));
    sqlCase.when(in(str(FLOAT), type(left), type(right)), str(FLOAT));
    sqlCase.when(in(str(DECIMAL), type(left), type(right)), str(DECIMAL));
    sqlCase.when(new GeneralDBTrueValue(), divide ? str(DECIMAL) : str(INTEGER));
    result = sqlCase;
  }

  @Override
  public void meet(Str node) throws UnsupportedRdbmsOperatorException {
    result = sqlNull();
  }

  @Override
  public void meet(ValueConstant vc) {
    result = valueOf(vc.getValue());
  }

  @Override
  public void meet(Var var) {
    if (var.getValue() == null) {
      result = new GeneralDBDatatypeColumn(var);
    } else {
      result = valueOf(var.getValue());
    }
  }

  @Override
  protected void meetNode(QueryModelNode arg) throws UnsupportedRdbmsOperatorException {
    throw unsupported(arg);
  }

  private GeneralDBSqlExpr valueOf(Value value) {
    if (value instanceof Literal) {
      URI datatype = ((Literal) value).getDatatype();
      if (datatype != null)
        return str(datatype.stringValue());
    }
    return sqlNull();
  }

  private GeneralDBSqlExpr type(ValueExpr expr) throws UnsupportedRdbmsOperatorException {
    return createDatatypeExpr(expr);
  }

}
