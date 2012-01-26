/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class PostGISSqlTable extends GeneralDBSqlTable {

	public PostGISSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
		return "VACUUM ANALYZE " + getName();
	}

	@Override
	protected String buildClear() {
		return "TRUNCATE " + getName();
	}
}