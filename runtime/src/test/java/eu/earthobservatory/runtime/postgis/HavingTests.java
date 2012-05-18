package eu.earthobservatory.runtime.postgis;

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


public class HavingTests extends eu.earthobservatory.runtime.generaldb.HavingTests {
	
	@BeforeClass
	public static void beforeClass() throws SQLException, ClassNotFoundException, RDFParseException, RepositoryException, RDFHandlerException, IOException, InvalidDatasetFormatFault
	{
	 TemplateTests.beforeClass("/having-tests-srid.nt");
	}
	
	@AfterClass
	public static void afterClass() throws SQLException
	{
		TemplateTests.afterClass();
	}

}
