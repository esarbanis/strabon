package eu.earthobservatory.runtime.monetdb;

import java.io.IOException;
import java.io.InputStream;
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

public class AggregateTests extends eu.earthobservatory.runtime.generaldb.AggregateTests {

	@BeforeClass
	public static void beforeClass(String inputfile) throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
		 TemplateTests.beforeClass("/aggregate-tests-srid.nt");

	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		strabon.close();
	}
	

}
