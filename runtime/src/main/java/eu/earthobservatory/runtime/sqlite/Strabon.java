package eu.earthobservatory.runtime.sqlite;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.sqlite.SpatiaLiteSqlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.postgis.Strabon.class);
	private String spatiaLiteLib;
	private String pcreLib;
	public Strabon(String databaseName, String spatialite, String regex, boolean checkForLockTable) 
	throws Exception {
		super(databaseName, spatialite, regex, 0, "", checkForLockTable);
	}
	
	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws Exception {
		super(databaseName, user, password, 0, "", checkForLockTable);
		createSpatialAndDeleteTemp(databaseName, user, password);
			
	}

	private void createSpatialAndDeleteTemp(String databaseName, String libspatial, String regex) throws SQLException, ClassNotFoundException {
		String url = "";
		this.spatiaLiteLib=libspatial;
		this.pcreLib=regex;
		try {
			logger.info("[Strabon] Cleaning...");
			Class.forName("org.sqlite.JDBC");
			url = "jdbc:sqlite:" 	+ databaseName ;
			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);
		Connection c=DriverManager.getConnection(url,config.toProperties());
		
		Statement st=c.createStatement();
		//st.execute("SELECT load_extension('/usr/local/lib/libspatialite.so')");
		//st.execute("SELECT load_extension('/usr/lib/sqlite3/pcre.so')");
		st.execute("SELECT load_extension('"+libspatial+"')");
		try{
		st.execute("SELECT load_extension('"+regex+"')");
		}catch(Exception e){
			logger.warn("Error loading regex library. Regular expressions will not be supported.");
			}
		DatabaseMetaData dbm = 	c.getMetaData();
		ResultSet rs = dbm.getTables(null, null, "spatial_ref_sys", null);
		if (!rs.next()) {
			st.execute("SELECT InitSpatialMetaData()");

		}
	//	st.execute("SELECT load_extension('/usr/lib/libspatialite.so')");
		
		// check if "employee" table is there
		rs = dbm.getTables(null, null, "to_drop", null);
		if (!rs.next()) {
			st.executeUpdate("CREATE TABLE to_drop (name VARCHAR(255))");
		}
		else{
			rs=st.executeQuery("SELECT * FROM to_drop");
			ArrayList<String> names=new ArrayList<String>();
			while(rs.next()){
				names.add(rs.getString(1));
				//st.executeUpdate("DROP TABLE IF EXISTS "+ rs.getString(1));
			}
			rs.close();
			st.close();
			c.close(); //!!! If I continue with the same connection there is a "database table is locked" error
			
			Connection c2=DriverManager.getConnection(url);
			Statement st2=c2.createStatement();
			for(String name:names){	
			st2.executeUpdate("DROP TABLE IF EXISTS "+ name+";");
			}
			st2.executeUpdate("DELETE FROM to_drop WHERE (1=1)");
			st2.close();
			c2.close();
		}
		
		//rs.close();
		
		
		} catch (SQLException e) {
			logger.error("[Strabon.checkAndDeleteLock] SQL Exception occured. Connection URL is <"+url+">: " + e.getMessage());
			throw e;
			
		} catch (ClassNotFoundException e) {
			logger.error("[Strabon.checkAndDeleteLock] Could not load sqlite jdbc driver: " + e.getMessage());
			throw e;
		}
	}
	
	
	@Override
	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
		throws SQLException, ClassNotFoundException {
		createSpatialAndDeleteTemp(databaseName, user, password);
		String url = "";
		try {
			logger.info("[Strabon] Cleaning...");
			Class.forName("org.sqlite.JDBC");
			url = "jdbc:sqlite:" 	+ databaseName ;
			Connection conn = DriverManager.getConnection(url);
			java.sql.Statement st = conn.createStatement();
			st.execute("DROP TABLE IF EXISTS locked;");
			st.close();
			conn.close();
			
			logger.info("[Strabon] Clearing Successful.");
			
		} catch (SQLException e) {
			logger.error("[Strabon.checkAndDeleteLock] SQL Exception occured. Connection URL is <"+url+">: " + e.getMessage());
			throw e;
			
		} catch (ClassNotFoundException e) {
			logger.error("[Strabon.checkAndDeleteLock] Could not load sqlite jdbc driver: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public void deregisterDriver() {
		try {
			logger.info("[Strabon.deregisterDriver] Deregistering JDBC driver...");
	        java.sql.Driver driver = DriverManager.getDriver("jdbc:sqlite:"+ databaseName);
	        DriverManager.deregisterDriver(driver);
	        logger.info("[Strabon.deregisterDriver] JDBC driver deregistered successfully.");
	        
	    } catch (SQLException e) {
	        logger.warn("[Strabon.deregisterDriver] Could not deregister JDBC driver: {}", e.getMessage());
	    }
	}

	@Override
	protected boolean isLocked() throws SQLException, ClassNotFoundException{
		createSpatialAndDeleteTemp(databaseName, user, password);
		Connection conn = null;
		Statement st = null;
		String url = "";
		
		try {
			logger.info("[Strabon] Checking for locks...");
			Class.forName("org.sqlite.JDBC");
			url = "jdbc:sqlite:" 	+ databaseName ;
			conn = DriverManager.getConnection(url);
			st = conn.createStatement();
			DatabaseMetaData dbm = 	conn.getMetaData();
			ResultSet rs = dbm.getTables(null, null, "locked", null);
			//dbilid to check for locks in sqlite????
			//ResultSet rs = st.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename='locked';");
			boolean toReturn=false;
			if(rs.next())
				toReturn=true;
			//dbm=null;
			st.close();
			st=null;
			conn.close();
			conn=null;
			return toReturn;
			//return false;
		} catch (SQLException e) {
			logger.error("[Strabon.isLocked] SQL Exception occured. Connection URL is <{}>: {}", url, e.getMessage());
			throw e;
			
		} catch (ClassNotFoundException e) {
			logger.error("[Strabon.isLocked] Could not load sqlite jdbc driver: {}", e.getMessage());
			throw e;
			
		} finally {
			// close statement and connection
			if (st != null) {
				st.close();
			}
			
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Override
	protected void initiate(String databaseName, String user, String password,
			int port, String serverName) {
		db_store = new SpatiaLiteSqlStore();

		SpatiaLiteSqlStore spatialLite_store = (SpatiaLiteSqlStore) db_store;
		
		spatialLite_store.setDatabaseName(databaseName);
		spatialLite_store.setSpatiaLiteLib(spatiaLiteLib);
		spatialLite_store.setPcreLib(pcreLib);
		spatialLite_store.setMaxNumberOfTripleTables(2048);
		init();
		logger.info("[Strabon] Initialization completed.");
		
	}


		
}
