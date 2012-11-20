package eu.earthobservatory.runtime.generaldb;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.naming.StringRefAddr;
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
 *  based on code provided by ua Shinavier (http://fortytwo.net).  Builds on code by Aduna.
 *
 */

/**
 * RDFParser implementation for the N-Quads RDF format.
 * <p/>
 * Changes made to Aduna's N-Triple parser:
 * 1) "final" removed from NTriplesParser.getRDFFormat
 * 2) private member variables made public: reader, lineno, subject, predcate, object
 * 3) private methods: skipWhitespace, skipLine, parseSubject, parsePredicate, parseObject, throwEOFException
 *
 */
public class NQuadsParser extends ModifiedNTriplesParser {
    protected Resource context;
    protected String validTimeLiteral;

    /*
    // FIXME: delete me
    public static void main(final String[] args) throws Exception {
        String baseURI = "http://example.org/bogusBaseURI/";

        Sail sail = new NativeStore(new File("/tmp/btcSmallNativeStore"));
        sail.initialize();
        try {
            Repository repo = new SailRepository(sail);
            RepositoryConnection conn = repo.getConnection();
            try {
                InputStream is = new FileInputStream(
                        new File("/Users/josh/datasets/btc/btc-2009-small.nq"));
                try {
                    RDFParser parser = new NQuadsParser();
                    parser.setRDFHandler(new RDFInserter(conn));
                    parser.parse(is, baseURI);
                } finally {
                    is.close();
                }
            } finally {
                conn.close();
            }
        } finally {
            sail.shutDown();
        }
    }
    */

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.NQUADS;
    }

    @Override
    public void parse(final InputStream inputStream,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream can not be 'null'");
        }
        // Note: baseURI will be checked in parse(Reader, String)

        try {
            parse(new InputStreamReader(inputStream, "US-ASCII"), baseURI);
        } catch (UnsupportedEncodingException e) {
            // Every platform should support the US-ASCII encoding...
            throw new RuntimeException(e);
        }
    }

    @Override
    public void parse(final Reader reader,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader can not be 'null'");
        }
        if (baseURI == null) {
            throw new IllegalArgumentException("base URI can not be 'null'");
        }

        rdfHandler.startRDF();

        this.reader = reader;
        lineNo = 1;

        reportLocation(lineNo, 1);

        try {
            int c = reader.read();
            c = skipWhitespace(c);

            while (c != -1) {
                if (c == '#') {
                    // Comment, ignore
                    c = skipLine(c);
                } else if (c == '\r' || c == '\n') {
                    // Empty line, ignore
                    c = skipLine(c);
                } else {
                    c = parseQuad(c);
                }

                c = skipWhitespace(c);
            }
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            clear();
        }

        rdfHandler.endRDF();
    }

    private int parseQuad(int c)
            throws IOException, RDFParseException, RDFHandlerException, ParseException {
        
        boolean ignoredAnError = false;
        try
        {
            c = parseSubject(c);
    
            c = skipWhitespace(c);
    
            c = parsePredicate(c);
    
            c = skipWhitespace(c);
    
            c = parseObject(c);
    
            c = skipWhitespace(c);
    
            // Context is not required
            if (c != '.') {
                c = parseContext(c);
                c = skipWhitespace(c);
            }
            if (c == -1) {
                throwEOFException();
            } else if (c != '.') {
                reportFatalError("Expected '.', found: " + (char) c);
            }
    
            c = assertLineTerminates(c);
        }
        catch(RDFParseException rdfpe)
        {
            if(stopAtFirstError())
            {
                throw rdfpe;
            }
            else
            {
                ignoredAnError = true;
            }
        }
        
        c = skipLine(c);

        if(!ignoredAnError)
        {
            Statement st = createStatement(subject, predicate, object, context);
            rdfHandler.handleStatement(st);
        }
        
        subject = null;
        predicate = null;
        object = null;
        context = null;

        return c;
    }

    public Resource createValidTimeURI(String sb) throws ParseException, RDFParseException
    {
    	String strdf = "http://strdf.di.uoa.gr/ontology#validTime";
    	validTimeLiteral=sb;
    	if(sb.toString().contains("^^<http://strdf.di.uoa.gr/ontology#validTime>"))
     	{	
     	String[] splits = sb.toString().split(",");
     	int i = splits[0].indexOf('[');
     	String element1 = splits[0].substring(++i);
     	String[] splash2 = splits[1].split("]");
        DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.SHORT);
     	int syn = element1.indexOf('+');
     	String startDate = element1.substring(0,syn);
    	syn = element1.indexOf('+');
     	String endDate = element1.substring(0,syn);
     	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	Date start = format.parse(startDate);
     	Date end = format.parse(endDate);
        String uri = strdf+"/"+ startDate+"_"+ endDate+ "_" +format.getTimeZone().getID(); 
        Resource cont = createURI(uri);
        return cont;
    }
    	return null;
    }
    
    protected int parseContext(int c)
            throws IOException, RDFParseException, ParseException {
        // FIXME: context (in N-Quads) can be a literal
        StringBuilder sb = new StringBuilder(100);
        
        // subject is either an uriref (<foo://bar>) or a nodeID (_:node1)
        if (c == '<') {
            // subject is an uriref
        	System.out.println("PARSEURI");
            c = parseUriRef(c, sb);
            context = createURI(sb.toString());
        } else if (c == '_') {
            // subject is a bNode
            c = parseNodeID(c, sb);
        	System.out.println("PARSENODE");
            context = createBNode(sb.toString());
        }else if(c == '"'){
        	c = parseLiteral(c, sb);
        	validTimeLiteral=sb.toString();
        	context = createURI(sb.toString());
        	
        }else if (c == -1) {
            throwEOFException();
        } else {
            reportFatalError("Expected '<' or '_', found: " + (char) c);
        }

        return c;
    }
}