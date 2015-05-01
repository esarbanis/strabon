package eu.earthobservatory.org.StrabonEndpoint;

import eu.earthobservatory.runtime.generaldb.Strabon;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author efthymis
 */
public class StrabonBeanWrapperDBConfiguration {
    private static final Logger LOGGER = getLogger(StrabonBeanWrapperDBConfiguration.class);

    private String serverName;
    private int port;
    private String databaseName;
    private String user;
    private String password;
    private boolean checkForLockTable;
    private String dbBackend;

    public StrabonBeanWrapperDBConfiguration(String serverName, int port, String databaseName,
        String user, String password, boolean checkForLockTable, String dbBackend) {
        this.serverName = serverName;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.checkForLockTable = checkForLockTable;
        this.dbBackend = dbBackend;
    }

    Strabon initStrabon() throws Exception {
        if (Common.DBBACKEND_MONETDB.equalsIgnoreCase(dbBackend)) {
            return
                new eu.earthobservatory.runtime.monetdb.Strabon(databaseName, user, password, port,
                    serverName, checkForLockTable);

        } else {
            // check whether the user typed wrong database backend and report
            if (!Common.DBBACKEND_POSTGIS.equalsIgnoreCase(dbBackend)) {
                LOGGER.warn("[StrabonEndpoint] Unknown database backend \"" + dbBackend
                    + "\". Assuming PostGIS.");
            }

            // use PostGIS as the default database backend
            return
                new eu.earthobservatory.runtime.postgis.Strabon(databaseName, user, password, port,
                    serverName, checkForLockTable);
        }
    }

    void setConnectionDetails(String dbname, String username, String password, String port,
        String hostname, String dbengine) {
        this.databaseName = dbname;
        this.user = username;
        this.password = password;
        try {
            this.port = Integer.valueOf(port);
        } catch (NumberFormatException e) {
            this.port = 5432;
        }
        this.serverName = hostname;
        this.dbBackend = dbengine;
        this.checkForLockTable = true;
    }

    String getDetails() {
        String details = "-----------------------------------------\n";
        details += "host     : " + serverName + "\n";
        details += "port     : " + port + "\n";
        details += "database : " + databaseName + "\n";
        details += "user     : " + user + "\n";
        details += "password : " + password + "\n";
        details += "-----------------------------------------\n";

        return details;
    }

    void populateWithConnectionDetails(HttpServletRequest request) {
        request.setAttribute("username", user);
        request.setAttribute("password", password);
        request.setAttribute("dbname", databaseName);
        request.setAttribute("hostname", serverName);
        request.setAttribute("port", port);
        request.setAttribute("dbengine", dbBackend);
    }
}
