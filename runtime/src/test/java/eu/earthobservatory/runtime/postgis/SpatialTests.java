package eu.earthobservatory.runtime.postgis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

import eu.earthobservatory.runtime.postgis.Strabon;

public class SpatialTests extends eu.earthobservatory.runtime.generaldb.SpatialTests {

	@BeforeClass
	public static void initialize() throws SQLException, ClassNotFoundException
	{
		strabon = new Strabon("spatial-tests-srid","strabon","p1r3as", 5432, "strabon.di.uoa.gr", true);
	}
	
	@Test
	public void testStrdfLeft() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ // TODO left not implemented in monetdb
		String query = 
			prefixes+
				"SELECT DISTINCT ?id1 \n"+
				"WHERE { \n" +
				" ?s1 ex:id ?id1 . \n"+
				" ?s2 ex:id ?id2 . \n"+
				" FILTER( str(?id1) != str(?id2) ) . \n"+
				" FILTER( str(?id2) = \"Z\"^^xsd:string ) . \n"+
				" ?s2 ex:geometry ?g2 . \n" +
				" ?s1 ex:geometry ?g1 . \n"+
				" FILTER( strdf:left(?g1, ?g2 )) . \n"+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id1=\"A\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfRight() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ // TODO right not implemented in monetdb
		String query = 
			prefixes+
				"SELECT DISTINCT ?id1 \n"+
				"WHERE { \n" +
				" ?s1 ex:id ?id1 . \n"+
				" ?s2 ex:id ?id2 . \n"+
				" FILTER( str(?id1) != str(?id2) ) . \n"+
				" FILTER( str(?id2) = \"Z\"^^xsd:string ) . \n"+
				" ?s2 ex:geometry ?g2 . \n" +
				" ?s1 ex:geometry ?g1 . \n"+
				" FILTER( strdf:right(?g1, ?g2 )) . \n"+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id1=\"D\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfAbove() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ // TODO above not implemented in monetdb
		String query = 
			prefixes+
				"SELECT DISTINCT ?id1 \n"+
				"WHERE { \n" +
				" ?s1 ex:id ?id1 . \n"+
				" ?s2 ex:id ?id2 . \n"+
				" FILTER( str(?id1) != str(?id2) ) . \n"+
				" FILTER( str(?id2) = \"Z\"^^xsd:string ) . \n"+
				" ?s2 ex:geometry ?g2 . \n" +
				" ?s1 ex:geometry ?g1 . \n"+
				" FILTER( strdf:above(?g1, ?g2 )) . \n"+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id1=\"H\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
	
	@Test
	public void testStrdfBelow() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{ // TODO below not implemented in monetdb
		String query = 
			prefixes+
				"SELECT DISTINCT ?id1 \n"+
				"WHERE { \n" +
				" ?s1 ex:id ?id1 . \n"+
				" ?s2 ex:id ?id2 . \n"+
				" FILTER( str(?id1) != str(?id2) ) . \n"+
				" FILTER( str(?id2) = \"Z\"^^xsd:string ) . \n"+
				" ?s2 ex:geometry ?g2 . \n" +
				" ?s1 ex:geometry ?g1 . \n"+
				" FILTER( strdf:below(?g1, ?g2 )) . \n"+
			"}";
		
		ArrayList<String> bindings = (ArrayList<String>) strabon.query(query,strabon.getSailRepoConnection());
		
		assertEquals(1, bindings.size());
		assertTrue(-1<bindings.indexOf("[id1=\"C\"^^<http://www.w3.org/2001/XMLSchema#string>]"));
	}
}
