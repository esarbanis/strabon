/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.vocabulary;


/**
 * GeoSPARQL vocabulary, version 1.0.1, Document  11-052r4
 * <a>http://schemas.opengis.net/geosparql/geosparql-1_0_1.zip</a>
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeoSPARQL {
	
	public static final String ONTOLOGY_NAMESPACE 	= "http://www.opengis.net/ont/geosparql#";
	public static final String FUNCTION_NAMESPACE 	= "http://www.opengis.net/def/function/geosparql/";
	public static final String RULE_NAMESPACE 		= "http://www.opengis.net/def/rule/geosparql/";
	
	public static final String GEO  = ONTOLOGY_NAMESPACE;
	public static final String GEOF = FUNCTION_NAMESPACE;
	public static final String GEOR = RULE_NAMESPACE;
	
	/**
	 *  Defined Classes
	 */
	public static final String SpatialObject 		= GEO + "SpatialObject";
	public static final String Feature 				= GEO + "Feature";
	public static final String Geometry				= GEO + "Geometry";
	
	/**
	 * Defined geometry properties
	 */
	public static final String hasGeometry 			= GEO + "hasGeometry";
	public static final String hasDefaultGeometry	= GEO + "hasDefaultGeometry";

}
