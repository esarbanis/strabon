package eu.earthobservatory.runtime.postgis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openrdf.sail.postgis.PostGISSqlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.postgis.Strabon.class);
	
	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) 
	throws SQLException, ClassNotFoundException {
		super(databaseName, user, password, port, serverName, checkForLockTable);
	}


	protected void initiate(String databaseName, String user, String password, int port, String serverName) {
		db_store = new PostGISSqlStore();

		PostGISSqlStore postGIS_store = (PostGISSqlStore) db_store;
		
		postGIS_store.setDatabaseName(databaseName);
		postGIS_store.setUser(user);
		postGIS_store.setPassword(password);
		postGIS_store.setPortNumber(port);
		postGIS_store.setServerName(serverName);
		postGIS_store.setMaxNumberOfTripleTables(2048);
		init();
		logger.info("[Strabon] Initialization completed.");
	}
	
	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
		throws SQLException, ClassNotFoundException {
		String url = "";
		try {
			logger.info("[Strabon] Cleaning...");
			Class.forName("org.postgresql.Driver");
			url = "jdbc:postgresql://" + serverName + ":" + port + "/"
			+ databaseName + "?user=" + user + "&password=" + password;
			Connection conn = DriverManager.getConnection(url);
			java.sql.Statement st = conn.createStatement();
			st.execute("DROP TABLE IF EXISTS locked;");
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error("[Strabon.checkAndDeleteLock] SQL Exception occured. Connection URL is <"+url+">: " + e.getMessage());
			throw e;
		} catch (ClassNotFoundException e) {
			logger.error("[Strabon.checkAndDeleteLock] Could not load postgres jdbc driver: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public void deregisterDriver() {
		try {
			logger.info("[Strabon.deregisterDriver] Deregistering JDBC driver...");
	        java.sql.Driver driver = DriverManager.getDriver("jdbc:postgresql://" + serverName + ":" + port + "/");
	        DriverManager.deregisterDriver(driver);
	        logger.info("[Strabon.deregisterDriver] JDBC driver deregistered successfully.");
	        
	    } catch (SQLException e) {
	        logger.warn("[Strabon.deregisterDriver] Could not deregister JDBC driver: {}", e.getMessage());
	    }
	}
}
