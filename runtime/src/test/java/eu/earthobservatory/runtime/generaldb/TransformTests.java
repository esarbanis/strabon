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

public class TransformTests {

	public static Strabon strabon;

	protected static String jdbcDriver= "org.postgresql.Driver";  
	protected static String serverName = "localhost";
	protected static String username = "postgres";
	protected static String password = "postgres";
	protected static Integer port = 5432;
	protected static java.sql.Connection conn = null;
	protected static String databaseName = null; 
	


	String prefixes = "PREFIX gag: <http://www.semanticweb.org/ontologies/2011/gagKallikratis.rdf#> \n"+
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n"+
			"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n"+
			"PREFIX base: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n"+
			"PREFIX ex: <http://www.example.org/ontology#> \n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX geof: <http://www.opengis.net/def/queryLanguage/OGC-GeoSPARQL/1.0/function/> \n";

	protected String query1 = prefixes +
			"SELECT    ?H1 ?HAT1 ?HGEO1 " +
			"(strdf:transform(?HGEO1, <http://www.opengis.net/def/crs/EPSG/0/4326> ) AS ?converted)" +
			"(strdf:transform(strdf:union(?HGEO1,?HGEO1),<http://www.opengis.net/def/crs/EPSG/0/4326>) AS ?united) "  +
			"WHERE { \n"+
			"?H1 rdf:type noa:Hotspot . \n"+
			"?H1 noa:hasConfidence ?HCONF1 . \n"+
			"?H1 noa:hasGeometry ?HGEO1 . \n"+
			"?H1 noa:hasAcquisitionTime ?HAT1 . \n"+
//			" FILTER(strdf:mbbIntersects(?HGEO1,?HGEO1)) "+
			"}" +
			" LIMIT 5 \n";

//	protected String query2 = prefixes +
//			"SELECT    ?H1 ?H2 (strdf:transform(strdf:union(?HGEO1,?HGEO2),<http://www.opengis.net/def/crs/EPSG/0/4326>) AS ?united)"  +
//			"WHERE { \n"+
////			"?H1 rdf:type noa:Hotspot . \n"+
////			"?H2 rdf:type noa:Hotspot . \n"+
//			"?H1 noa:hasGeometry ?HGEO1 . \n"+
//			"?H2 noa:hasGeometry ?HGEO2 . \n"+
////			"?H1 noa:producedFromProcessingChain ?PC1 . \n"+
////			"?H2 noa:producedFromProcessingChain ?PC2 . \n"+
////			" FILTER(strdf:mbbIntersects(?HGEO1,?HGEO2)) "+
//			" FILTER(?H1 != ?H2) "+
//			"}" +
//			" LIMIT 5 \n";



	protected String query3= prefixes +
			"SELECT ?H ?HGEO ?HAT " +
			"(strdf:transform(strdf:intersection(?HGEO, strdf:union(?CGEO)),<http://www.opengis.net/def/crs/EPSG/0/4326>) AS ?DIF) \n"+                                                      
			"WHERE { \n"+ 
			"	?H rdf:type noa:Hotspot ; \n"+
			"      noa:hasAcquisitionTime ?HAT ; \n"+    
			"	   noa:isDerivedFromSensor ?HS ; \n"+    
			"	   noa:hasGeometry ?HGEO . \n"+
			"	FILTER(str(?HS) = \"MSG2\") . \n"+  
			"	FILTER(str(?HAT) = \"2007-08-24T14:45:00\") . \n"+
			"	?C rdf:type noa:Coastline ; \n"+
			"	   noa:hasGeometry ?CGEO . \n"+    
			"	FILTER( strdf:mbbIntersects(?HGEO, ?CGEO) ) . \n"+
			"} \n"+
			"GROUP BY ?H ?HAT ?HGEO\n"+
			"HAVING strdf:overlap(strdf:union(?CGEO), ?HGEO) " +
			" ";

	


}
