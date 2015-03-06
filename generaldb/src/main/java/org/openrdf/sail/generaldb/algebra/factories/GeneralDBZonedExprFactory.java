/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlShift;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.*;

/**
 * Creates a binary SQL expression for a dateTime zoned value.
 */
public class GeneralDBZonedExprFactory extends
    QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

  protected GeneralDBSqlExpr result;

  private IdSequence ids;

  public GeneralDBZonedExprFactory(IdSequence ids) {
    super();
    this.ids = ids;
  }

  public GeneralDBSqlExpr createZonedExpr(ValueExpr expr) throws UnsupportedRdbmsOperatorException {
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
  public void meet(Var node) {
    if (node.getValue() == null) {
      result = new GeneralDBSqlShift(new GeneralDBRefIdColumn(node), ids.getShift(), ids.getMod());
    } else {
      result = valueOf(node.getValue());
    }
  }

  @Override
  protected void meetNode(QueryModelNode arg) throws UnsupportedRdbmsOperatorException {
    throw unsupported(arg);
  }

  private GeneralDBSqlExpr valueOf(Value value) {
    if (value instanceof Literal) {
      return num(ids.code((Literal) value));
    }
    return null;
  }
}
