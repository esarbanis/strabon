package eu.earthobservatory.testsuite.utils;

/**
 * @author efthymis
 */
public class JdbcUrlBuilder {
    private static final String H2_TEMPLATE = "jdbc:%s:file:~/.strabon/%s";
    private static final String POSTGRIS_TEMPLATE = "jdbc:%s://%s:%s";

    private final String dbType;
    private String host;
    private String port;
    private String databaseName;

    public JdbcUrlBuilder(String dbType) {
        this.dbType = dbType;
    }

    public static JdbcUrlBuilder forDb(String dbType) {
        return new JdbcUrlBuilder(dbType);
    }

    public JdbcUrlBuilder host(String serverName) {
        this.host = serverName;
        return this;
    }

    public JdbcUrlBuilder port(String port) {
        this.port = port;
        return this;
    }

    public String build() {
        String url = null;
        if("postgris".equals(dbType)) {
            url = String.format(POSTGRIS_TEMPLATE, dbType, host, port);
        } else {
            url = String.format(H2_TEMPLATE, dbType, databaseName);
        }
        return url;
    }

    public JdbcUrlBuilder databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }
}
