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
    private TranslateRDFHandler rdfHandler;
    
    private class TranslateRDFHandler extends StatementCollector {

        

        @Override
        public void startRDF() throws RDFHandlerException {
            super.startRDF();
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            super.endRDF();
        }

        @Override
        public void handleStatement(Statement statement) {
            super.handleStatement(statement);
            //logger.debug(statement.toString());
        }

		public TranslateRDFHandler() {
			super();
		}
        
        

    }
    
    public NQuadsTranslator() {
		super();
		this.parser = new NQuadsParser();
		this.rdfHandler = new TranslateRDFHandler();
	}

	public Collection<Statement>  translate(InputStream is,String baseURI)
    {
    	Collection<Statement> statements = null; 
    	TranslateRDFHandler handler = new TranslateRDFHandler();
    	parser.setRDFHandler(handler);
    	try {
			parser.parse(is, "http://test.base.uri");
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
