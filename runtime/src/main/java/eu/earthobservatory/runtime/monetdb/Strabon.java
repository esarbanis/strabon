package eu.earthobservatory.runtime.monetdb;

import info.aduna.concurrent.locks.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.openrdf.sail.monetdb.MonetDBSqlStore;

public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws SQLException, ClassNotFoundException {
		super(databaseName, user, password, port, serverName, checkForLockTable);
	}

	public Strabon (String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable, String cachePath) throws SQLException, ClassNotFoundException {

		super(databaseName, user, password, port, serverName, checkForLockTable,cachePath);
	}

	protected void initiate(String databaseName, String user, String password, int port, String serverName) {
		db_store = new MonetDBSqlStore();

		MonetDBSqlStore monetDB_store = (MonetDBSqlStore)db_store;

		//		Map<String, String> properties = new HashedMap();
		//		properties.put("debug", "true");
		//		monetDB_store.setProperties(properties);

		monetDB_store.setDatabaseName(databaseName);
		monetDB_store.setUser(user);
		monetDB_store.setPassword(password);
		monetDB_store.setPortNumber(port);
		monetDB_store.setServerName(serverName);
		monetDB_store.setMaxNumberOfTripleTables(2048);
		init();
		System.out.println("[Strabon] Initiatation completed.");
	}


	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName) throws SQLException, ClassNotFoundException {
		String url = "";
		try {
			System.out.println("[Strabon] Cleaning...");
			Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
			url = "jdbc:monetdb://" + serverName + ":" + port + "/"
					+ databaseName + "?user=" + user + "&password=" + password;
			Connection conn = DriverManager.getConnection(url);
			java.sql.Statement st = conn.createStatement();
			// change
			ResultSet resultSet = st.executeQuery("SELECT true FROM sys._tables WHERE name='locked' AND system=false");
			if ( resultSet.next() )
				st.execute("DROP TABLE \"locked\"");
			st.close();
			conn.close();
		} catch (SQLException e) {
			System.err.println("SQL Exception occured.");
			System.err.println("Connection url: " + url);
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load postgres jdbc driver...");
			e.printStackTrace();
			throw e;
		}
	}
}
