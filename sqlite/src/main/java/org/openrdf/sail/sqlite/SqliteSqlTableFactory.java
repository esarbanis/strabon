package org.openrdf.sail.sqlite;

import org.openrdf.sail.generaldb.GeneralDBSqlTableFactory;
import org.openrdf.sail.rdbms.schema.RdbmsTable;

public class SqliteSqlTableFactory extends GeneralDBSqlTableFactory {

	@Override
	protected RdbmsTable newTable(String name) {
		return new SqliteSqlTable(name);
	}
}