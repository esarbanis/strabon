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

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

import eu.earthobservatory.constants.GeoConstants;

public class GeosparqlRDFHandlerBase extends RDFHandlerBase {
	
	public static String GEO 		= GeoConstants.GEO;
	public static String GML 		= GeoConstants.GML_OGC;
	public static String SF	 		= GeoConstants.SF;
	public static String RDF_TYPE 	= "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	public static String SpatialObject 			= GEO + "SpatialObject";
	public static String Feature 				= GEO + "Feature";
	public static String Geometry				= GEO + "Geometry";
	public static String hasGeometry 			= GEO + "hasGeometry";
	public static String hasDefaultGeometry		= GEO + "hasDefaultGeometry";
	
	public static String dimension				= GEO + "dimension";
	public static String coordinateDimension	= GEO + "coordinateDimension";
	public static String spatialdimension		= GEO + "spatialDimension";
	public static String isEmpty				= GEO + "isEmpty";
	public static String isSimple				= GEO + "isSimple";
	
	public static String hasSerialization		= GEO + "hasSerialization";
	public static String asWKT					= GEO + "asWKT";
	public static String asGML					= GEO + "asGML";
	
	public static List <String> ogc_sf= Arrays.asList("Geometry", "Point", "Curve", "Surface", "GeometryCollection", "LineString", "Polygon", "MultiSurface", "MultiCurve",
			            "MultiPoint", "Line", "LinearRing", "MultiPolygon","MultiLineString");
	
	public static List <String> GM_Objects= Arrays.asList("GM_Complex", "GM_Agreggate", "GM_Primitive", "GM_Composite", "GM_MultiPrimitive",
			"GM_Point", "GM_OrientablePrimitive","GM_OrientableCurve","GM_OrientableSurface", "GM_Curve","GM_Surface","GM_Solid",
			 "GM_CompositeCurve", "GM_CompositeSurface", "GM_CompositeSolid", "GM_Multipoint", "GM_MultiCurve", "GM_MultiSurface", "GM_MultiSolid");
	
	public static List <String> geometryDomainList = Arrays.asList(dimension, coordinateDimension, spatialdimension,isEmpty, isSimple, asWKT, asGML);
	
	public static String WKTLiteral	= GeoConstants.WKTLITERAL;
	public static String GMLLiteral	= GeoConstants.GMLLITERAL;
	
	public static List <String> rcc8 = Arrays.asList(GEO+"rcc8eq",GEO+"rcc8dc",GEO+"rcc8ec",GEO+"rcc8po",
			GEO+"rcc8tppi", GEO+"rcc8tpp",GEO+ "rcc8ntpp", GEO+"rcc8ntpp");
	
	//loose check: tha elegxw an arxizei apo eh- i apo sf- i apo rcc8- (den einai ola tou rcc8)
	
	private StringBuffer triples = new StringBuffer(1024);
	
	/** 
	 * The number of triples that the "triples" object above contains.
	 */
	private int ntriples = 0;
	
	public StringBuffer getTriples()
	{
		return triples;
	}
	
	public List <String> getrcc8()
	{
		return rcc8;
	}
	
	public List <String> getgeometryDomainList()
	{
		return geometryDomainList;
	}
	
	@Override
	public void startRDF() { 
		triples.append("\n");
	}

	public int getNumberOfTriples() {
		return ntriples;
	}
	
	@Override
	public void handleStatement(Statement st)
	{
		String subject = st.getSubject().toString();
		String predicate = st.getPredicate().toString();
		String object = st.getObject().toString();
		
		if(predicate.startsWith("http://www.opengis.net/ont/geosparql#sf")||predicate.startsWith(GEO+"eh")|| 
				rcc8.contains(predicate))
		{
			String triple = "<"+subject+ "> <"+ RDF_TYPE +"> <"+ SpatialObject+ "> .\n" +
					"<"+object+ "> <"+ RDF_TYPE +"> <"+ SpatialObject+ "> .\n" ;
			triples.append(triple);
			ntriples++;
		}
		if(predicate.equals(RDF_TYPE)&&(object.equals(Feature) || object.equals(Geometry) ))
		{
			String triple = "<"+subject+ "> <"+ RDF_TYPE +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
			ntriples++;
		}
		
		if(predicate.equals(hasGeometry))
		{
			String triple = "<"+subject+ "> <"+ RDF_TYPE +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> <"+ RDF_TYPE +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> <"+ RDF_TYPE +"> <"+ SpatialObject + "> .\n" +
					"<"+	object+ "> <"+ RDF_TYPE +"> <"+ SpatialObject + "> .\n";
			triples.append(triple);
			ntriples++;
		}
		else if(predicate.equals(hasDefaultGeometry))
		{
			String triple = "<"+subject+ "> <"+ RDF_TYPE +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> <"+ RDF_TYPE +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> <"+ RDF_TYPE +"> <"+ SpatialObject + "> .\n" +
					"<"+	object+ "> <"+ RDF_TYPE +"> <"+ SpatialObject + "> .\n";
			triples.append(triple);
			ntriples++;
		}
		
		if(geometryDomainList.contains(predicate))
		{
			String triple = "<"+subject+ "> <"+ RDF_TYPE +"> <"+ Geometry+ "> .\n" +
					"<"+subject+ "> <"+ RDF_TYPE +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
			ntriples++;
		}
		if (predicate.equals(RDF_TYPE)) {
			if (object.equals(GML + "GM_Complex")
					|| object.equals(GML + "GM_Aggregate")
					|| object.equals(GML + "GM_Primitive")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(GML + "GM_Composite")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + GML + "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_MultiPrimitive")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Aggregate" + "> .\n" + "<" + subject + "> <"
						+ RDF_TYPE + "> <" + GML + "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_Point")
					|| object.equals(GML + "GM_OrientablePrimitive")
					|| object.equals(GML + "GM_Solid")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Primitive" + "> .\n" + "<" + subject + "> <"
						+ RDF_TYPE + "> <" + GML + "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_OrientableCurve")
					|| object.equals(GML + "GM_OrientableSurface")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + GML + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_Curve")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Aggregate" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE +"> <" + GML + "GM_OrientableCurve" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML+ "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_Surface")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML+ "GM_Aggregate" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML + "GM_OrientableSurface" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_CompositeCurve")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Aggregate" + "> .\n" + "<" + subject + "> <"
						+ RDF_TYPE + "> <" + GML + "GM_OrientableCurve" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + GML + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + GML + "GM_Composite" + "> .\n" + "<"
						+ subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_CompositeSurface")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_OrientableSurface" + "> .\n" +

						"<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + GML + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + GML + "GM_Composite" + "> .\n" + "<"
						+ subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_CompositeSolid")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Solid" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + GML + "GM_Primitive" + "> .\n" + "<"
						+ subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Complex"
						+ "> .\n" + "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_Composite" + "> .\n" + "<" + subject + "> <"
						+ RDF_TYPE + "> <" + GML + "GM_Object" + "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(GML + "GM_MultiPoint")
					|| object.equals(GML + "GM_MultiCurve")
					|| object.equals(GML + "GM_MultiSurface")
					|| object.equals(GML + "GM_MultiSolid")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + GML
						+ "GM_MultiPrimitive" + "> .\n" + "<" + subject + "> <"
						+ RDF_TYPE + "> <" + GML + "GM_Aggregate" + "> .\n" + "<"
						+ subject + "> <" + RDF_TYPE + "> <" + GML + "GM_Object"
						+ "> .\n";
				triples.append(triple);
				ntriples++;

			}
			if (object.equals(SF + "Point") || object.equals(SF + "Curve")
					|| object.equals(SF + "Surface")
					|| object.equals(SF + "GeometryCollection")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "LineString")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "Curve" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "Line") || object.equals(SF + "LinearRing")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "Curve" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + SF + "LineString" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "Polygon")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "Surface" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "MultiSurface")
					|| object.equals(SF + "MultiCurve")
					|| object.equals(SF + "MultiPoint")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "GeometryCollection" + "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "MultiPolygon")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "MultiSurface" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + SF + "GeometryCollection"
						+ "> .\n";
				triples.append(triple);
				ntriples++;
			}
			if (object.equals(SF + "MultiLineString")) {
				String triple = "<" + subject + "> <" + RDF_TYPE + "> <" + SF
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + RDF_TYPE
						+ "> <" + SF + "MultiCurve" + "> .\n" + "<" + subject
						+ "> <" + RDF_TYPE + "> <" + SF + "GeometryCollection"
						+ "> .\n";
				triples.append(triple);
				ntriples++;
			}
		}
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
        
		String gmltext= "<http://example.org/GM_MultiSolid> <"+RDF_TYPE+"> <"+GML+"GM_Object> .\n"; 
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
