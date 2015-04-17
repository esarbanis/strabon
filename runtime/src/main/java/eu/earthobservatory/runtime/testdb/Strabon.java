/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.testdb;

import org.openrdf.sail.h2.H2SqlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {

  private static Logger logger = LoggerFactory
      .getLogger(eu.earthobservatory.runtime.postgis.Strabon.class);

  public Strabon(String databaseName, String user, String password, int port, String serverName,
      boolean checkForLockTable) throws Exception {
    super(databaseName, user, password, port, serverName, checkForLockTable);
  }

  protected void initiate(String databaseName, String user, String password, int port,
      String serverName) {
    db_store = new H2SqlStore();

    H2SqlStore h2SqlStore = (H2SqlStore) db_store;

    h2SqlStore.setDatabaseName(databaseName);
    h2SqlStore.setUser(user);
    h2SqlStore.setPassword(password);
    h2SqlStore.setPortNumber(port);
    h2SqlStore.setServerName(serverName);
    h2SqlStore.setMaxNumberOfTripleTables(2048);
    init();
    logger.info("[Strabon] Initialization completed.");
  }

  protected void checkAndDeleteLock(String databaseName, String user, String password, int port,
      String serverName) throws SQLException, ClassNotFoundException {
    String url = "";
    try {
      logger.info("[Strabon] Cleaning...");
      Class.forName("org.h2.Driver");
      url =
          "jdbc:h2:mem://" + databaseName;
      Connection conn = DriverManager.getConnection(url);
      java.sql.Statement st = conn.createStatement();
      st.execute("DROP TABLE IF EXISTS locked;");
      st.close();
      conn.close();
      logger.info("[Strabon] Clearing Successful.");

    } catch (SQLException e) {
      logger.error("[Strabon.checkAndDeleteLock] SQL Exception occured. Connection URL is <" + url
          + ">: " + e.getMessage());
      throw e;

    } catch (ClassNotFoundException e) {
      logger.error("[Strabon.checkAndDeleteLock] Could not load postgres jdbc driver: "
          + e.getMessage());
      throw e;
    }
  }

  @Override
  public void deregisterDriver() {
    try {
      logger.info("[Strabon.deregisterDriver] Deregistering JDBC driver...");
      java.sql.Driver driver =
          DriverManager.getDriver("jdbc:h2:mem://" + databaseName);
      DriverManager.deregisterDriver(driver);
      logger.info("[Strabon.deregisterDriver] JDBC driver deregistered successfully.");

    } catch (SQLException e) {
      logger
          .warn("[Strabon.deregisterDriver] Could not deregister JDBC driver: {}", e.getMessage());
    }
  }

  @Override
  protected boolean isLocked() throws SQLException, ClassNotFoundException {
    Connection conn = null;
    Statement st = null;
    String url = "";

    try {
      logger.info("[Strabon] Checking for locks...");
      Class.forName("org.h2.Driver");
      url = "jdbc:h2:mem://" + databaseName;

      conn = DriverManager.getConnection(url);
      st = conn.createStatement();
      ResultSet rs =
          st.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename='locked';");

      return rs.next() ? true : false;

    } catch (SQLException e) {
      logger.error("[Strabon.isLocked] SQL Exception occured. Connection URL is <{}>: {}", url,
          e.getMessage());
      throw e;

    } catch (ClassNotFoundException e) {
      logger.error("[Strabon.isLocked] Could not load postgres jdbc driver: {}", e.getMessage());
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
}
