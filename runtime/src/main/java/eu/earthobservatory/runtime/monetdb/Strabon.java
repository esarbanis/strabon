package eu.earthobservatory.runtime.monetdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.sail.monetdb.MonetDBSqlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.Strabon.class);

	public Strabon(String databaseName, String user, String password, int port, 
			String serverName, boolean checkForLockTable) throws SQLException, ClassNotFoundException {
		super(databaseName, user, password, port, serverName, checkForLockTable);
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
		logger.info("[Strabon] Initialization completed.");
	}


	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName) throws SQLException, ClassNotFoundException {
		String url = "";
		try {
			logger.info("[Strabon] Cleaning...");
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
			logger.error("SQL Exception occured. Connection URL: " + url, e);
			throw e;
		} catch (ClassNotFoundException e) {
			logger.error("Could not load monetdb jdbc driver...", e);
			throw e;
		}
	}
}
