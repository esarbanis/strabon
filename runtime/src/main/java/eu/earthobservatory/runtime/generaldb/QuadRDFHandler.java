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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

//import org.junit.Assert;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
//import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

public class QuadRDFHandler extends StatementCollector {
	        
	        private SailRepositoryConnection connection= null;
	        
	        
	   
	        public QuadRDFHandler(SailRepositoryConnection connection) {
				super();
				this.connection = connection;
			}

			@Override
	        public void startRDF() throws RDFHandlerException {
	            super.startRDF();
	        }
	             
	    	
	        @Override
	        public void endRDF() throws RDFHandlerException {
	            super.endRDF();
	        }
	        
	        @Override
	        public void handleStatement(Statement st) {
	            if(st.getContext().toString().contains("^^<"+TemporalConstants.PERIOD)||st.getContext().toString().contains("^^<"+TemporalConstants.INSTANT))
	         	{	
	         	    NQuadsParser parser = new NQuadsParser();
	         		try {
	         			String context = st.getContext().toString();
						 String validPeriod= context;
						 if(context.contains("^^<"+TemporalConstants.INSTANT))
						 {
							 int i = context.indexOf('"')+1;
							 int j = context.lastIndexOf('"');
							 validPeriod = "\"[" + context.substring(i,j)+","+context.substring(i,j) + "]\"^^<"+TemporalConstants.PERIOD+">"; 

						 }
					 Resource graph = parser.createValidTimeURI(validPeriod);
					 
					 String triple = "<"+graph.toString()+">"+  " <http://strdf.di.uoa.gr/ontology#hasValidTime> "+ validPeriod+ " .\n" ;
				
					 
					 try {
						//connection.add(new URIImpl("<"+graph.toString()+">"),new URIImpl(" <http://strdf.di.uoa.gr/ontology#hasValidTime>"), new URIImpl(validPeriod));
					   StringReader reader = new StringReader(triple);
						 connection.add(reader, "null", RDFFormat.NTRIPLES);
					 } catch (RepositoryException e) {
						// TODO Auto-generated catch block
						System.out.println("Error in QuadRDFHandler: could not store rewritter triple");
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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


