/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.egenhofer;


import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSpatial;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public class GeneralDBSqlEgenhofer_Overlap extends GeneralDBSqlGeoSpatial {

  public GeneralDBSqlEgenhofer_Overlap(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
    super(left, right);
  }

}
