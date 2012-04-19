package eu.earthobservatory.runtime.generaldb;

import java.io.StringReader;
import java.util.List;
import java.util.Arrays;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

public class GeosparqlRDFHandlerBase extends RDFHandlerBase {
	
	public static String geonamespace = "http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/";
	public static String gml="http://www.opengis.net/def/geometryType/OGC-GML/3.2/";
	public static String sf="http://www.opengis.net/def/geometryType/OGC-SF/1.0/";
	public static String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static String SpatialObject= geonamespace + "SpatialObject";
	public static String Feature = geonamespace + "Feature";
	public static String Geometry= geonamespace + "Geometry";
	public static String hasGeometry = geonamespace + "hasGeometry";
	public static String defaultGeometry = geonamespace + "defaultGeometry";
	public static String dimension=   geonamespace + "dimension";
	public static String coordinateDimension=   geonamespace + "coordinateDimension";
	public static String spatialdimension=   geonamespace + "spatialdimension";
	public static String isEmpty=   geonamespace + "isEmpty";
	public static String isSimple=   geonamespace + "isSimple";
	public static String is3D=   geonamespace + "is3D";
	public static String asWKT=   geonamespace + "asWKT";
	public static String asGML=   geonamespace + "asGML";
	public static List <String> ogc_sf= Arrays.asList("Geometry", "Point", "Curve", "Surface", "GeometryCollection", "LineString", "Polygon", "MultiSurface", "MultiCurve",
			            "MultiPoint", "Line", "LinearRing", "MultiPolygon","MultiLineString");
	public static List <String> GM_Objects= Arrays.asList("GM_Complex", "GM_Agreggate", "GM_Primitive", "GM_Composite", "GM_MultiPrimitive",
			"GM_Point", "GM_OrientablePrimitive","GM_OrientableCurve","GM_OrientableSurface", "GM_Curve","GM_Surface","GM_Solid",
			 "GM_CompositeCurve", "GM_CompositeSurface", "GM_CompositeSolid", "GM_Multipoint", "GM_MultiCurve", "GM_MultiSurface", "GM_MultiSolid");
	public static List <String> geometryDomainList = Arrays.asList(dimension, coordinateDimension, spatialdimension,isEmpty, isSimple, is3D,asWKT, asGML);
	public static String WKTLiteral=   geonamespace + "WKTLiteral";
	public static String GMLLiteral=   geonamespace + "GMLLiteral";
	public static List <String> rcc8 = Arrays.asList(geonamespace+"rcc8-eq",geonamespace+"rcc8-dc",geonamespace+"rcc8-ec",geonamespace+"rcc8-po",
			geonamespace+"rcc8-tppi", geonamespace+"rcc8-tpp",geonamespace+ "rcc8-ntpp", geonamespace+"rcc8-ntpp");
	
	//loose check: tha elegxw an arxizei apo eh- i apo sf- i apo rcc8- (den einai ola tou rcc8)
	
	private StringBuffer triples = new StringBuffer(1024);
	
	public StringBuffer getTriples()
	{
		return triples;
	};
	
	public List <String> getrcc8()
	{
		return rcc8;
	}
	
	public List <String> getgeometryDomainList()
	{
		return geometryDomainList;
	}
	
	public void startRDF() { triples.append("\n");}; 

	public void endRDF() {};
	
	
	public void handleStatement(Statement st)
	{
		String subject = st.getSubject().toString();
		String predicate = st.getPredicate().toString();
		String object = st.getObject().toString();
		
		if(predicate.startsWith("http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/sf-")||predicate.startsWith(geonamespace+"eh-")|| 
				rcc8.contains(predicate))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ SpatialObject+ "> .\n" +
					"<"+object+ "> <"+ type +"> <"+ SpatialObject+ "> .\n" ;
			triples.append(triple);
		}
		if(predicate.equals(type)&&(object.equals(Feature) || object.equals(Geometry) ))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
		}
		if(predicate.equals(hasGeometry))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> <"+ type +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> <"+ type +"> <"+ SpatialObject + "> .\n" +
					"<"+	object+ "> <"+ type +"> <"+ SpatialObject + "> .\n";
			triples.append(triple);
		}
		if(predicate.equals(defaultGeometry))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> <"+ type +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> <"+ type +"> <"+ SpatialObject + "> .\n"+
			"<"+	subject+ "> <"+ hasGeometry +"> <"+ object + "> .\n";
			triples.append(triple);
		}
		if(geometryDomainList.contains(predicate))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ Geometry+ "> .\n" +
					"<"+subject+ "> <"+ type +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
		}
		if (predicate.equals(type)) {
			if (object.equals(gml + "GM_Complex")
					|| object.equals(gml + "GM_Aggregate")
					|| object.equals(gml + "GM_Primitive")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Object" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(gml + "GM_Composite")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + gml + "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_MultiPrimitive")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Aggregate" + "> .\n" + "<" + subject + "> <"
						+ type + "> <" + gml + "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_Point")
					|| object.equals(gml + "GM_OrientablePrimitive")
					|| object.equals(gml + "GM_Solid")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Primitive" + "> .\n" + "<" + subject + "> <"
						+ type + "> <" + gml + "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_OrientableCurve")
					|| object.equals(gml + "GM_OrientableSurface")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + gml + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_Curve")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Aggregate" + "> .\n"
						+ "<" + subject + "> <" + type +"> <" + gml + "GM_OrientableCurve" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml+ "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_Surface")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml+ "GM_Aggregate" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml + "GM_OrientableSurface" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml + "GM_OrientablePrimitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_CompositeCurve")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Aggregate" + "> .\n" + "<" + subject + "> <"
						+ type + "> <" + gml + "GM_OrientableCurve" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + gml + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + gml + "GM_Composite" + "> .\n" + "<"
						+ subject + "> <" + type + "> <" + gml + "GM_Object"
						+ "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_CompositeSurface")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_OrientableSurface" + "> .\n" +

						"<" + subject + "> <" + type + "> <" + gml
						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + gml + "GM_Primitive" + "> .\n"
						+ "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Complex" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + gml + "GM_Composite" + "> .\n" + "<"
						+ subject + "> <" + type + "> <" + gml + "GM_Object"
						+ "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_CompositeSolid")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Solid" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + gml + "GM_Primitive" + "> .\n" + "<"
						+ subject + "> <" + type + "> <" + gml + "GM_Complex"
						+ "> .\n" + "<" + subject + "> <" + type + "> <" + gml
						+ "GM_Composite" + "> .\n" + "<" + subject + "> <"
						+ type + "> <" + gml + "GM_Object" + "> .\n";
				triples.append(triple);

			}
			if (object.equals(gml + "GM_MultiPoint")
					|| object.equals(gml + "GM_MultiCurve")
					|| object.equals(gml + "GM_MultiSurface")
					|| object.equals(gml + "GM_MultiSolid")) {
				String triple = "<" + subject + "> <" + type + "> <" + gml
						+ "GM_MultiPrimitive" + "> .\n" + "<" + subject + "> <"
						+ type + "> <" + gml + "GM_Aggregate" + "> .\n" + "<"
						+ subject + "> <" + type + "> <" + gml + "GM_Object"
						+ "> .\n";
				triples.append(triple);

			}
			if (object.equals(sf + "Point") || object.equals(sf + "Curve")
					|| object.equals(sf + "Surface")
					|| object.equals(sf + "GeometryCollection")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "LineString")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "Curve" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "Line") || object.equals(sf + "LinearRing")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "Curve" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + sf + "LineString" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "Polygon")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "Surface" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "MultiSurface")
					|| object.equals(sf + "MultiCurve")
					|| object.equals(sf + "MultiPoint")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "GeometryCollection" + "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "MultiPolygon")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "MultiSurface" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + sf + "GeometryCollection"
						+ "> .\n";
				triples.append(triple);
			}
			if (object.equals(sf + "MultiLineString")) {
				String triple = "<" + subject + "> <" + type + "> <" + sf
						+ "Geometry" + "> .\n" + "<" + subject + "> <" + type
						+ "> <" + sf + "MultiCurve" + "> .\n" + "<" + subject
						+ "> <" + type + "> <" + sf + "GeometryCollection"
						+ "> .\n";
				triples.append(triple);
			}
		}

		
	
				
		
		//triples.append("niania\n");
		
	}
	

	public static void main(String[] args) throws Exception {		
		NTriplesParser parser = new NTriplesParser();
		parser.setVerifyData(true);

		String text = 
				"<http://example.org/rcc8Obj1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/rcc8-eq> <http://example.org/rcc8Obj2> . " +
				"<http://example.org/simpleGeometry1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/isEmpty> _:nai . \n"+
		"<http://example.org/ForestArea1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/defaultGeometry> _:b2 . \n"+
		"<http://example.org/SpatialObject1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/eh-intersects> <http://example.org/SpatialObject2> . \n";
        
		String gmltext= "<http://example.org/GM_MultiSolid> <"+type+"> <"+gml+"GM_Object> .\n"; 
		String sftext= "<http://example.org/Line> <"+type+"> <"+sf+"Geometry> .\n"; 
		
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
