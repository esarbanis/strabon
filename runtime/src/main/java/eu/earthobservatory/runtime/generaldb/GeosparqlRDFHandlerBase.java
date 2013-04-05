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
	
	private static String TYPE = RDF.TYPE.stringValue();
	private static String SUBCLASS = RDFS.SUBCLASSOF.stringValue();

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
		if( pred.startsWith(GeoConstants.GEO+"sf") ||
			pred.startsWith(GeoConstants.GEO+"eh") || 
			pred.startsWith(GeoConstants.GEO+"rcc8"))
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
				
			} else if (SimpleFeatures.LineString.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.Curve);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.Polygon.equals(obj) || 
					   SimpleFeatures.PolyhedralSurface.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.MultiSurface.equals(obj) ||
					SimpleFeatures.MultiCurve.equals(obj) ||
					SimpleFeatures.MultiPoint.equals(obj)) { // second level
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if ( SimpleFeatures.Line.equals(obj) || 
						SimpleFeatures.LinearRing.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.LineString);
				writeTriple(subj, TYPE, SimpleFeatures.Curve);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.Triangle.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.Polygon);
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.TIN.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.PolyhedralSurface);
				writeTriple(subj, TYPE, SimpleFeatures.Surface);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.MultiPolygon.equals(obj)) { // third level
				writeTriple(subj, TYPE, SimpleFeatures.MultiSurface);
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			} else if (SimpleFeatures.MultiLineString.equals(obj)) {// third level
				writeTriple(subj, TYPE, SimpleFeatures.MultiCurve);
				writeTriple(subj, TYPE, SimpleFeatures.GeometryCollection);
				writeTriple(subj, TYPE, SimpleFeatures.Geometry);
				
			}
		/* Spatial properties
		 * ~~~~~~~~~~~~~~~~~~~~
		 * Infer
		 * 		subj rdf:type geo:Feature
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj {any spatial property defined in Req. 9, 14, and 18} obj
		 */
		} else if ( GeoConstants.GEOspatialDimension.equals(pred)    || GeoConstants.GEOdimension.equals(pred)  		||
					GeoConstants.GEOcoordinateDimension.equals(pred) || GeoConstants.GEOisEmpty.equals(pred) 			||
					GeoConstants.GEOisSimple.equals(pred) 		     || GeoConstants.GEOhasSerialization.equals(pred)   ||
					GeoConstants.GEOasWKT.equals(pred)	 || GeoConstants.GEOasGML.equals(pred)) {
			
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
		writeTriple(SimpleFeatures.Geometry, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Point, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Curve, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Surface, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.GeometryCollection, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.LineString, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Polygon, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.PolyhedralSurface, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.MultiSurface, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.MultiCurve, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.MultiPoint, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Line, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.LinearRing, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.Triangle, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.TIN, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.MultiPolygon, TYPE, RDFS.CLASS.stringValue());
		writeTriple(SimpleFeatures.MultiLineString, TYPE, RDFS.CLASS.stringValue());
		
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
		writeTriple(GeoSPARQL.SpatialObject, TYPE, RDFS.CLASS.stringValue());
		writeTriple(GeoSPARQL.Feature, TYPE, RDFS.CLASS.stringValue());
		writeTriple(GeoSPARQL.Geometry, TYPE, RDFS.CLASS.stringValue());
		
		
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
