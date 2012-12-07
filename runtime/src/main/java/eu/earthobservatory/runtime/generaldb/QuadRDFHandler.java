/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 * 
 *  @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *  
 *  This class handles every quadruple statement by using the fourth element as a named graph and
 *  creates another triple to annotate the former statement with valid time in the default graph
 *
 */
package eu.earthobservatory.runtime.generaldb;

import java.text.ParseException;

//import org.junit.Assert;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
//import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

public class QuadRDFHandler extends StatementCollector {
	        
	        private StringBuffer triples = new StringBuffer(4096);

	   
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
	         	{	
	         	    NQuadsParser parser = new NQuadsParser();
	         		try {
	         			String context = st.getContext().toString();
						 String validPeriod= context;
						 if(!context.contains(","))
						 {
							 int i = context.indexOf('"')+1;
							 int j = context.lastIndexOf('"');
							 validPeriod = "\"[" + context.substring(i,j)+","+context.substring(i,j) + "]\"^^<http://strdf.di.uoa.gr/ontology#validTime>"; 

							// validPeriod = context.replace("]",","+context.substring(i, j)+"]");
						 }
					 Resource graph = parser.createValidTimeURI(validPeriod);
					 
					 String triple = "<"+graph.toString()+">"+  " <http://strdf.di.uoa.gr/ontology#hasValidTime> "+ validPeriod+ " .\n" ;
					 if (!triples.toString().contains(triple))
					 {
						 triples.append(triple);
						 System.out.println("TRIPLE:"+triple);

					 }
					
						 
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


