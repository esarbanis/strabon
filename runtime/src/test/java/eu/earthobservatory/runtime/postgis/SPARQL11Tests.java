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

/**
 * A set of simple tests on SPARQL query functionality 
 * 
 * @author George Garbis
 */

public class SPARQL11Tests extends eu.earthobservatory.runtime.generaldb.SPARQL11Tests {
	
	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		strabon = TemplateTests.beforeClass("/sparql11-tests.nt");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass(strabon);
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
