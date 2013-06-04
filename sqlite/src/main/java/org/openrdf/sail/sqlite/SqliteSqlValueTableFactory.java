package org.openrdf.sail.sqlite;

import org.openrdf.sail.generaldb.GeneralDBSqlTableFactory;
import org.openrdf.sail.generaldb.GeneralDBSqlValueTableFactory;
import org.openrdf.sail.sqlite.SqliteSqlTableFactory;
import org.openrdf.sail.sqlite.schema.SqliteValueTable;


public class SqliteSqlValueTableFactory extends GeneralDBSqlValueTableFactory {
	
	public SqliteSqlValueTableFactory(SqliteSqlTableFactory sqlTableFactory) {
		super(sqlTableFactory);
	}
	
	public SqliteSqlValueTableFactory(){
		super(new SqliteSqlTableFactory());
	}

	public SqliteValueTable newValueTable() {
		return new SqliteValueTable();
	}
}
