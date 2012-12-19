package eu.earthobservatory.runtime.generaldb;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;
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
    	String strdf = TemporalConstants.PERIOD;
    	String period = "http://strdf.di.uoa.gr/ontology#period";
    	validTimeLiteral=sb;
    	int i2=0; 
    	
    	if(sb.toString().contains("^^<"+TemporalConstants.PERIOD)||
    			sb.toString().contains("^^http://strdf.di.uoa.gr/ontology#period>"))
     	{	
    	
     	String[] splits = sb.toString().split(",");
     	int i1 = splits[0].indexOf('[');
     	if (splits[1].contains("]"))
     		 i2 = splits[1].indexOf(']');
     	else if (splits[1].contains(")"))
     	{
     		i2 = splits[1].indexOf(')');
     	}
     	String element1 = splits[0].substring(++i1);
     	String element2 = splits[1].substring(0,i2);
     	//System.out.println("element2"+element2);
        DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.SHORT);
        String startDate=null; 
        String endDate=null;
        
        int syn=0;
        
        if (element1.contains("+"))
        {
        	syn = element1.indexOf('+');
         	if (syn<0)
         		syn = element1.indexOf('+');
        
         	startDate = element2.substring(0,syn);
        }
        else
        {
        	startDate = element1;
        }
     	//System.out.println("element1 = "+element1);
     	
     	
     	if (element2.contains("+"))
        {
        	syn = element2.indexOf('+');
         	if (syn<0)
         		syn = element2.indexOf('+');
         	endDate = element2.substring(0,syn);
         	
        }
     	else
     	{
     		endDate = element2;
     	}
     	
     	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	Date start = format.parse(startDate);
     	//System.out.println("start date:"+startDate.toString());
     	Date end = format.parse(endDate);
     	//System.out.println("End date:"+ endDate.toString());
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
        	//System.out.println("PARSEURI");
            c = parseUriRef(c, sb);
            context = createURI(sb.toString());
        } else if (c == '_') {
            // subject is a bNode
            c = parseNodeID(c, sb);
        	//System.out.println("PARSENODE");
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
