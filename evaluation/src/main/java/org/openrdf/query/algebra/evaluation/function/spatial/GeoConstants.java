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
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
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
	 * The namespace for the RDFi framework
	 */
	public static final String rdfi						= "http://rdfi.di.uoa.gr/ontology#";

	/**
	 * The URI for the datatype SemiLinearPointSet
	 * (linear constraint-based representation of geometries)
	 */
	public static final String stRDFSemiLinearPointset			= stRDF + "SemiLinearPointSet";

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
	 * The namespace for GeoSPARQL ontology
	 */
	public static final String geo						= "http://www.opengis.net/ont/geosparql#";
	
	/**
	 * The namespace for geometry functions declared by GeoSPARQL
	 */
	public static final String geof						= "http://www.opengis.net/def/function/geosparql/";
	
	/**
	 * The URI for the datatype wktLiteral
	 */
	public static final String WKTLITERAL				=  geo + "wktLiteral";
	
	/**
	 * The URI for the datatype gmlLiteral
	 */
	public static final String GMLLITERAL				=  geo + "gmlLiteral";
	
	/**																		*
	 *  						Extended functions 							*
	 *  							stSPARQL								*
	 * 																		*/
	// Spatial Relationships
	public static final String equals 			= stRDF + "equals";
	public static final String disjoint 		= stRDF + "disjoint";
	public static final String intersects 		= stRDF + "intersects";
	public static final String touches 			= stRDF + "touches";
	public static final String within 			= stRDF + "within";
	public static final String contains 		= stRDF + "contains";
	public static final String overlaps 		= stRDF + "overlaps";
	public static final String crosses 			= stRDF + "crosses";
	
	// The generic relate function
	public static final String relate 			= stRDF + "relate";
	
	// Topological Relationships utilizing mbb
	public static final String mbbIntersects	= stRDF + "mbbIntersects";
	public static final String mbbContains 		= stRDF + "mbbContains";
	public static final String mbbEquals 		= stRDF + "mbbEquals";
	public static final String mbbWithin 		= stRDF + "mbbWithin";
	
	// Directional functions
	public static final String left 			= stRDF + "left";
	public static final String right			= stRDF + "right";
	public static final String above 			= stRDF + "above";
	public static final String below			= stRDF + "below";

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
	public static final String sfEquals 						= geof + "sfEquals";
	public static final String sfDisjoint 						= geof + "sfDisjoint";
	public static final String sfIntersects 					= geof + "sfIntersects";
	public static final String sfTouches 						= geof + "sfTouches";
	public static final String sfCrosses 						= geof + "sfCrosses";
	public static final String sfWithin 						= geof + "sfWithin";
	public static final String sfContains 						= geof + "sfContains";
	public static final String sfOverlaps 						= geof + "sfOverlaps";

	// Egenhofer - 8 functions - all with 2 arguments + boolean
	public static final String ehEquals 						= geof + "ehEquals";
	public static final String ehDisjoint 						= geof + "ehDisjoint";
	public static final String ehMeet 							= geof + "ehMeet";
	public static final String ehOverlap 						= geof + "ehOverlap";
	public static final String ehCovers 						= geof + "ehCovers";
	public static final String ehCoveredBy 						= geof + "ehCoveredBy";
	public static final String ehInside 						= geof + "ehInside";
	public static final String ehContains 						= geof + "ehContains";

	// RCC8 - 8 functions - all with 2 arguments + boolean
	public static final String rccEquals 						 = geof + "rcc8eq";
	public static final String rccDisconnected 					 = geof + "rcc8dc";
	public static final String rccExternallyConnected 			 = geof + "rcc8ec";
	public static final String rccPartiallyOverlapping 			 = geof + "rcc8po";
	public static final String rccTangentialProperPartInverse 	 = geof + "rcc8tppi";
	public static final String rccTangentialProperPart 			 = geof + "rcc8tpp";
	public static final String rccNonTangentialProperPart 		 = geof + "rcc8ntpp";
	public static final String rccNonTangentialProperPartInverse = geof + "rcc8ntppi";
	
	// The generic relate function
	public static final String geoSparqlRelate 					 = geof + "relate";

	/**
	 * Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	public static final String diffDateTime = "http://strdf.di.uoa.gr/extensions/ontology#diffDateTime";
	/** End of addition **/


	/**
	 * RCC-8 relations for the RDFi framework
	 */
	public static final String rdfiDC						= rdfi + "DC";
	public static final String rdfiEC						= rdfi + "EC";
	public static final String rdfiPO						= rdfi + "PO";
	public static final String rdfiNTPP						= rdfi + "NTPP";
	public static final String rdfiNTPPi					= rdfi + "NTPPi";
	public static final String rdfiTPP						= rdfi + "TPP";
	public static final String rdfiTPPi						= rdfi + "TPPi";
	public static final String rdfiEQ						= rdfi + "EQ";
}
