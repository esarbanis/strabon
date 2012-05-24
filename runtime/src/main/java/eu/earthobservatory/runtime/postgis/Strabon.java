package eu.earthobservatory.runtime.postgis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.postgis.PostGISSqlStore;


public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws SQLException, ClassNotFoundException {
		super(databaseName, user, password, port, serverName, checkForLockTable);
	}


	public Strabon (String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable, String cachePath) throws SQLException, ClassNotFoundException {
		
		super(databaseName, user, password, port, serverName, checkForLockTable,cachePath);
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
		System.out.println("[Strabon] Initiatation completed.");
	}
	
	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
		throws SQLException, ClassNotFoundException {
		String url = "";
		try {
			System.out.println("[Strabon] Cleaning...");
			Class.forName("org.postgresql.Driver");
			url = "jdbc:postgresql://" + serverName + ":" + port + "/"
			+ databaseName + "?user=" + user + "&password=" + password;
			Connection conn = DriverManager.getConnection(url);
			java.sql.Statement st = conn.createStatement();
			st.execute("DROP TABLE IF EXISTS locked;");
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
