/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

/**
 * This class is a placeholder for various constants around geometries. These
 * constants range from URIs of namespaces, functions, representations, etc.,
 * to other constants, such as the default spatial reference system (SRID) that
 * is assumed in Strabon.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeoConstants {
	/**																		*
	 *  						Namespaces									*
	 * 																		*/
	
	/**
	 * The namespace for stRDF data model
	 */
	public static final String stRDF					= "http://strdf.di.uoa.gr/ontology#";
	
	/**
	 * The URI for the datatype SemiLinearPointSet
	 * (linear constraint-based representation of geometries)
	 */
	public static final String stRDFSemiLinearPointset	= stRDF + "SemiLinearPointSet";

	/**
	 * The URI for the datatype Well-Known Text (WKT)
	 */
	public static final String WKT 						= stRDF + "WKT";

	/**
	 * The URI for the datatype Geography Markup Language (GML) as it defined
	 * in the model stRDF and query language stSPARQL
	 */
	public static final String GML						= stRDF + "GML";

	/**
	 * 
	 * The URI for the namespace of GML.
	 * 
	 * Initially, it was set to "http://www.opengis.net/def/geometryType/OGC-GML/3.2/".
	 * Afterwards, it was set to "http://www.opengis.net/gml/3.2/" in order to be compliant
	 * with GML version 3.3, as defined by OGC in the document with title
	 * <tt>"OGC® Geography Markup Language (GML) — Extended schemas and encoding rules"</tt>
	 * ({@link https://portal.opengeospatial.org/files/?artifact_id=46568}). However, none
	 * of these work with the parser provided by JTS, which assumes that the namespace for
	 * GML should be only "http://www.opengis.net/gml" and nothing else. In every other case,
	 * an exception is thrown by the GML parser.
	 * 
	 * 
	 * @see {@link org.openrdf.query.algebra.evaluation.util.JTSWrapper.GMLReader}, {@link GMLReader}
	 */
	public static final String GML_OGC					= "http://www.opengis.net/gml";
	
	/**
	 * The namespace for geometry functions declared by GeoSPARQL
	 */
	public static final String geof						= "http://www.opengis.net/def/queryLanguage/OGC-GeoSPARQL/1.0/function/";
	
	/**																		*
	 *  						Extended functions 							*
	 *  							stSPARQL								*
	 * 																		*/
	// Spatial Relationships
	public static final String anyInteract 		= stRDF + "anyInteract";
	public static final String intersects 		= stRDF + "intersects";
	public static final String contains 		= stRDF + "contains";
	public static final String coveredBy 		= stRDF + "coveredBy";
	public static final String covers 			= stRDF + "covers";
	public static final String disjoint 		= stRDF + "disjoint";
	public static final String equals 			= stRDF + "equals";
	public static final String inside 			= stRDF + "inside";
	public static final String overlap 			= stRDF + "overlap";
	public static final String touch 			= stRDF + "touch";
	public static final String relate 			= stRDF + "relate";
	public static final String left 			= stRDF + "left";
	public static final String right			= stRDF + "right";
	public static final String above 			= stRDF + "above";
	public static final String below			= stRDF + "below";
	
	// Spatial Relationships utilizing mbb
	public static final String mbbIntersects 	= stRDF + "mbbIntersects";
	public static final String mbbEquals 	= stRDF + "mbbEquals";

	// Spatial Constructs
	public static final String union 			= stRDF + "union";
	public static final String buffer 			= stRDF + "buffer";
	public static final String envelope 		= stRDF + "envelope";
	public static final String convexHull		= stRDF + "convexHull";
	public static final String boundary 		= stRDF + "boundary";
	public static final String intersection 	= stRDF + "intersection";
	public static final String difference 		= stRDF + "difference";
	public static final String symDifference	= stRDF + "symDifference";
	public static final String transform 		= stRDF + "transform";
	
	// Spatial Metric Functions
	public static final String distance 		= stRDF + "distance";
	public static final String area 			= stRDF + "area";

	// Spatial Properties
	public static final String dimension 		= stRDF + "dimension";
	public static final String geometryType 	= stRDF + "geometryType";
	public static final String asText 			= stRDF + "asText";
	public static final String asGML 			= stRDF + "asGML";
	public static final String srid 			= stRDF + "srid";
	public static final String isEmpty 			= stRDF + "isEmpty";
	public static final String isSimple 		= stRDF + "isSimple";

	// Spatial Aggregate Functions
	public static final String extent 			= stRDF + "extent";
	
	/**
	 * Default SRID
	 */
	public static final Integer defaultSRID 	= 4326;
	
	/**																		*
	 *  						Extended functions 							*
	 *  							GeoSPARQL								*
	 * 																		*/	
	// Non-topological
	public static final String geoSparqlDistance 				= geof + "distance"; //3 arguments
	public static final String geoSparqlBuffer 					= geof + "buffer"; //3 arguments
	public static final String geoSparqlConvexHull 				= geof + "convexHull";
	public static final String geoSparqlIntersection 			= geof + "intersection";
	public static final String geoSparqlUnion 					= geof + "union";
	public static final String geoSparqlDifference 				= geof + "difference";
	public static final String geoSparqlSymmetricDifference 	= geof + "symmetricDifference";
	public static final String geoSparqlEnvelope 				= geof + "envelope";
	public static final String geoSparqlBoundary 				= geof + "boundary";

	// Simple Features - 8 functions - all with 2 arguments + boolean
	public static final String sfEquals 						= geof + "sf-equals";
	public static final String sfDisjoint 						= geof + "sf-disjoint";
	public static final String sfIntersects 					= geof + "sf-intersects";
	public static final String sfTouches 						= geof + "sf-touches";
	public static final String sfCrosses 						= geof + "sf-crosses";
	public static final String sfWithin 						= geof + "sf-within";
	public static final String sfContains 						= geof + "sf-contains";
	public static final String sfOverlaps 						= geof + "sf-overlaps";

	// Egenhofer - 8 functions - all with 2 arguments + boolean
	public static final String ehEquals 						= geof + "eh-equals";
	public static final String ehDisjoint 						= geof + "eh-disjoint";
	public static final String ehMeet 							= geof + "eh-meet";
	public static final String ehOverlap 						= geof + "eh-overlap";
	public static final String ehCovers 						= geof + "eh-covers";
	public static final String ehCoveredBy 						= geof + "eh-coveredBy";
	public static final String ehInside 						= geof + "eh-inside";
	public static final String ehContains 						= geof + "eh-contains";

	// RCC8 - 8 functions - all with 2 arguments + boolean
	public static final String rccEquals 						 = geof + "rcc8-eq";
	public static final String rccDisconnected 					 = geof + "rcc8-dc";
	public static final String rccExternallyConnected 			 = geof + "rcc8-ec";
	public static final String rccPartiallyOverlapping 			 = geof + "rcc8-po";
	public static final String rccTangentialProperPartInverse 	 = geof + "rcc8-tppi";
	public static final String rccTangentialProperPart 			 = geof + "rcc8-tpp";
	public static final String rccNonTangentialProperPart 		 = geof + "rcc8-ntpp";
	public static final String rccNonTangentialProperPartInverse = geof + "rcc8-ntppi";
	public static final String geoSparqlRelate 					 = geof + "relate";
	
}
