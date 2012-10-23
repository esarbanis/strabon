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
	         		String validTimeLiteral = st.getContext().toString();
						String triple = st.getContext().toString() + " <http://strdf.di.uoa.gr/ontology#hasValidTime> "+ st.getContext().toString()+ " .\n" ;
					    triples.append(triple);
	         	}
	            super.handleStatement(st);
	        }

	    }


