package eu.earthobservatory.runtime.generaldb;

import java.text.ParseException;

import org.junit.Assert;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

public class QuadRDFHandler extends StatementCollector {
	        
	        private StringBuffer triples = new StringBuffer(1024);

	   
	        @Override
	        public void startRDF() throws RDFHandlerException {
	            super.startRDF();
	            triples.append("\n");
	        }
	        
	        public StringBuffer getTriples()
	    	{
	    		return triples;
	    	};
	    	
	        @Override
	        public void endRDF() throws RDFHandlerException {
	            super.endRDF();
	        }
	        
	        @Override
	        public void handleStatement(Statement st) {
	            //super.handleStatement(st);
	            if(st.getContext().toString().contains("^^<http://strdf.di.uoa.gr/ontology#validTime>"))
	         	{	System.out.println("THIS IS A VALID TIME LITERAL");
	         	    NQuadsParser parser = new NQuadsParser();
	         		try {
					 Resource graph = parser.createValidTimeURI(st.getContext().toString());
					 String triple = graph.toString() + " <http://strdf.di.uoa.gr/ontology#hasValidTime> "+ st.getContext().toString()+ " .\n" ;
					    triples.append(triple);
					} catch (RDFParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
	         	}
	            super.handleStatement(st);
	        }

	    }


