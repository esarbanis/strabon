/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb.evaluation;

import java.math.BigDecimal;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

/**
 * Assemblies an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBSqlExprBuilder extends GeneralDBSqlExprBuilder {

	public MonetDBSqlExprBuilder(GeneralDBQueryBuilderFactory factory) {
		super(factory);
	}

	@Override
	public void appendBoolean(boolean booleanValue) {
		if (booleanValue) {
			where.append(" (1=1) ");
		}
		else {
			where.append(" (0=1) ");
		}
	}
	
	@Override
	public GeneralDBSqlExprBuilder appendNumeric(Number doubleValue) {
		String dataType = null;
		if ( doubleValue instanceof Integer ){
			dataType = "INTEGER";
		} else if (doubleValue instanceof Long ) {
			dataType = "BIGINT";
		} else if ( doubleValue instanceof BigDecimal ) {
			int precision = ((BigDecimal)doubleValue).precision();
			int scale = ((BigDecimal)doubleValue).scale();
			dataType = "DECIMAL(,"+precision+","+scale+")";
		} else if ( doubleValue instanceof Byte ) {
			dataType = "TINYINT";
		} else if ( doubleValue instanceof Double ) {
			dataType = "DOUBLE";
		} else if ( doubleValue instanceof Float ) {
			dataType = "REAL";
		} else if ( doubleValue instanceof Short ) {
			dataType = "SMALLINT";
		} else {
			where.append(" ? ");
			parameters.add(doubleValue);
			return this;
		}
			
		where.append(" CAST( ? AS "+dataType+") ");
		parameters.add(doubleValue);
		return this;
	}

// TODO should this be overriden ??
//	public MonetDBSqlExprBuilder number(Number time) {
//		where.append(" ? ");
//		parameters.add(time);
//		return this;
//	}

//	// TODO should this be overriden ??
//	protected String getSqlNull() {
////		return "false"; // FIXME
//		return NULL;
//	}

}