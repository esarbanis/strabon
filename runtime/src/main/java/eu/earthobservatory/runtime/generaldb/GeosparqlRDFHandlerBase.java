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
import java.util.Arrays;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.vocabulary.SimpleFeatures;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 */
public class GeosparqlRDFHandlerBase extends RDFHandlerBase {
	
	//private static final Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.GeosparqlRDFHandlerBase.class);
	
	private static String TYPE = RDF.TYPE.stringValue();
	
	public static String SpatialObject 			= GeoConstants.GEO + "SpatialObject";
	public static String Feature 				= GeoConstants.GEO + "Feature";
	public static String Geometry				= GeoConstants.GEO + "Geometry";
	public static String hasGeometry 			= GeoConstants.GEO + "hasGeometry";
	public static String hasDefaultGeometry		= GeoConstants.GEO + "hasDefaultGeometry";
	
	public static String dimension				= GeoConstants.GEO + "dimension";
	public static String coordinateDimension	= GeoConstants.GEO + "coordinateDimension";
	public static String spatialDimension		= GeoConstants.GEO + "spatialDimension";
	public static String isEmpty				= GeoConstants.GEO + "isEmpty";
	public static String isSimple				= GeoConstants.GEO + "isSimple";
	
	public static String hasSerialization		= GeoConstants.GEO + "hasSerialization";
	public static String asWKT					= GeoConstants.GEO + "asWKT";
	public static String asGML					= GeoConstants.GEO + "asGML";
	
	public static List <String> GM_Objects = Arrays.asList("GM_Complex", "GM_Agreggate", "GM_Primitive", "GM_Composite", "GM_MultiPrimitive",
			"GM_Point", "GM_OrientablePrimitive","GM_OrientableCurve","GM_OrientableSurface", "GM_Curve","GM_Surface","GM_Solid",
			 "GM_CompositeCurve", "GM_CompositeSurface", "GM_CompositeSolid", "GM_Multipoint", "GM_MultiCurve", "GM_MultiSurface", "GM_MultiSolid");
	
	public static String WKTLiteral	= GeoConstants.WKTLITERAL;
	public static String GMLLiteral	= GeoConstants.GMLLITERAL;
	
	public static List <String> rcc8 = Arrays.asList(GeoConstants.GEO+"rcc8eq",GeoConstants.GEO+"rcc8dc",GeoConstants.GEO+"rcc8ec",GeoConstants.GEO+"rcc8po",
			GeoConstants.GEO+"rcc8tppi", GeoConstants.GEO+"rcc8tpp",GeoConstants.GEO+ "rcc8ntpp", GeoConstants.GEO+"rcc8ntpp");
	
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
			writeTriple(subj, TYPE, SpatialObject);
			writeTriple(obj, TYPE, SpatialObject);
		}
		/* Infer 
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj rdf:type geo:Feature
		 * or
		 * 		subj rdf:type geo:Geometry 
		 */
		else if(pred.equals(TYPE) && (obj.equals(Feature) || obj.equals(Geometry)))
		{
			writeTriple(subj, TYPE, SpatialObject);
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
		else if(pred.equals(hasGeometry) || pred.equals(hasDefaultGeometry))
		{
			writeTriple(subj, TYPE, Feature);
			writeTriple(subj, TYPE, SpatialObject);
			
			writeTriple(obj, TYPE, Feature);
			writeTriple(obj, TYPE, SpatialObject);
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
		} else if ( spatialDimension.equals(pred)    || dimension.equals(pred)  		||
					coordinateDimension.equals(pred) || isEmpty.equals(pred) 			||
					isSimple.equals(pred) 		     || hasSerialization.equals(pred)   ||
					asWKT.equals(pred) 				 || asGML.equals(pred)) {
			
			writeTriple(subj, TYPE, Geometry);
			writeTriple(subj, TYPE, SpatialObject);
		}
	}

	protected void writeTriple(String subj, String pred, String obj) {
		String triple = "<"+subj+"> <"+pred+"> <"+obj+"> .\n";
		//logger.info(triple);
		triples.append(triple);
		numTriples++;
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
