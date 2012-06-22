package eu.earthobservatory.runtime.generaldb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
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
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.resultio.sparqlxml.Format;
import org.openrdf.query.resultio.sparqlxml.stSPARQLResultsKMLWriter;
import org.openrdf.query.resultio.sparqlxml.stSPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
	 * Called by Strabon.close() to deregister the underlying JDBC driver used.
	 */
	protected abstract void deregisterDriver();
	
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
			
			// deregister jdbc driver
			deregisterDriver();

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
		
		if (out == null) {
			logger.error("[Strabon.query] Cannot write to null stream.");
			
			return false;
		}
		
		logger.info("[Strabon.query] Executing query: {}", queryString);
		
		TupleQuery tupleQuery = null;
		try {
			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.query] Error in preparing tuple query.", e);
			status = false;
		}
		
		TupleQueryResult result = null;
		
		switch (resultsFormat) {
			case DEFAULT:
				
				result = tupleQuery.evaluate();
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					
					writeString(out, bindingSet.toString());
					writeString(out, NEWLINE);
				}

				break;
				
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
				
			case XML:
				if (logger.isDebugEnabled()) {
					logger.debug("Serializing results (XML)");
				}
				
				tupleQuery.evaluate(new stSPARQLResultsXMLWriter(out));
				break;
				
			case KML:
				if (logger.isDebugEnabled()) {
					logger.debug("Serializing results (KML)");
				}

				tupleQuery.evaluate(new stSPARQLResultsKMLWriter(out));
				
				break;
				
			case KMZ:
				// create a zip entry
				ZipEntry entry = new ZipEntry("sparql_results.kml");
				
				// create a zip stream on standard output
				ZipOutputStream kmzout = new ZipOutputStream(out);
				
				// add the zip entry in it
				kmzout.putNextEntry(entry);
				
				// pass the zip stream for evaluation
				tupleQuery.evaluate(new stSPARQLResultsKMLWriter(kmzout));

				// close the zip entry
				kmzout.closeEntry();
				
				// close the zip stream
				kmzout.close();
				break;
				
			case GEOJSON:

				DataOutputStream dos = new DataOutputStream(out);
	
				try {
					result = tupleQuery.evaluate();
					
				} catch (QueryEvaluationException e) {
					logger.error("[Strabon.query] Error during query evaluation.", e);
					status = false;
					
					return status;
				}
	
				int resultsCounter = 0;
	
				//Setting a Feature Collection
				SimpleFeatureCollection sfCollection = FeatureCollections.newCollection("geomOutput");
	
				int spatialBindingsNo=0;
	
				//May not need that much - still initializing it
				String[] spatialBindings = new String[result.getBindingNames().size()];
				SimpleFeatureTypeBuilder[] tb = new SimpleFeatureTypeBuilder[result.getBindingNames().size()];
	
				for(int i=0;i<result.getBindingNames().size();i++)
				{
					tb[i] = new SimpleFeatureTypeBuilder(); 
				}
	
				BindingSet bindingSet;
				if(result.hasNext())
				{
					//Sneak Peek to obtain info on which bindings are spatial
					bindingSet = result.next();
					boolean spatial = false;
					for(String bindingName : bindingSet.getBindingNames())
					{
						Value val = bindingSet.getValue(bindingName);
	
						if(val instanceof RdbmsLiteral)
						{
							if(((RdbmsLiteral) val).getDatatype()!=null)
							{
								if(((RdbmsLiteral) val).getDatatype().toString().equals(StrabonPolyhedron.ogcGeometry))
								{
									spatial = true;
								}
							}
						}
	
						if(val instanceof GeneralDBPolyhedron)
						{
							spatial = true;
	
						}
	
						if(spatial)
						{
							spatial = false;
							spatialBindings[spatialBindingsNo] = bindingName;
							spatialBindingsNo++;
						}
					}
	
				} else {
					return status;//empty
				}
	
				boolean firstLineParsed = false;
	
				do {
					if(firstLineParsed)
					{
						bindingSet = result.next();
					}
	
					firstLineParsed = true;
	
					//How many features will occur from a single result? --> spatialBindingsNo
					for(int i=0; i<spatialBindingsNo;i++)
					{
						tb[i].setName("Feature_"+(++resultsCounter));
	
						//Every time a featureType is built, the builder is nullified!!
						//Can't avoid re-iterating...
						for(String otherBinding : bindingSet.getBindingNames())
						{
							if(!otherBinding.equals(spatialBindings[i]))
							{
								tb[i].add(otherBinding,String.class);
							}
						}
	
	
						int SRID=4326;
						Geometry geom = null;
						Value unparsedGeometry = bindingSet.getValue(spatialBindings[i]);
						//Regardless of our geometry's input, we need its SRID
						if(unparsedGeometry instanceof GeneralDBPolyhedron)
						{
							geom = ((GeneralDBPolyhedron) unparsedGeometry).getPolyhedron().getGeometry();
							SRID = ((GeneralDBPolyhedron) unparsedGeometry).getPolyhedron().getGeometry().getSRID();
						}
						else //RdbmsLiteral
							//TODO GML support to be added
						{
							String unparsedWKT = ((RdbmsLiteral)unparsedGeometry).getLabel();
							try {
								int pos = unparsedWKT.indexOf(";");
								if(pos!=-1)
								{
									geom = new WKTReader().read(unparsedWKT.substring(0,pos));
									int whereToCut = unparsedWKT.lastIndexOf('/');
									SRID = Integer.parseInt(unparsedWKT.substring(whereToCut+1));
								}
								else
								{
									geom = new WKTReader().read(unparsedWKT);
									SRID=4326;
								}
	
							} 
							catch (ParseException e) {
								logger.error("[Strabon.query] Faults detected in spatial literal representation.", e);
								status = false;
							}
						}
	
						CoordinateReferenceSystem geomCRS = null;
						try {
							geomCRS = CRS.decode("EPSG:"+SRID);
							
						} catch (NoSuchAuthorityCodeException e) {
							logger.error("[Strabon.query] Error decoding returned geometry's SRID", e);
							status = false;
							
							return status;
						} catch (FactoryException e) {
							logger.error("[Strabon.query]", e);
							status = false;
							
							return status;
						}
	
						tb[i].setCRS(geomCRS);
						tb[i].setSRS("EPSG:" + SRID);
						tb[i].add("geometry", Geometry.class);
	
						SimpleFeatureType featureType = tb[i].buildFeatureType();
						SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
	
						for(String otherBinding : bindingSet.getBindingNames())
						{
							if(!otherBinding.equals(spatialBindings[i]))
							{
								featureBuilder.add(bindingSet.getValue(otherBinding));
							}
						}
	
						featureBuilder.add(geom);
	
	
						SimpleFeature feature = featureBuilder.buildFeature(null);
						sfCollection.add(feature);
					}
	
				}
				
				while((result.hasNext()));
	
				FeatureJSON fjson = new FeatureJSON();
				fjson.setEncodeFeatureCRS(true);
				fjson.writeFeatureCollection(sfCollection, dos);
				writeString(out, NEWLINE);
				
				break;

			case HTML:
		
				result = tupleQuery.evaluate();
				
				if (result.hasNext()) {
					BindingSet set = result.next();
					Set<String> bindingNames = set.getBindingNames();			
					writeString(out, "<tr>");
					for (String bindingName: bindingNames) {
						writeString(out, "<th>");
						writeString(out, bindingName);
						writeString(out, "</th>");
					}
					
					writeString(out, "</tr>");
					writeString(out, "<tr>");
	
					for (String bindingName: bindingNames) {
						writeString(out, "<td>");
						writeString(out, set.getValue(bindingName).stringValue());
						writeString(out, "</td>");
					}
					writeString(out, "</tr>");
	
	
					while (result.hasNext()) {
						writeString(out, "<tr>");
						BindingSet bindingSet1 = result.next();
	
						for (String bindingName: bindingNames) {
							writeString(out, "<td>");
							Binding binding = bindingSet1.getBinding(bindingName); 
							if (binding != null) {
								Value val = binding.getValue();
								writeString(out, val.stringValue());
							}					
							writeString(out, "</td>");
						}
	
						writeString(out, "</tr>");
					}
					writeString(out, NEWLINE);
				}
				
				break;
			
		default:
			logger.warn("[Strabon.query] Invalid format.");
			status = false;
		}

		return status;
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

	public void storeInRepo(String src, String format) throws RDFParseException, RepositoryException, IOException,InvalidDatasetFormatFault, RDFHandlerException
	{
		storeInRepo(src, null, null, format);
	}

	public void storeInRepo(String src, String baseURI, String context, String format) throws RDFParseException, RepositoryException, IOException,InvalidDatasetFormatFault, RDFHandlerException
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
			
		} else {
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
			logger.debug("[Strabon.storeURL] Base URI : {}", ((baseURI == null) ? "null" : baseURI));
			logger.debug("[Strabon.storeURL] Context  : {}", ((context == null) ? "null" : context));
			logger.debug("[Strabon.storeURL] Format   : {}", ((format == null) ? "null" : format));
		}

		InputStream in = (InputStream) url.openStream();
		InputStreamReader reader = new InputStreamReader(in);

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

	public void describe(String describeString, SailRepositoryConnection con, String outFile) throws MalformedQueryException
	{
		GraphQuery  graphQuery = null;

		try {
			graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, describeString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] Executing describe query:" + describeString);

		try {
			OutputStream out = new FileOutputStream(outFile);
			RDFHandler rdfHandler = new NTriplesWriter(out);
			graphQuery.evaluate(rdfHandler);
			out.close();

		} catch (FileNotFoundException e) {
			logger.error("[Strabon.describe]", e);
		} catch (QueryEvaluationException e) {
			logger.error("[Strabon.describe]", e);
		} catch (RDFHandlerException e) {
			logger.error("[Strabon.describe]", e);
		} catch (IOException e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] Output: {}", outFile);
	}
	
	private static void writeString(OutputStream out, String str) throws IOException {
		if (str != null && str.length() > 0) {
			out.write(str.getBytes(Charset.defaultCharset()));
		}
	}
}
