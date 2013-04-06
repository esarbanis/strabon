/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.StringReader;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.vocabulary.GeoSPARQL;
import eu.earthobservatory.vocabulary.SimpleFeatures;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 */
public class GeosparqlRDFHandlerBase extends RDFHandlerBase {
	
	//private static final Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.GeosparqlRDFHandlerBase.class);
	
	private static String TYPE 		= RDF.TYPE.stringValue();
	private static String CLASS 	= RDFS.CLASS.stringValue();
	private static String SUBCLASS 	= RDFS.SUBCLASSOF.stringValue();

	private StringBuffer triples = new StringBuffer(1024);
	
	/** 
	 * The number of triples that the "triples" object above contains.
	 */
	private int numTriples = 0;
	
	public StringBuffer getTriples()
	{
		return triples;
	}
	
	public int getNumberOfTriples() {
		return numTriples;
	}
	
	@Override
	public void startRDF() {
		insertGeoSPARQLClassHierarchy();
		insertSimpleFeaturesClassHierarchy();
	}
	
	@Override
	public void handleStatement(Statement st)
	{
		String subj = st.getSubject().toString();
		String pred = st.getPredicate().toString();
		String obj = st.getObject().toString();
		
		/* Infer
		 * 		subj rdf:type geo:SpatialObject
		 * 		obj  rdf:type geo:SpatialObject
		 * from
		 * 		subj {any topological property from the Topology Vocabulary Extension} obj
		 */
		if( pred.startsWith(GeoSPARQL.GEO+"sf") ||
			pred.startsWith(GeoSPARQL.GEO+"eh") || 
			pred.startsWith(GeoSPARQL.GEO+"rcc8"))
		{
			writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
			writeTriple(obj, TYPE, GeoSPARQL.SpatialObject);
		}
		/* Infer 
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj rdf:type geo:Feature
		 * or
		 * 		subj rdf:type geo:Geometry 
		 */
		else if(pred.equals(TYPE) && (obj.equals(GeoSPARQL.Feature) || obj.equals(GeoSPARQL.Geometry)))
		{
			writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
		} 
		/*
		 * Infer
		 * 		subj rdf:type geo:Feature
		 * 		subj rdf:type geo:SpatialObject
		 * 		obj  rdf:type geo:Feature
		 * 		obj  rdf:type geo:SpatialObject
		 * from
		 * 		subj geo:hasGeometry obj
		 * or
		 * 		sub geo:hasDefaultGeometry obj
		 */
		else if(pred.equals(GeoSPARQL.hasGeometry) || pred.equals(GeoSPARQL.hasDefaultGeometry))
		{
			writeTriple(subj, TYPE, GeoSPARQL.Feature);
			writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
			
			writeTriple(obj, TYPE, GeoSPARQL.Feature);
			writeTriple(obj, TYPE, GeoSPARQL.SpatialObject);
		}
		else if (pred.equals(TYPE)) {
			// GML class hierarchy
			if (obj.equals(GeoConstants.GML_OGC + "GM_Complex")
					|| obj.equals(GeoConstants.GML_OGC + "GM_Aggregate")
					|| obj.equals(GeoConstants.GML_OGC + "GM_Primitive")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;
			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_Composite")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
						+ "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_MultiPrimitive")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Aggregate" + "> .\n" + "<" + subj + "> <"
						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_Point")
					|| obj.equals(GeoConstants.GML_OGC + "GM_OrientablePrimitive")
					|| obj.equals(GeoConstants.GML_OGC + "GM_Solid")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Primitive" + "> .\n" + "<" + subj + "> <"
						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_OrientableCurve")
					|| obj.equals(GeoConstants.GML_OGC + "GM_OrientableSurface")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_Curve")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Aggregate" + "> .\n"
						+ "<" + subj + "> <" + TYPE +"> <" + GeoConstants.GML_OGC + "GM_OrientableCurve" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_Surface")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC+ "GM_Aggregate" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientableSurface" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeCurve")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Aggregate" + "> .\n" + "<" + subj + "> <"
						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientableCurve" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
						+ "> <" + GeoConstants.GML_OGC + "GM_Composite" + "> .\n" + "<"
						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeSurface")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_OrientableSurface" + "> .\n" +

						"<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
						+ "> <" + GeoConstants.GML_OGC + "GM_Composite" + "> .\n" + "<"
						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeSolid")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Solid" + "> .\n" + "<" + subj + "> <" + TYPE
						+ "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n" + "<"
						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Complex"
						+ "> .\n" + "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_Composite" + "> .\n" + "<" + subj + "> <"
						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
				triples.append(triple);
				numTriples++;

			}
			if (obj.equals(GeoConstants.GML_OGC + "GM_MultiPoint")
					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiCurve")
					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiSurface")
					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiSolid")) {
				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
						+ "GM_MultiPrimitive" + "> .\n" + "<" + subj + "> <"
						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Aggregate" + "> .\n" + "<"
						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				numTriples++;

			}
			/*
			 * Simple Features class hierarchy
			 */
			if (SimpleFeatures.Point.equals(obj)   || 
				SimpleFeatures.Curve.equals(obj)   ||
				SimpleFeatures.Surface.equals(obj) ||
				SimpleFeatures.GeometryCollection.equals(obj)) {// first level
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.LineString.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.Curve);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.Polygon.equals(obj) || 
					   SimpleFeatures.PolyhedralSurface.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.MultiSurface.equals(obj) ||
					SimpleFeatures.MultiCurve.equals(obj) ||
					SimpleFeatures.MultiPoint.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if ( SimpleFeatures.Line.equals(obj) || 
						SimpleFeatures.LinearRing.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.LineString);
				writeTriple(subj, TYPE, SimpleFeatures.Curve);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.Triangle.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.Polygon);
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.TIN.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.PolyhedralSurface);
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.MultiPolygon.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.MultiSurface);
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			} else if (SimpleFeatures.MultiLineString.equals(obj)) {// third level
				writeTriple(subj, TYPE, SimpleFeatures.MultiCurve);
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
				writeTriple(subj, TYPE, GeoSPARQL.Geometry);
				writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
				
			}
		/* Spatial properties
		 * ~~~~~~~~~~~~~~~~~~~~
		 * Infer
		 * 		subj rdf:type geo:Feature
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj {any spatial property defined in Req. 9, 14, and 18} obj
		 */
		} else if ( GeoSPARQL.spatialDimension.equals(pred)    || GeoSPARQL.dimension.equals(pred)  		||
					GeoSPARQL.coordinateDimension.equals(pred) || GeoSPARQL.isEmpty.equals(pred) 			||
					GeoSPARQL.isSimple.equals(pred) 		   || GeoSPARQL.hasSerialization.equals(pred)   ||
					GeoSPARQL.asWKT.equals(pred)	 		   || GeoSPARQL.asGML.equals(pred)) {
			
			writeTriple(subj, TYPE, GeoSPARQL.Geometry);
			writeTriple(subj, TYPE, GeoSPARQL.SpatialObject);
		}
	}

	protected void writeTriple(String subj, String pred, String obj) {
		String triple = "<"+subj+"> <"+pred+"> <"+obj+"> .\n";
		//logger.info(triple);
		triples.append(triple);
		numTriples++;
	}

	/**
	 * Materializes the RDF class hierarchy of Simple Features
	 */
	protected void insertSimpleFeaturesClassHierarchy() {
		// insert rdf:type rdfs:Class
		writeTriple(SimpleFeatures.Geometry, TYPE, CLASS);
		writeTriple(SimpleFeatures.Point, TYPE, CLASS);
		writeTriple(SimpleFeatures.Curve, TYPE, CLASS);
		writeTriple(SimpleFeatures.Surface, TYPE, CLASS);
		writeTriple(SimpleFeatures.GeometryCollection, TYPE, CLASS);
		writeTriple(SimpleFeatures.LineString, TYPE, CLASS);
		writeTriple(SimpleFeatures.Polygon, TYPE, CLASS);
		writeTriple(SimpleFeatures.PolyhedralSurface, TYPE, CLASS);
		writeTriple(SimpleFeatures.MultiSurface, TYPE, CLASS);
		writeTriple(SimpleFeatures.MultiCurve, TYPE, CLASS);
		writeTriple(SimpleFeatures.MultiPoint, TYPE, CLASS);
		writeTriple(SimpleFeatures.Line, TYPE, CLASS);
		writeTriple(SimpleFeatures.LinearRing, TYPE, CLASS);
		writeTriple(SimpleFeatures.Triangle, TYPE, CLASS);
		writeTriple(SimpleFeatures.TIN, TYPE, CLASS);
		writeTriple(SimpleFeatures.MultiPolygon, TYPE, CLASS);
		writeTriple(SimpleFeatures.MultiLineString, TYPE, CLASS);
		
		// insert rdfs:subClassOf geo:Geometry
		writeTriple(SimpleFeatures.Geometry, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Point, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Curve, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Surface, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.GeometryCollection, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.LineString, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Polygon, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.PolyhedralSurface, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.MultiSurface, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.MultiCurve, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.MultiPoint, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Line, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.LinearRing, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.Triangle, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.TIN, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.MultiPolygon, SUBCLASS, GeoSPARQL.Geometry);
		writeTriple(SimpleFeatures.MultiLineString, SUBCLASS, GeoSPARQL.Geometry);
		
		// insert rdfs:subClassOf geo:SpatialObject
		writeTriple(SimpleFeatures.Geometry, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Point, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Curve, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Surface, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.GeometryCollection, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.LineString, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Polygon, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.PolyhedralSurface, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.MultiSurface, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.MultiCurve, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.MultiPoint, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Line, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.LinearRing, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.Triangle, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.TIN, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.MultiPolygon, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(SimpleFeatures.MultiLineString, SUBCLASS, GeoSPARQL.SpatialObject);
		
		// first level 
		writeTriple(SimpleFeatures.Point, SUBCLASS, SimpleFeatures.Geometry);
		writeTriple(SimpleFeatures.Curve, SUBCLASS, SimpleFeatures.Geometry);
		writeTriple(SimpleFeatures.Surface, SUBCLASS, SimpleFeatures.Geometry);
		writeTriple(SimpleFeatures.GeometryCollection, SUBCLASS, SimpleFeatures.Geometry);
		
		// second level
		writeTriple(SimpleFeatures.LineString, SUBCLASS, SimpleFeatures.Curve);
		writeTriple(SimpleFeatures.LineString, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.Polygon, SUBCLASS, SimpleFeatures.Surface);
		writeTriple(SimpleFeatures.Polygon, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.PolyhedralSurface, SUBCLASS, SimpleFeatures.Surface);
		writeTriple(SimpleFeatures.PolyhedralSurface, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.MultiSurface, SUBCLASS, SimpleFeatures.GeometryCollection);
		writeTriple(SimpleFeatures.MultiSurface, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.MultiCurve, SUBCLASS, SimpleFeatures.GeometryCollection);
		writeTriple(SimpleFeatures.MultiCurve, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.MultiPoint, SUBCLASS, SimpleFeatures.GeometryCollection);
		writeTriple(SimpleFeatures.MultiPoint, SUBCLASS, SimpleFeatures.Geometry);
		
		// third level
		writeTriple(SimpleFeatures.Line, SUBCLASS, SimpleFeatures.LineString);
		writeTriple(SimpleFeatures.Line, SUBCLASS, SimpleFeatures.Curve);
		writeTriple(SimpleFeatures.Line, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.LinearRing, SUBCLASS, SimpleFeatures.Polygon);
		writeTriple(SimpleFeatures.LinearRing, SUBCLASS, SimpleFeatures.Surface);
		writeTriple(SimpleFeatures.LinearRing, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.Triangle, SUBCLASS, SimpleFeatures.Polygon);
		writeTriple(SimpleFeatures.Triangle, SUBCLASS, SimpleFeatures.Surface);
		writeTriple(SimpleFeatures.Triangle, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.TIN, SUBCLASS, SimpleFeatures.PolyhedralSurface);
		writeTriple(SimpleFeatures.TIN, SUBCLASS, SimpleFeatures.Surface);
		writeTriple(SimpleFeatures.TIN, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.MultiPolygon, SUBCLASS, SimpleFeatures.MultiSurface);
		writeTriple(SimpleFeatures.MultiPolygon, SUBCLASS, SimpleFeatures.GeometryCollection);
		writeTriple(SimpleFeatures.MultiPolygon, SUBCLASS, SimpleFeatures.Geometry);
		
		writeTriple(SimpleFeatures.MultiLineString, SUBCLASS, SimpleFeatures.MultiSurface);
		writeTriple(SimpleFeatures.MultiLineString, SUBCLASS, SimpleFeatures.GeometryCollection);
		writeTriple(SimpleFeatures.MultiLineString, SUBCLASS, SimpleFeatures.Geometry);
	}
	
	/**
	 * Materializes the RDF class hierarchy of GeoSPARQL
	 */
	protected void insertGeoSPARQLClassHierarchy() {
		writeTriple(GeoSPARQL.SpatialObject, TYPE, CLASS);
		writeTriple(GeoSPARQL.Feature, TYPE, CLASS);
		writeTriple(GeoSPARQL.Geometry, TYPE, CLASS);
		
		
		writeTriple(GeoSPARQL.Feature, SUBCLASS, GeoSPARQL.SpatialObject);
		writeTriple(GeoSPARQL.Geometry, SUBCLASS, GeoSPARQL.SpatialObject);
		
	}
	
	public static void main(String[] args) throws Exception {
		NTriplesParser parser = new NTriplesParser();
		parser.setVerifyData(true);

		/*String text = 
				"<http://example.org/rcc8Obj1> <http://www.opengis.net/ont/geosparql#rcc8eq> <http://example.org/rcc8Obj2> . " +
				"<http://example.org/simpleGeometry1> <http://www.opengis.net/ont/geosparql#isEmpty> _:nai . \n"+
		"<http://example.org/ForestArea1> <http://www.opengis.net/ont/geosparql#defaultGeometry> _:b2 . \n"+
		"<http://example.org/SpatialObject1> <http://www.opengis.net/ont/geosparql#ehIntersects> <http://example.org/SpatialObject2> . \n";
		*/
        
		String gmltext= "<http://example.org/GM_MultiSolid> <"+TYPE+"> <"+GeoConstants.GML_OGC+"GM_Object> .\n"; 
		//String sftext= "<http://example.org/Line> <"+type+"> <"+sf+"Geometry> .\n"; 
		
		StringReader reader = new StringReader(gmltext);

		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();

		handler.startRDF();
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
		handler.endRDF();

		reader.close();	

		System.out.println("Original triples: " + gmltext);
		//System.out.println("Geometry domain list: " + handler.getgeometryDomainList());
		System.out.println("New triples: " + handler.getTriples());
	}
}
