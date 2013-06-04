package org.openrdf.sail.sqlite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.sqlite.SQLiteConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBConnectionFactory;
import org.openrdf.sail.sqlite.SqliteConnectionFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;

public class SpatiaLiteSqlStore extends org.openrdf.sail.generaldb.GeneralDBStore{

	protected String databaseName;

	@Override
	protected GeneralDBConnectionFactory newFactory(DatabaseMetaData metaData)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void initialize()
		throws SailException
	{
		try {
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e) {
			throw new RdbmsException(e.toString(), e);
		}
		StringBuilder url = new StringBuilder();
		url.append("jdbc:sqlite:");

		url.append(databaseName);
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(url.toString());
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);
		for (Map.Entry e : config.toProperties().entrySet()) {
            ds.addConnectionProperty((String)e.getKey(), (String)e.getValue());
        }
    		
		SqliteConnectionFactory factory = new SqliteConnectionFactory();
		factory.setSail(this);
		factory.setDataSource(ds);
		setBasicDataSource(ds);
		setConnectionFactory(factory);
		
		
		super.initialize();
		
	}

	public void setDatabaseName(String databaseName2) {
		this.databaseName = databaseName2;		
	}

}
