package eu.earthobservatory.runtime.postgis;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.postgis.Strabon;
import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;

public class AggregateTests extends eu.earthobservatory.runtime.generaldb.AggregateTests {

	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		// Create database
		Date date = new Date(0);
		databaseName = "strabon-test-"+date.getTime();
		Class.forName(jdbcDriver);
		String url = "jdbc:postgresql://"+serverName+":"+port+"/template1";
		conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement();
	    String sql = "CREATE DATABASE \""+databaseName+"\" WITH TEMPLATE = template_postgis";
	    stmt.executeUpdate(sql);
	    stmt.close();
		conn.close();
		
	    // Connect to database
		url = "jdbc:postgresql://"+serverName+":"+port+"/"+databaseName;
		System.out.println("open database");
		conn = DriverManager.getConnection(url, username, password);
	    
		strabon = new Strabon(databaseName,"postgres","postgres", 5432, "localhost", true);
		
		loadTestData();
	}
}
