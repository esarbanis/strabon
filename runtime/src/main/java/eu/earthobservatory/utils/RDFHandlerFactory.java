/**
 * 
 */
package eu.earthobservatory.utils;

import java.io.OutputStream;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.binary.BinaryRDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * Factory class for creating instances of RDFHandler class
 * based on the given format, which should be one of the formats
 * mentioned in {@link org.openrdf.rio.RDFFormat} class, and 
 * an OutputStream to which the handler should write to.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class RDFHandlerFactory {

	public static RDFHandler createRDFHandler(String format, OutputStream out) {
		RDFHandler handler = null;
		RDFFormat rdfFormat = RDFFormat.valueOf(format);
		
		if (rdfFormat == RDFFormat.NTRIPLES || rdfFormat == null) {
			handler = new NTriplesWriter(out);
			
		} else if (rdfFormat == RDFFormat.N3) {
			handler = new N3Writer(out);
			
		} else if (rdfFormat == RDFFormat.RDFXML) {
			handler = new RDFXMLPrettyWriter(out);
			
		} else if (rdfFormat == RDFFormat.TURTLE) {
			handler = new TurtleWriter(out);
			
		} else if (rdfFormat == RDFFormat.TRIG) {
			handler = new TriGWriter(out);
			
		} else if (rdfFormat == RDFFormat.TRIX) {
			handler = new TriXWriter(out);
			
		} else if (rdfFormat == RDFFormat.BINARY) {
			handler = new BinaryRDFWriter(out);
			
		}
		
		return handler;
	}
}