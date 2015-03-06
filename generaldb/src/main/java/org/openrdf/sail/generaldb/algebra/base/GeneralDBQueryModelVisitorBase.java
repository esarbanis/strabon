/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.*;

/**
 * Base class for RDBMS visitor classes. This class is extended with additional meet methods.
 */
public class GeneralDBQueryModelVisitorBase<X extends Exception> extends QueryModelVisitorBase<X> {

  public void meet(GeneralDBBNodeColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBDatatypeColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBDateTimeColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBDoubleValue node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBFalseValue node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBHashColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBIdColumn node) throws X {
    meetSqlExpr(node);
  }

  public void meet(GeneralDBJoinItem node) throws X {
    meetFromItem(node);
  }

  public void meet(GeneralDBLabelColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBLanguageColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBLongLabelColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBLongURIColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBNumberValue node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBNumericColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBRefIdColumn node) throws X {
    meetValueColumnBase(node);
  }

  public void meet(GeneralDBSelectProjection node) throws X {
    meetNode(node);
  }

  public void meet(GeneralDBSelectQuery node) throws X {
    meetNode(node);
  }

  public void meet(GeneralDBSqlAbs node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlAnd node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlCase node) throws X {
    meetNode(node);
  }

  public void meet(GeneralDBSqlCast node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlCompare node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlConcat node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlEq node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlIsNull node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlLike node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlLowerCase node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlMathExpr node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlNot node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlNull node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBSqlOr node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlRegex node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlShift node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBStringValue node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBTrueValue node) throws X {
    meetSqlConstant(node);
  }

  public void meet(GeneralDBUnionItem node) throws X {
    meetFromItem(node);
  }

  public void meet(GeneralDBURIColumn node) throws X {
    meetValueColumnBase(node);
  }

  protected void meetBinarySqlOperator(BinaryGeneralDBOperator node) throws X {
    meetNode(node);
  }

  protected void meetFromItem(GeneralDBFromItem node) throws X {
    meetNode(node);
  }

  protected void meetSqlConstant(GeneralDBSqlConstant<?> node) throws X {
    meetNode(node);
  }

  protected void meetSqlExpr(GeneralDBSqlExpr node) throws X {
    meetNode(node);
  }

  protected void meetUnarySqlOperator(UnaryGeneralDBOperator node) throws X {
    meetNode(node);
  }

  protected void meetValueColumnBase(GeneralDBValueColumnBase node) throws X {
    meetSqlExpr(node);
  }

  /**
   * FIXME Spatials
   */
  public void meet(GeneralDBSqlSpatialConstructBinary node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlSpatialConstructUnary node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlSpatialMetricBinary node) throws X {
    meetBinarySqlOperator(node);
  }

  public void meet(GeneralDBSqlSpatialMetricUnary node) throws X {
    meetUnarySqlOperator(node);
  }

  public void meet(GeneralDBSqlSpatialProperty sqlSpatialTerm) throws X {
    meetUnarySqlOperator(sqlSpatialTerm);

  }

  public void meet(GeneralDBSqlGeoSpatial node) throws X {
    meetBinarySqlOperator(node);
  }

  // Used for ST_Relate
  public void meet(TripleGeneralDBOperator node) throws X {
    meetTripleSqlOperator(node);
  }

  protected void meetTripleSqlOperator(TripleGeneralDBOperator node) throws X {
    meetNode(node);
  }

  /**
   * Addition for datetime metric functions
   */
  public void meet(GeneralDBSqlDateTimeMetricBinary node) throws X {
    meetBinarySqlOperator(node);
  }
  /***/

  // public void meet(GeneralDBSqlAnyInteract node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlDisjoint node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlTouch node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlEquals node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlContains node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlCovers node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlInside node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlCoveredBy node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  //
  // public void meet(GeneralDBSqlOverlap node) throws X
  // {
  // meetBinarySqlOperator(node);
  // }
  /**
	  * 
	  */
}
