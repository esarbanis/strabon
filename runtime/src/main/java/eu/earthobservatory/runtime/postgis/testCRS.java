package eu.earthobservatory.runtime.postgis;
import org.openrdf.repository.RepositoryException;


import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;

public class testCRS {
	
	public static Strabon strabon;
	
	public static void main(String[] args) throws RDFParseException, RepositoryException, IOException, InvalidDatasetFormatFault, RDFHandlerException {
		try {
			strabon = new Strabon("tut","postgres","p1r3as", 5432, "localhost", true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			strabon.close();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String text = 
				"<http://example.org/rcc8Obj1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/rcc8-eq> <http://example.org/rcc8Obj2> . \n" +
				"<http://example.org/simpleGeometry1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/isEmpty> <http://example.org/nai> . \n"+
		"<http://example.org/ForestArea1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/defaultGeometry> <http://example.org/defaultgeom> . \n"+
		"<http://example.org/SpatialObject1> <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/eh-intersects> <http://example.org/SpatialObject2> . \n";

		String statement1= "<http://example.org/CoastLine4> <http://www.earthobservatory.eu/ontologies/noaOntology.owl#hasGeometry> " +
				"\"POLYGON((34.80 19.37,41.74 19.37,41.74 29.64 ,34.80 29.64,34.80 19.37));http://www.opengis.net/def/crs/EPSG/0/4326" +
				"\"^^<http://strdf.di.uoa.gr/ontology#WKT> .";
	String statement2= "<http://example.org/CoastLine5> <http://www.earthobservatory.eu/ontologies/noaOntology.owl#hasGeometry> " +
				"\"POLYGON((34.80 19.37,41.74 19.37,41.74 29.64 ,34.80 29.64,34.80 19.37));http://www.opengis.net/def/crs/EPSG/0/32630" +
				"\"^^<http://strdf.di.uoa.gr/ontology#WKT> .";
		String gml =  "<http://example.org/rcc8Obj1> <http://example.org/hasGeometry> \"<gml:Point> <gml:coordinates>45.67, 88.56</gml:coordinates> </gml:Point>\"^^<http://strdf.di.uoa.gr/ontology#GML> .\n";
				
		File file = new File ("/home/konstantina/Desktop/streason.nt");
		URL url = new URL("http://www.di.uoa.gr/~pms509/rdf-data/streason.nt");
		String fileBaseURI = "http://example#";
		String fileRDFFormat = "N3";
		String stringBaseURI = "http://example#";
		String stringRDFFormat = "NTRIPLES";
		try {
			strabon.storeInRepo(statement1, stringBaseURI, null, stringRDFFormat);
			//strabon.storeInRepo(statement2, stringBaseURI, null, stringRDFFormat);
			//strabon.storeInRepo(text, null, null, "NTRIPLES");
			//strabon.storeInRepo(file, null, null, fileRDFFormat);
			//strabon.storeInRepo(gml, null, null, fileRDFFormat);
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			strabon.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			strabon.close();
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			strabon.close();
			e.printStackTrace();
		} catch (InvalidDatasetFormatFault e) {
			// TODO Auto-generated catch block
			strabon.close();
			e.printStackTrace();
		}
		finally{
			strabon.close();
			System.out.println("connection closed.");
		}
		
	}

}
