/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBSqlTable extends GeneralDBSqlTable {

	public MonetDBSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
//		return "VACUUM ANALYZE " + getName();
		return null; // TODO vacuum analyze in monetdb
	}

	@Override
	protected String buildClear() {
//		return "TRUNCATE " + getName();
		return null; // TODO vacuum analyze in monetdb
	}

}