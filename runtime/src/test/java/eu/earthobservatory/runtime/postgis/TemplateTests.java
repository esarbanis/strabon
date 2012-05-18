package eu.earthobservatory.runtime.postgis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.SimpleTests;
import eu.earthobservatory.runtime.postgis.Strabon;

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author George Garbis
 */

public class TemplateTests {
	
	public static Strabon strabon;

	public static java.sql.Connection conn = null;
	public static String databaseName = null; 

	public static String jdbcDriver = null;
	public static String serverName = null;
	public static String username = null;
	public static String password = null;
	public static Integer port = null;
	
	@BeforeClass
	public static void beforeClass(String inputFile)
		throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		// Read properties
		Properties properties = new Properties();
		InputStream propertiesStream =  SimpleTests.class.getResourceAsStream("/databases.properties");
		properties.load(propertiesStream);

		serverName = properties.getProperty("postgis.serverName");
		databaseName = properties.getProperty("postgis.databaseName");
		port = Integer.parseInt(properties.getProperty("postgis.port"));
		username = properties.getProperty("postgis.username");
		password = properties.getProperty("postgis.password");
				
		// Connect to database
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		conn = DriverManager.getConnection(url, username, password);
				
//		// Clean database
		Statement stmt = conn.createStatement();
		ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE " +
						"table_schema='public' AND table_name <> 'spatial_ref_sys' " +
						"AND table_name <> 'geometry_columns' AND " +
						"table_name <> 'geography_columns' AND table_name <> 'locked'");
		while (results.next()) {
			String table_name = results.getString("table_name");
			Statement stmt2 = conn.createStatement();
			stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
			stmt2.close();
		}
		stmt.close();
		
	    strabon = new Strabon(databaseName, username, password, port, serverName, true);
		
		loadTestData(inputFile);
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		strabon.close();
	}
	
	protected static void loadTestData(String inputFile)
		throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		URL src = SimpleTests.class.getResource("/simple-tests.ntriples");
		strabon.storeInRepo(src, "NTRIPLES");
	}
	
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@Before
//	public void before()
//		throws Exception
//	{
//		
//	}
//
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@After
//	public void after()
//		throws Exception
//	{
//		// Clean database
//		Statement stmt = conn.createStatement();
//		ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE " +
//						"table_schema='public' and table_name <> 'spatial_ref_sys' " +
//						"and table_name <> 'geometry_columns' and " +
//						"table_name <> 'geography_columns' and table_name <> 'locked'");
//		while (results.next()) {
//			String table_name = results.getString("table_name");
//			Statement stmt2 = conn.createStatement();
//			stmt2.executeUpdate("DROP TABLE \""+table_name+"\"");
//			stmt2.close();
//		}
//			
//		stmt.close();
//	}
}
