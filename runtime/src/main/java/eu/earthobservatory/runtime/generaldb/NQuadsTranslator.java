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
 *This class can be considered as a wrapper which makes the conversion from quadruples into valid time
 *annotated triples, with valid time annotation being stored as a named graph.
 */

package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;


import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

//import net.fortytwo.sesametools.nquads.NQuadsParser;
//import net.fortytwo.sesametools.nquads.NQuadsFormat;

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
