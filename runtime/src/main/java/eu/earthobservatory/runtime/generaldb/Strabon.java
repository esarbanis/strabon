/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.Format;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultWriterFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.helpers.SailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.utils.RDFHandlerFactory;


public abstract class Strabon {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.Strabon.class);

	public static final String FORMAT_DEFAULT	= "";
	public static final String FORMAT_XML		= "XML";
	public static final String FORMAT_KML		= "KML";
	public static final String FORMAT_KMZ		= "KMZ";
	public static final String FORMAT_GEOJSON	= "GeoJSON";
	public static final String FORMAT_EXP		= "EXP";
	public static final String FORMAT_HTML		= "HTML";
	
	public static final String NEWLINE	= "\n";
	
	/**
	 * Connection details (shared with subclasses)
	 */
	protected String databaseName;
	protected String user;
	protected String password;
	protected int port;
	protected String serverName;
	
	protected SailBase db_store;
	private SailRepository repo1;
	private SailRepositoryConnection con1 = null;

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws SQLException, ClassNotFoundException {
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.port = port;
		this.serverName = serverName;
		
		if (checkForLockTable == true) {
			checkAndDeleteLock(databaseName, user, password, port, serverName);
		}

		initiate(databaseName, user, password, port, serverName);
	}


	/**
	 * Deregisters the JDBC driver. This is advisable when running <tt>Strabon</tt>
	 * through the <tt>strabon-endpoint</tt>, i.e., in a container, such as Apache Tomcat.
	 * Earlier versions of Tomcat would not deregister the JDBC drivers leading
	 * to memory leaks.
	 * 
	 * Deregistering the JDBC driver when running Strabon through <tt>Java</tt> is not required.
	 * Instead, it might lead to unexpected errors when creating many <tt>Strabon</tt> instances,
	 * one after the other, and deregistering the driver. Subsequent instantiations of
	 * <tt>Strabon</tt> in the same <tt>Java</tt> run would fail to load the driver again.
	 */
	public abstract void deregisterDriver();
	
	/**
	 * Called in Strabon constructor to initialize Strabon (establish connection to the
	 * underlying database, etc.).
	 * 
	 * @param databaseName
	 * @param user
	 * @param password
	 * @param port
	 * @param serverName
	 */
	protected abstract void initiate(String databaseName, String user, String password, int port, String serverName);

	protected void init() {

		//Setting up store

		//Used for the conversions taking place involving JTS + WGS84 (4326)
		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		//our repository
		repo1 = new SailRepository(db_store);

		try {
			repo1.initialize();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.init] initialize", e);
		}

		logger.info("[Strabon.init] Clearing Successful.");

		try {
			con1 = repo1.getConnection();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.init] getConnection", e);
		}
	}


	protected abstract void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
			throws SQLException, ClassNotFoundException;

	public SailRepositoryConnection getSailRepoConnection() {
		return con1;
	}

	public void setCon1(SailRepositoryConnection con1) {
		this.con1 = con1;
	}

	/**
	 * Close connection to Strabon.
	 */
	public void close() {
		logger.info("[Strabon.close] Closing connection...");

		try {
			con1.commit();
			con1.close();
			repo1.shutDown();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.close]", e);
		}

		logger.info("[Strabon.close] Connection closed.");
	}

	public Object query(String queryString, OutputStream out)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		return query(queryString, Format.DEFAULT, this.getSailRepoConnection(), out);	
	}

	public Object query(String queryString, Format resultsFormat, OutputStream out)
	throws  MalformedQueryException , QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		return query(queryString, resultsFormat, this.getSailRepoConnection(), out);
	}

	public ArrayList<String> query(String queryString, SailRepositoryConnection con)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		TupleQuery tupleQuery = null;
		ArrayList<String> ret = new ArrayList<String>();
		
		try {

			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				ret.add(bindingSet.toString());
			}
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.query] Error in preparing tuple query.", e);
			
		}
		
		return ret;	
	}

	public Object query(String queryString, Format resultsFormat, SailRepositoryConnection con, OutputStream out)
	throws MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		boolean status = true;
		
		logger.info("[Strabon.query] Executing query: \n{}", queryString);
		
		// check for null stream
		if (out == null) {
			logger.error("[Strabon.query] Cannot write to null stream.");
			
			return false;
		}
		
		TupleQuery tupleQuery = null;
		try {
			queryString = queryRewriting(queryString);
			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.query] Error in preparing tuple query.", e);
			status = false;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Serializing results ({})", resultsFormat.name());
		}
		
		TupleQueryResult result = null;
		switch (resultsFormat) {
			case EXP:
				long results = 0;
				
				long t1 = System.nanoTime();
				result = tupleQuery.evaluate();
				long t2 = System.nanoTime();
				
				while (result.hasNext()) {
					results++;
				}
				
				long t3 = System.nanoTime();
	
				//return new long[]{t2-t1, t3-t2, t3-t1, results};
				break;
				
		default:
			// get the writer for the specified format
			TupleQueryResultWriter resultWriter = stSPARQLQueryResultWriterFactory.createstSPARQLQueryResultWriter(resultsFormat, out);
			
			// check for null format
			if (resultWriter == null) {
				logger.error("[Strabon.query] Invalid format.");
				return false;
			}
			
			tupleQuery.evaluate(resultWriter);
		}

		return status;
	}

	private String queryRewriting(String queryString) 
	{	
		//TODO
		String newQueryString="";
		int numOfQuadruples=0;
		int startIndex=0;
		
		//remove comments from query
		String REGEX = "((^(\\s)*#)|((\\s)+#)).*$";
		Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);							
		Matcher matcher = pattern.matcher(queryString);
		String oldQueryString=matcher.replaceAll("");

		
		// check whether the query contains quadruples
		String URI = "[\\w?/<>^#]+";
		REGEX = "("+URI+"(\\s)+){3}"+URI+"(\\s)*[.}]{1}";
		pattern = Pattern.compile(REGEX, Pattern.MULTILINE);							
		matcher = pattern.matcher(oldQueryString);
		
		while(matcher.find())		
		{
			numOfQuadruples++;
			newQueryString+=oldQueryString.substring(startIndex, matcher.start());
			startIndex=matcher.end();
			
			String quadruple=oldQueryString.substring(matcher.start(), matcher.end());


			String[] token = quadruple.split("(\\s)+");
			
			newQueryString+="\n GRAPH ?g"+numOfQuadruples+" {" +token[0]+" "+token[1]+" "+token[2]+" .}\n";
			newQueryString+="?g"+numOfQuadruples+" strdf:hasValidTime "+ token[3];
			

			//case that the '.' or '}' has a '//s' character before.
			if(token.length==5)
				newQueryString+=token[4];
		}
		
		if(numOfQuadruples==0)
		{
			logger.info("\n\nQuadruple not found\n\n");
			return queryString;
		}
		else
		{
			newQueryString+=oldQueryString.substring(startIndex);
			logger.info("\n\nNew QueryString:\n {}\n\n", newQueryString);		
			return newQueryString;
		}
	}


	public void update(String updateString, SailRepositoryConnection con) throws MalformedQueryException 
	{
		Update update = null;

		try {
			update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.update]", e);
		}

		logger.info("[Strabon.update] executing update query: " + updateString);

		try {
			update.execute();
		} catch (UpdateExecutionException e) {
			logger.error("[Strabon.update]", e);
		}
	}

	public void storeInRepo(String src, String format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		storeInRepo(src, null, null, format);
	}

	public void storeInRepo(String src, String baseURI, String context, String format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		RDFFormat realFormat = null;

		if ((baseURI != null) && (baseURI.equals(""))) {
			baseURI = null;
		}

		URI uriContext;

		if ((context == null) || (context.equals(""))) {
			uriContext  = null;
			
		} else {
			ValueFactory f = repo1.getValueFactory();
			uriContext = f.createURI(context);
		}


		if(format.equalsIgnoreCase("N3")) {
			realFormat =  RDFFormat.N3;
			
		} else if(format.equalsIgnoreCase("NTRIPLES")) {
			realFormat =  RDFFormat.NTRIPLES;
			
		} else if(format.equalsIgnoreCase("RDFXML")) {
			realFormat =  RDFFormat.RDFXML;
			
		} else if(format.equalsIgnoreCase("TURTLE")) {
			realFormat =  RDFFormat.TURTLE;
			
		}else if(format.equalsIgnoreCase("NQUADS")) {
			realFormat =  RDFFormat.NQUADS;
		} 
		else {
			throw new InvalidDatasetFormatFault();
		}

		try{
			URL source = new URL(src);
			storeURL(source, baseURI, uriContext, realFormat);

		} catch(MalformedURLException e) {

			URL fromClasspath = getClass().getResource(src);
			if(fromClasspath!=null) {
				storeURL(fromClasspath, baseURI, uriContext, realFormat);
				
			} else {
				File file = new File(src);
				if (file.exists()) {
					storeURL(new URL("file://" + src), baseURI, uriContext, realFormat);

				} else {
					logger.info("File \"{}\" does not exist. Trying reading as String.", src);
					storeString((String) src, baseURI, uriContext, realFormat);
				}
			}
		}
	}

	private void storeURL(URL url, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		logger.info("[Strabon.storeURL] Storing file.");
		logger.info("[Strabon.storeURL] URL      : {}", url.toString());
		if (logger.isDebugEnabled()) {
			logger.debug("[Strabon.storeURL] Base URI : {}", ((baseURI == null) ? url.toExternalForm() : baseURI));
			logger.debug("[Strabon.storeURL] Context  : {}", ((context == null) ? "null" : context));
			logger.debug("[Strabon.storeURL] Format   : {}", ((format == null) ? "null" : format));
		}

		InputStream in = (InputStream) url.openStream();
		InputStreamReader reader = new InputStreamReader(in);
		if(baseURI == null)
		{
			baseURI = url.toExternalForm();
		}
		
		if(format.equals(RDFFormat.NQUADS))
		{
			NQuadsTranslator translator = new NQuadsTranslator();
		//	 final ByteArrayInputStream bais = new ByteArrayInputStream(i);
			 final ByteArrayInputStream bais = new ByteArrayInputStream(
			            "<http://www.v/dat/4b> <http://www.w3.org/20/ica#dtend> <http://sin/value/2> \"[2005-01-01 00:00:00+01,2006-01-01 00:00:00+01]\"^^<http://strdf.di.uoa.gr/ontology#validTime> ."
			            .getBytes()
			        );
			 final ByteArrayInputStream bais2 = new ByteArrayInputStream(
			            "<http://strdf.di.uoa.gr/ontology#validTime2005-01-01 00:00:00_2005-01-01 00:00:00_Europe/Athens> <http://strdf.di.uoa.gr/ontology#hasValidTime> \"[2005-01-01 00:00:00+01,2006-01-01 00:00:00+01]\"^^<http://strdf.di.uoa.gr/ontology#validTime> ."
			            .getBytes()
			        );
			 
			Collection<Statement> statements = translator.translate(bais2, baseURI);
			Iterator iterator = statements.iterator();
			for(Statement st: statements)
			{
				//edw prepei na mpei sunartisi pou na metasximatizei to context an einai temporal
				con1.add(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
				System.out.println("STATEMENT: "+st.toString());
				System.out.println("CONTEXT: "+st.getContext().toString());
			}
			StringReader quadGraphReader = new StringReader(translator.getHandledTriples().toString());
			con1.add(quadGraphReader, "", RDFFormat.NTRIPLES);
			return;
		}

		RDFParser parser = Rio.createParser(format);

		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();

		parser.setRDFHandler(handler);
		parser.parse(reader, "");

		logger.info("[Strabon.storeURL] Inferred {} triples.", handler.getNumberOfTriples());
		if (handler.getNumberOfTriples() > 0) {
			logger.info("[Strabon.storeURL] Triples inferred: {}", handler.getTriples());
		}
		
		StringReader georeader = new StringReader(handler.getTriples().toString());
		handler.endRDF();

		if (context == null) {
			con1.add(url, baseURI, format);
			
		} else {
			con1.add(url, baseURI, format, context);
			
		}
		
		con1.add(georeader, "", RDFFormat.NTRIPLES);
		georeader.close();
		logger.info("[Strabon.storeURL] Storing was successful.");
	}

	private void storeString(String text, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		if (baseURI == null)
			baseURI = "";

		logger.info("[Strabon.storeString] Storing triples.");
		logger.info("[Strabon.storeString] Text     : " + text);
		logger.info("[Strabon.storeString] Base URI : " + ((baseURI == null) ? "null" : baseURI));
		logger.info("[Strabon.storeString] Context  : " + ((context == null) ? "null" : context));
		logger.info("[Strabon.storeString] Format   : " + ((format == null) ? "null" : format.toString()));

		StringReader reader = new StringReader(text);
		
		

		RDFParser parser = Rio.createParser(format);

		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();

		parser.setRDFHandler(handler);
		parser.parse(reader, "");

		logger.info("[Strabon.storeString] Inferred " + handler.getNumberOfTriples() + " triples.");
		if (handler.getNumberOfTriples() > 0) {
			logger.info("[Strabon.storeString] Triples inferred:"+ handler.getTriples().toString());
		}
		StringReader georeader = new StringReader(handler.getTriples().toString());
		handler.endRDF();

		if (context == null) {
			con1.add(reader, baseURI, format);
			reader.close();
			
		} else {
			con1.add(reader, baseURI, format, context);
			reader.close();
			
		}
		
		con1.add(georeader, "", RDFFormat.NTRIPLES);
		georeader.close();
		logger.info("[Strabon.storeString] Storing was successful.");
	}

	public void describe(String describeString, String format, SailRepositoryConnection con, OutputStream out) throws MalformedQueryException
	{
		GraphQuery  graphQuery = null;

		try {
			graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, describeString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] Executing DESCRIBE query:" + describeString);

		try {
			graphQuery.evaluate(RDFHandlerFactory.createRDFHandler(format, out));

		} catch (Exception e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] DESCRIBE query executed successfully.");
	}
}
