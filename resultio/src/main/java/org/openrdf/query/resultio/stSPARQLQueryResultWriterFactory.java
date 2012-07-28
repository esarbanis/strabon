package org.openrdf.query.resultio;

import java.io.OutputStream;

import org.openrdf.query.resultio.sparqlgeojson.stSPARQLResultsGeoJSONWriterFactory;
import org.openrdf.query.resultio.sparqlhtml.stSPARQLResultsHTMLWriterFactory;
import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMZWriterFactory;
import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriterFactory;
import org.openrdf.query.resultio.sparqlxml.stSPARQLResultsXMLWriterFactory;
import org.openrdf.query.resultio.text.stSPARQLResultsTSVWriterFactory;

/**
 * This is a factory class for creating stSPARQLQueryResultWriter
 * instances according to a format in {@link org.openrdf.query.resultio.sparqlxml.Format}.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLQueryResultWriterFactory {

	private static TupleQueryResultWriterFactory html = new stSPARQLResultsHTMLWriterFactory();
	private static TupleQueryResultWriterFactory xml = new stSPARQLResultsXMLWriterFactory();
	private static TupleQueryResultWriterFactory kml = new stSPARQLResultsKMLWriterFactory();
	private static TupleQueryResultWriterFactory kmz = new stSPARQLResultsKMZWriterFactory();
	private static TupleQueryResultWriterFactory tsv = new stSPARQLResultsTSVWriterFactory();
	private static TupleQueryResultWriterFactory geojson = new stSPARQLResultsGeoJSONWriterFactory();
	
	public static TupleQueryResultWriter createstSPARQLQueryResultWriter(Format format, OutputStream out) {
		TupleQueryResultWriter writer = null;
		
		switch (format) {
			case DEFAULT:
				writer = tsv.getWriter(out);
				break;
				
			case XML:
				writer = xml.getWriter(out);
				break;
				
			case KML:
				writer = kml.getWriter(out);
				break;
				
			case KMZ:
				writer = kmz.getWriter(out);
				break;
				
			case GEOJSON:
				writer = geojson.getWriter(out);
				break;

			case EXP:
				// TODO: add
				break;
				
			case HTML:
				writer = html.getWriter(out);
				break;
			
			case TSV:
				writer = tsv.getWriter(out);
		}
		
		return writer;
	}
	
}
