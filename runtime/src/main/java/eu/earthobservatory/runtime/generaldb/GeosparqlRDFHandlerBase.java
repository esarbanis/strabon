package eu.earthobservatory.runtime.generaldb;

import java.io.StringReader;
import java.util.List;
import java.util.Arrays;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

public class GeosparqlRDFHandlerBase extends RDFHandlerBase {
	
	public static String geonamespace = "http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/";
	public static String type = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
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
	public static List <String> geometryDomainList = Arrays.asList(dimension, coordinateDimension, spatialdimension,isEmpty, isSimple, is3D);
	public static String WKTLiteral=   geonamespace + "WKTLiteral";
	public static String GMLLiteral=   geonamespace + "GMLLiteral";
	public static String asWKT=   geonamespace + "asWKT";
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
			String triple = "<"+subject+ "> "+ type +"> <"+ SpatialObject+ "> .\n" +
					"<"+object+ "> "+ type +"> <"+ SpatialObject+ "> .\n" ;
			triples.append(triple);
		}
		if(subject.equals(Feature) || subject.equals(Geometry) )
		{
			String triple = "<"+subject+ "> "+ type +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
		}
		if(predicate.equals(hasGeometry))
		{
			String triple = "<"+subject+ "> "+ type +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> "+ type +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> "+ type +"> <"+ SpatialObject + "> .\n" +
					"<"+	object+ "> "+ type +"> <"+ SpatialObject + "> .\n";
			triples.append(triple);
		}
		if(predicate.equals(defaultGeometry))
		{
			String triple = "<"+subject+ "> "+ type +"> <"+ Feature+ "> .\n" +
					"<"+object+ "> "+ type +"> <"+ Geometry+ "> .\n" +
					"<"+	subject+ "> "+ type +"> <"+ SpatialObject + "> .\n"+
			"<"+	subject+ "> <"+ hasGeometry +"> <"+ object + "> .\n";
			triples.append(triple);
		}
		if(geometryDomainList.contains(predicate))
		{
			String triple = "<"+subject+ "> <"+ type +"> <"+ Geometry+ "> .\n" +
					"<"+subject+ "> "+ type +"> <"+ SpatialObject+ "> .\n";
			triples.append(triple);
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

		StringReader reader = new StringReader(text);

		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();

		handler.startRDF();
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
		handler.endRDF();

		reader.close();	

		System.out.println("Original triples: " + text);
		//System.out.println("Geometry domain list: " + handler.getgeometryDomainList());
		System.out.println("New triples: " + handler.getTriples());
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
