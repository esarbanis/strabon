package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import org.junit.Assert;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

import net.fortytwo.sesametools.nquads.NQuadsParser;
import net.fortytwo.sesametools.nquads.NQuadsFormat;

public class NQuadsTranslator {
	
    private NQuadsParser parser;
    private QuadRDFHandler handler;
    private StringBuffer handledTriples;
    

    
    public NQuadsTranslator() {
		super();
		this.parser = new NQuadsParser();
		this.handler = new QuadRDFHandler();
		this.handledTriples = new StringBuffer(1024);
	}

	public StringBuffer getHandledTriples() {
		return handledTriples;
	}

	public Collection<Statement>  translate(InputStream is,String baseURI)
    {
    	Collection<Statement> statements = null; 

    	parser.setRDFHandler(handler);
    	try {
			parser.parse(is, "http://test.base.uri");
		    handledTriples = handler.getTriples();
	        System.out.println("HANDLED TRIPLES: "+handledTriples.toString());
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	statements= handler.getStatements();
    	return statements;
    }

}
