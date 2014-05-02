/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;

import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

public class NOATests {
	public static Strabon strabon;
	

	protected static String jdbcDriver= "org.postgresql.Driver";  
	protected static String serverName = "localhost";
	protected static String username = "postgres";
	protected static String password = "postgres";
	protected static Integer port = 5432;
	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 
	


	String prefixes = 
		"PREFIX imAn: <http://teleios.di.uoa.gr/ontologies/imageAnnotationOntology.owl#> "+
		"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> "+
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		"PREFIX eolo:<http://www.dlr.de/ontologies/EOLO.owl#> "+
		"PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> "+
		"PREFIX base: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> "+
		"PREFIX ex: <http://www.example.org/ontology#> "+
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

	String query1 = prefixes+
	"SELECT ?FILENAME { "+
	"?COLLECTION rdf:type imAn:NOA-UC . "+
	"?FILE noa:belongToCollection ?COLLECTION . "+
	"?FILE noa:hasFilename ?FILENAME .}";

	String query2 = prefixes+
	"SELECT ?FILENAME { "+
	"?COLLECTION rdf:type imAn:NOA-UC . "+
	"?FILE noa:belongToCollection ?COLLECTION . "+
	"?FILE noa:hasFilename ?FILENAME . "+
	"?COLLECTION eolo:hasProcessingLevel eolo:L1 . "+
	"?COLLECTION eolo:hasResolution eolo:LowResolution .}";

	String query3 = prefixes+
	"SELECT     ?FILENAME "+
	"WHERE {    ?FILE rdf:type noa:ShpFile . "+
	"?FILE noa:hasFilename ?FILENAME . "+
	"?FILE noa:hasAcquisitionTime ?SENSING_TIME . "+
	"FILTER( str(?SENSING_TIME) > \"2010-08-21T20:00:00\" ) . "+
	"FILTER( str(?SENSING_TIME) < \"2010-08-21T20:30:00\" ) . "+
	"?FILE noa:isDerivedFromSensor ?SENSOR . "+
	"FILTER( str(?SENSOR) = \"MSG1_RSS\" ) . }";


	protected String query4 = prefixes+
	"SELECT DISTINCT ?H1 ?HGEO1 ?CONFIRMATION1 ?SATTELITE1 ?HCONF1 ?PC1 "+
	"WHERE { "+
	"?H1 rdf:type noa:Hotspot . "+
	"?H1 noa:hasConfidence ?HCONF1 . "+
	"?H1 noa:hasGeometry ?HGEO1 . "+
	"?H1 noa:hasAcquisitionTime ?HAT1 . "+
	"?H1 noa:isDerivedFromSensor ?SENSOR1 . "+
	"?H1 noa:isDerivedFromSatellite ?SATTELITE1 . "+
	"?H1 noa:producedFromProcessingChain ?PC1 . "+
	"?H1 noa:hasConfirmation ?CONFIRMATION1 . "+
	"FILTER( \"2010-08-22T15:35:00\" < str(?HAT1) " +
	"&& str(?HAT1) < \"2010-08-22T16:10:00\" && str(?SENSOR1) = \"MSG1_RSS\" && str(?PC1) = \"plain\" ) . "+
	"OPTIONAL { "+
	"?H2 rdf:type noa:Hotspot . "+
	"?H2 noa:hasGeometry  ?HGEO2 . "+
	"?H2 noa:hasAcquisitionTime ?HAT2 . "+
	"?H2 noa:isDerivedFromSensor ?SENSOR2 . "+
	"?H2 noa:producedFromProcessingChain ?PC2 . "+
	"FILTER( strdf:equals(?HGEO1 , ?HGEO2) ) . "+
	"FILTER( str(?HAT2) = \"2010-08-22T16:10:00\" && str(?SENSOR2) = \"MSG1_RSS\" " +
	"&& str(?PC2) = \"plain\" ) . "+
	"} . "+
	"FILTER (!bound(?H2)) .} ";

	String query5 = prefixes+
	"SELECT DISTINCT ?H "+
	"WHERE { "+
	"?H rdf:type noa:Hotspot . "+
	"?H noa:hasGeometry ?HGEO . "+
	"?H noa:isDerivedFromSensor ?HS . "+
	"FILTER( str(?HS) = \"MSG2\" ) . "+
	"?H noa:hasAcquisitionTime ?HAT . "+
	"FILTER(str(?HAT) = \"2007-08-24T12:45:00\") . "+
	"OPTIONAL { "+
	"?C rdf:type noa:Coastline . "+
	"?C noa:hasGeometry ?CGEO . "+
	"filter ( strdf:mbbIntersects(?HGEO , ?CGEO) ) . "+
	"} FILTER( !bound(?C) ) "+
	"}";

	protected String query6 = prefixes+
	"SELECT ?H (strdf:intersection(?HGEO, ?CGEO) AS ?REFINEDGEO) "+
	"WHERE { "+ 
	"?H rdf:type noa:Hotspot . "+
	"?H noa:hasGeometry ?HGEO . "+
	"?H noa:isDerivedFromSensor ?HS . "+
	"FILTER( str(?HS) = \"MSG2\" ) . "+
	"?H noa:hasAcquisitionTime ?HAT . "+
	"FILTER(str(?HAT) = \"2007-08-24T12:45:00\") . "+
	"?C rdf:type noa:Coastline . "+
	"?C noa:hasGeometry ?CGEO . "+
	"FILTER( strdf:overlap(?HGEO,?CGEO) ) . "+
	"} ORDER BY ?H ";
	
	protected String query7 = prefixes+
	//"SELECT ?H (strdf:intersection(?HGEO, ?CGEO) AS ?REFINEDGEO) "+
			"INSERT  {?H noa:hasGeometry (strdf:intersection(?HGEO, ?CGEO) AS ?REFINEDGEO)} \n"+
	"WHERE { "+ 
	"?H rdf:type noa:Hotspot . "+
	"?H noa:hasGeometry ?HGEO . "+
	"?H noa:isDerivedFromSensor ?HS . "+
	"FILTER( str(?HS) = \"MSG2\" ) . "+
	"?H noa:hasAcquisitionTime ?HAT . "+
	"FILTER(str(?HAT) = \"2007-08-24T12:45:00\") . "+
	"?C rdf:type noa:Coastline . "+
	"?C noa:hasGeometry ?CGEO . "+
	"FILTER( strdf:overlap(?HGEO,?CGEO) ) . "+
	"}  ";


	@Test
	public void testQuery1() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query1,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery2() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query2,strabon.getSailRepoConnection());

	}

	@Test
	public void testQuery3() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException
	{
		strabon.query(query3,strabon.getSailRepoConnection());

	}
	
	@Test
	public void testQuery7() throws MalformedQueryException
	{
		strabon.update(query7,strabon.getSailRepoConnection());

	}

}
