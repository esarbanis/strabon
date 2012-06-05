package eu.earthobservatory.runtime.generaldb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import java.util.zip.*;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


public abstract class Strabon {

	/**
	 * @param args
	 */

	protected SailBase db_store;
	SailRepository repo1;
	SailRepositoryConnection con1 = null;

	public static void main(String[] args) {

		//PgSqlStore db_store;

	}

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) 
	throws SQLException, ClassNotFoundException 
	{
		if (checkForLockTable == true) {
			checkAndDeleteLock(databaseName, user, password, port, serverName);
		}

		initiate(databaseName, user, password, port, serverName);
	}
	
	public Strabon (String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable, String cachePath) throws SQLException, ClassNotFoundException {
		
		if(!cachePath.endsWith("/"))
		{	
			cachePath = cachePath+"/";
		}
		StrabonPolyhedron.CACHEPATH = ""+cachePath;
		//StrabonPolyhedron.TABLE_COUNTS = cachePath + "counts.bin";
		StrabonPolyhedron.TABLE_SHIFTING = cachePath + "groupbys.bin";
		//StrabonPolyhedron.TABLE_SUBJ_OBJ_TYPES = cachePath + "tableProperties.bin";
		
		File test;
		
		test = new File(StrabonPolyhedron.CACHEPATH + "counts");
		if (!test.exists()) {
			test.mkdirs();
		}
		
		test = new File(StrabonPolyhedron.CACHEPATH + "tableProperties");
		if (!test.exists()) {
			test.mkdirs();
		}		
		
		if (checkForLockTable == true) {
			checkAndDeleteLock(databaseName, user, password, port, serverName);
		}

		initiate(databaseName, user, password, port, serverName);
	}


	protected abstract void initiate(String databaseName, String user, String password, int port, String serverName) ;

	protected void init() {

		//Setting up store

		//Used for the conversions taking place involving JTS + WGS84 (4326)
		System.setProperty("org.geotools.referencing.forceXY", "true");
		//our repository
		repo1 = new SailRepository(db_store);

		try {
			repo1.initialize();
		} catch (RepositoryException e) {

			e.printStackTrace();
		}

		System.out.println("Clearing Successful");

		try {
			con1 = repo1.getConnection();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected abstract void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
	throws SQLException, ClassNotFoundException ;

	public SailRepositoryConnection getSailRepoConnection() {
		return con1;
	}



	public void setCon1(SailRepositoryConnection con1) {
		this.con1 = con1;
	}

	public void close() {
		try {
			System.out.println("Closing...");
			con1.commit();
			con1.close();

			repo1.shutDown();
			
			try {
				FileWriter fw = new FileWriter(new File(StrabonPolyhedron.CACHEPATH+"initialized.bin"));
				fw.write(1);
				fw.close();
			} catch (IOException e) {
				 
				e.printStackTrace();
			}
			 
		} catch (RepositoryException e) {
			 
			e.printStackTrace();
		}
	}

	public Object query(String queryString)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException
	{
		return query(queryString, "", this.getSailRepoConnection());	
	}

	public Object query(String queryString, String resultsFormat)
	throws  MalformedQueryException , QueryEvaluationException, IOException, TupleQueryResultHandlerException
	{
		return query (queryString, resultsFormat, this.getSailRepoConnection());
	}

	public Object query(String queryString, SailRepositoryConnection con)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException
	{
		return query(queryString, "", con);	
	}

	public Object query(String queryString, String resultsFormat, SailRepositoryConnection con)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException 

	{
		TupleQuery tupleQuery = null;
		try {
			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		} catch (RepositoryException e) {

			e.printStackTrace();
		}
		//System.out.println("Placemark0");
		//System.out.println("\n\n\nGot query - new2: " + queryString + "\n\n\n");

		ArrayList<String> ret = new ArrayList<String>();

		ByteArrayOutputStream retStream = new ByteArrayOutputStream();
		//		DataOutputStream out = new DataOutputStream(retStream);
		OutputStreamWriter writeOut = new OutputStreamWriter(retStream,"UTF-8");
		if ( resultsFormat.equalsIgnoreCase("EXP") ) {
			long results = 0;
			long t1 = System.nanoTime();
			TupleQueryResult result = tupleQuery.evaluate();
			long t2 = System.nanoTime();
			while (result.hasNext()) {
				results++;
			}
			long t3 = System.nanoTime();

			return new long[]{t2-t1, t3-t2, t3-t1,results};
		} else if ( resultsFormat.equalsIgnoreCase("") ) {
			TupleQueryResult result = null;
			//System.out.println("About to evaluate: ");
			result = tupleQuery.evaluate();

			//System.out.println("-------------------------------------------");
			//System.out.println("-                RESULTS                  -");
			//System.out.println("-------------------------------------------");


			while (result.hasNext()) {

				BindingSet bindingSet = result.next();
				//					try {
				System.out.println(bindingSet.toString());
				ret.add(bindingSet.toString());
				//					} catch (NullPointerException e) {
				//						 System.err.println("Null pointer");
				//					}
			}

			//System.out.println("-------------------------------------------");
			System.out.flush();
			return ret;
		}
		else if (resultsFormat.equalsIgnoreCase("XML")) {
			System.out.println("Serializing results (XML)");
			tupleQuery.evaluate(new stSPARQLResultsXMLWriter(retStream));
		} 
		else if (resultsFormat.equalsIgnoreCase("GeoJSON")) {

			DataOutputStream dos = new DataOutputStream(retStream);

			TupleQueryResult result = null;
			try 
			{
				result = tupleQuery.evaluate();
			} 
			catch (QueryEvaluationException e1) 
			{
				e1.printStackTrace();
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

			}
			else
			{
				return retStream.toString(); //empty
			}

			boolean firstLineParsed = false;

			do
			{
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
						catch (ParseException e) 
						{
							System.out.println("Faults detected in spatial literal representation");
							e.printStackTrace();
						}
					}

					CoordinateReferenceSystem geomCRS = null;
					try {
						geomCRS = CRS.decode("EPSG:"+SRID);
					} catch (NoSuchAuthorityCodeException e) {
						System.out.println("Error decoding returned geometry's SRID");
						e.printStackTrace();
					} catch (FactoryException e) {
						e.printStackTrace();
					}

					tb[i].setCRS(geomCRS);
					tb[i].setSRS("EPSG:"+SRID);
					tb[i].add("geometry",Geometry.class);

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

					//					FeatureJSON fjson22 = new FeatureJSON();
					//					fjson22.setEncodeFeatureCRS(true);
					//					fjson22.writeFeatureCollection(sfCollection, dos);
					//					System.out.println(retStream.toString());
				}

			}
			while((result.hasNext()));

			FeatureJSON fjson = new FeatureJSON();
			fjson.setEncodeFeatureCRS(true);
			fjson.writeFeatureCollection(sfCollection, dos);
			System.out.println(retStream.toString());

		} 
		else if ( resultsFormat.equalsIgnoreCase("KML") || resultsFormat.equalsIgnoreCase("KMZ")) {
			//GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
			GeometryFactory gf = new GeometryFactory(new PrecisionModel(),4326);
			WKTReader reader = new WKTReader(gf);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			DataOutputStream dos = new DataOutputStream(baos);

			//used to construct the entire kml document
			StringBuilder sb = new StringBuilder();

			TupleQueryResult result = null;
			try {
				result = tupleQuery.evaluate();
			} catch (QueryEvaluationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println("-------------------------------------------");
			//System.out.println("-                RESULTS                  -");
			//System.out.println("-------------------------------------------");

			int resultCounter = 0;
			try {
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();	
					ret.add(bindingSet.toString());
					Set<String> bindingNames = bindingSet.getBindingNames();
					resultCounter++;
					int geometryCounter = 0;
					for(String bindingName : bindingNames)
					{
						String unparsed = bindingSet.getBinding(bindingName).getValue().toString();
						String corrResult = unparsed.substring(1,unparsed.length()-1);

						try {
							Geometry geom = reader.read(corrResult);
							//Integer s = new Integer(4326);
							//geom.setSRID(4326);

							//geom.setSRID(32630);

							//CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:32630");
							//CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

							//MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

							//geom = JTS.transform(geom, transform);
							//geom.geometryChanged();
							geometryCounter++;
							//Only way to reach this point is if the corrResult is indeed in the form of WKT
							QName geometryType = null;
							if(geom instanceof Point)
							{
								geometryType = KML.Point;
							}
							else if(geom instanceof Polygon)
							{
								geometryType = KML.Polygon;
							}
							else if(geom instanceof LineString)
							{
								geometryType = KML.LineString;
							}
							else if(geom instanceof MultiPoint)
							{
								geometryType = KML.MultiGeometry;
							}
							else if(geom instanceof MultiLineString)
							{
								geometryType = KML.MultiGeometry;

							}
							else if(geom instanceof MultiPolygon)
							{
								geometryType = KML.MultiGeometry;

							}
							else if(geom instanceof GeometryCollection)
							{
								geometryType = KML.MultiGeometry;

							}
							else //TODO exception should be thrown here --> Specialize it
							{
								//System.out.println("Wrong Handling--> "+geometryType.toString());
								throw new Exception("Wrong Handling--> "+geom.toString());

							}

							//Encoding to KML
							Encoder encoder = new Encoder(new KMLConfiguration());
							encoder.setIndenting(true);
							//encoder.encode(geom, geometryType, dos);
							encoder.encode(geom, geometryType, baos);
							//storing the freshly produced kml element
							corrResult = baos.toString();
							//removing the xml header
							corrResult = corrResult.substring(38);

							//Constructing each individual element
							sb.append("\n<Placemark>");
							corrResult = corrResult.replaceAll("xmlns:kml=\"http://earth.google.com/kml/2.1\"","").replaceAll("kml:","");
							sb.append("\n<name> Geometry"+resultCounter+"_"+geometryCounter+"</name>");
							sb.append("\n<description>");
							//Time to fill the description

							if(bindingNames.size() > 1)
							{
								//Creating Row1 --> names
								sb.append("<![CDATA[<table border=\"1\"> <tr>");
								for(String otherBinding: bindingNames)
								{
									if(!otherBinding.equals(bindingName))
									{
										sb.append("<td>");
										sb.append(otherBinding);
										sb.append("</td>");
									}
								}
								sb.append("</tr>");

								sb.append("<tr>");
								for(String otherBinding: bindingNames)
								{
									if(!otherBinding.equals(bindingName))
									{
										sb.append("<td>");

										String bindingValue = bindingSet.getBinding(otherBinding).getValue().toString();
										sb.append(bindingValue);
										sb.append("</td>");
									}
								}
								sb.append("</table>]]>");
							}
							else
							{
								sb.append("mantalakia");
							}
							sb.append("</description>");

							sb.append(corrResult);
							sb.append("\n</Placemark>\n");

							//emptying the buffer
							baos.reset();

						} catch (ParseException e) {
							//Den prokeitai gia WKT
							//System.out.println(bindingSet.toString());
							//e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

						//Start populating KML here
					}
					//				System.out.println(bindingSet.toString());
					//				out.writeChars(bindingSet.toString());
				}
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//Finishing the structure of the kml document
			sb.insert(0,"<?xml version=\"1.0\" encoding=\"UTF-8\"?> <kml xmlns=\"http://www.opengis.net/kml/2.2\"> <Folder>");
			sb.append("</Folder></kml>");

			//System.out.println(sb.toString());
			//System.out.println(baos.toString());

			//			StringBuilder sb = new StringBuilder(); 
			//			sb.append(baos.toString().replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>",""));
			//			sb.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <kml xmlns=\"http://www.opengis.net/kml/2.2\">" +
			//					"<kml:Placemark xmlns:kml=\"http://www.opengis.net/kml/2.2\">");
			//			sb.append("</kml:Placemark></kml>");


			//System.out.println("*******************************");
			//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			//System.out.println(sb.toString());

			//System.out.println(sb.toString());

			//XXX Probably not needed after all
			//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//			DocumentBuilder builder = factory.newDocumentBuilder();
			//			StringReader sr = new StringReader(sb.toString());
			//			Document document = builder.parse(new InputSource(sr));


			//System.out.println("-------------------------------------------");
			System.out.flush();

			try {
				//String cstr = new String("aa", "UTF8");
				String newString = new String(sb.toString().getBytes(), Charset.availableCharsets().get("UTF-8"));

				if(resultsFormat.equalsIgnoreCase("KML"))
				{
					writeOut.write(newString);
					//					System.out.println(newString);
				}
				else //KMZ
				{
					//compress
					//File zfile = new File("/tmp/deleteme.kmz");
					//retStream.reset();
					//FileOutputStream fos = new FileOutputStream(zfile);
					ZipOutputStream kmzout = new ZipOutputStream(retStream);
					//ZipOutputStream kmzout = new ZipOutputStream(fos);
					ZipEntry entry = new ZipEntry("doc.kml");

					//kmzout.setLevel(6);
					kmzout.putNextEntry(entry);
					kmzout.write(newString.getBytes());
					kmzout.closeEntry();
					kmzout.close();

					//String kmzString = FileUtils.readFileToString(zfile);
					//writeOut.write(kmzString);
					/*
					try {
						File file = new File("/tmp/tmp.kml");
						String filename = "/tmp/tmp.kml";
						FileUtils.writeStringToFile(file, newString);

						File zfile = new File("/tmp/tmp.kmz");
			            String zipfilename = "/tmp/tmp.kmz";

			            byte[] buf = new byte[1024];
			            FileInputStream fis = new FileInputStream(filename);
			            fis.read(buf,0,buf.length);

			            CRC32 crc = new CRC32();
			            ZipOutputStream s = new ZipOutputStream(
			                    (OutputStream)new FileOutputStream(zipfilename));

			            //s.setLevel(6);

			            ZipEntry entry = new ZipEntry(filename);
			            entry.setSize((long)buf.length);
			            crc.reset();
			            crc.update(buf);
			            entry.setCrc( crc.getValue());
			            s.putNextEntry(entry);
			            s.write(buf, 0, buf.length);
			            s.finish();
			            s.close();

			            String kmzString = FileUtils.readFileToString(zfile);
			            writeOut.write(kmzString);

			            //FileUtils.forceDelete(file);
			            //FileUtils.forceDelete(zfile);

			        } catch (Exception e) {
			            e.printStackTrace();
			        }
					 */				
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(resultsFormat.equalsIgnoreCase("HTML")) {
			//System.out.println("Placemark1!!");
			TupleQueryResult result = tupleQuery.evaluate();
			//			List<String> bindingNames = result.getBindingNames();
			//			out.writeBytes("<tr>");
			//			for (int i = 0; i < bindingNames.size(); i++) {
			//				String bindingName = bindingNames.get(i);
			//				out.writeBytes("<th>");
			//				out.writeBytes(bindingName);
			//				out.writeBytes("</th>");
			//			}
			///////////////////////////////

			//System.out.println("Placemark2!!");
			//Remove IF in case it is considered redundant -> added to test Registry.war
			if(result.hasNext())
			{
				BindingSet set = result.next();
				Set<String> bindingNames = set.getBindingNames();			
				writeOut.write("<tr>");
				for (String bindingName: bindingNames) {
					writeOut.write("<th>");
					writeOut.write(bindingName);
					writeOut.write("</th>");
				}			
				writeOut.write("</tr>");

				writeOut.write("<tr>");

				for (String bindingName: bindingNames) {
					writeOut.write("<td>");
					writeOut.write(set.getValue(bindingName).stringValue());
					writeOut.write("</td>");
				}
				writeOut.write("</tr>");


				while (result.hasNext()) {
					writeOut.write("<tr>");
					BindingSet bindingSet = result.next();

					for (String bindingName: bindingNames) {
						writeOut.write("<td>");
						Binding binding = bindingSet.getBinding(bindingName); 
						if (binding != null) {
							Value val = binding.getValue();
							writeOut.write(val.stringValue());
						}					
						writeOut.write("</td>");
					}

					writeOut.write("</tr>");
				}
			}
		}
		else {
			//			System.exit(-1);// throw new InvalidDatasetFormatFault(); // TODO
			System.out.println("No Such Format Available!!");
			return null;
		}

		try {
			//			baos.flush();
			writeOut.flush();
			retStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Print results.
		//System.out.println(retStream.toString());

		//return ret;
		return retStream.toString();
	}

	public void update(String updateString, SailRepositoryConnection con) throws MalformedQueryException 

	{
		Update update = null;

		try {
			update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		System.out.println("Placemark0");
		System.out.println("\n\n\nGot query: " + updateString + "\n\n\n");

		try {
			update.execute();
		} catch (UpdateExecutionException e) {
			e.printStackTrace();
		}

		System.out.println("-------------------------------------------");
		System.out.println("-            UPDATE EXECUTED              -");
		System.out.println("-------------------------------------------");
	}

	@SuppressWarnings("unused")
	private void store(File file, String baseURI, RDFFormat format) throws RDFParseException, RepositoryException, IOException,InvalidDatasetFormatFault {
		con1.add(file, baseURI, format);
	}

	public void storeInRepo(Object src, String format) throws RDFParseException, RepositoryException, IOException,InvalidDatasetFormatFault, RDFHandlerException
	{
		System.out.println("generaldb.Strabon.store in repo");
		storeInRepo(src, null, null, format);
	}

	public void storeInRepo(Object src, String baseURI, String context, String format) throws RDFParseException, RepositoryException, IOException,InvalidDatasetFormatFault, RDFHandlerException
	{
		RDFFormat realFormat = null;

		if ((baseURI != null) && (baseURI.equals("")))
			baseURI = null;

		URI uriContext;

		if ((context == null) || (context.equals(""))) {
			uriContext  = null; 
		} else {
			ValueFactory f = repo1.getValueFactory();
			uriContext = f.createURI(context);
		}


		if(format.equalsIgnoreCase("N3"))
		{
			realFormat =  RDFFormat.N3;
		}
		else if(format.equalsIgnoreCase("NTRIPLES"))
		{
			realFormat =  RDFFormat.NTRIPLES;
		}
		else if(format.equalsIgnoreCase("RDFXML"))
		{
			realFormat =  RDFFormat.RDFXML;
		}
		else if(format.equalsIgnoreCase("TURTLE"))
		{
			realFormat =  RDFFormat.TURTLE;
		}
		else
		{
			throw new InvalidDatasetFormatFault();
		}

		try
		{
			if(File.class.isInstance(src))
			{
				storeFile((File)src, baseURI, uriContext, realFormat);
			}
			else if(URL.class.isInstance(src))
			{
				storeURL((URL)src, baseURI, uriContext, realFormat);
			}
			else if(String.class.isInstance(src))
			{
				storeString((String)src, baseURI, uriContext, realFormat);
			}
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	private void storeFile(File file, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		System.out.println("File     : " + file.getName());
		System.out.println("Base URI : " + ((baseURI == null) ? "null" : baseURI));
		System.out.println("Context  : " + ((context == null) ? "null" : context));
		System.out.println("Format   : " + ((format == null) ? "null" : format.toString()));

		RDFParser parser = Rio.createParser(format);
		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();
		FileReader reader = new FileReader(file);
		handler.startRDF();
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
		System.out.println("Triples inferred:"+ handler.getTriples().toString());
		StringReader georeader= new StringReader(handler.getTriples().toString());
		handler.endRDF();
		if (context == null) {
			System.out.println("[1]");
			con1.add(file, baseURI, format);
		} else {
			System.out.println("[2]");
			con1.add(file, baseURI, format, context);
		}
		con1.add(georeader, "", RDFFormat.NTRIPLES);

	}

	private void storeURL(URL url, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		System.out.println("URL      : " + url.toString());
		System.out.println("Base URI : " + ((baseURI == null) ? "null" : baseURI));
		System.out.println("Context  : " + ((context == null) ? "null" : context));
		System.out.println("Format   : " + ((format == null) ? "null" : format.toString()));

		InputStream in = (InputStream) url.openStream();
		InputStreamReader reader = new InputStreamReader(in);
		RDFParser parser = Rio.createParser(format);
		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();
		handler.startRDF();
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
		System.out.println("These are the extra triples:"+ handler.getTriples().toString());
		StringReader georeader= new StringReader(handler.getTriples().toString());
		handler.endRDF();

		if (context == null) {
			System.out.println("[3]");
			con1.add(url, baseURI, format);
		} else {
			System.out.println("[4]");
			con1.add(url, baseURI, format, context);
		}
		con1.add(georeader, "", RDFFormat.NTRIPLES);
	}

	private void storeString(String text, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		if (baseURI == null)
			baseURI = "";

		System.out.println("Text     : " + text);
		System.out.println("Base URI : " + ((baseURI == null) ? "null" : baseURI));
		System.out.println("Context  : " + ((context == null) ? "null" : context));
		System.out.println("Format   : " + ((format == null) ? "null" : format.toString()));

		StringReader reader = new StringReader(text);
		RDFParser parser = Rio.createParser(format);
		GeosparqlRDFHandlerBase handler = new GeosparqlRDFHandlerBase();

		handler.startRDF();
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
		System.out.println("These are the extra triples:"+ handler.getTriples().toString());
		StringReader georeader= new StringReader(handler.getTriples().toString());
		handler.endRDF();


		if (context == null) {
			System.out.println("[5]");
			con1.add(reader, baseURI, format);
			reader.close();
		} else {
			System.out.println("[6]");
			con1.add(reader, baseURI, format, context);
			reader.close();
		}
		con1.add(georeader, "", RDFFormat.NTRIPLES);
		georeader.close();
	}

	public void describe(String describeString, SailRepositoryConnection con, String outFile) throws MalformedQueryException
	{
		GraphQuery  graphQuery = null;

		try {
			graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, describeString);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		System.out.println("Placemark0");
		System.out.println("\n\n\nGot query: " + describeString + "\n\n\n");

		try {
			OutputStream out = new FileOutputStream(outFile);
			RDFHandler rdfHandler = new NTriplesWriter(out);
			graphQuery.evaluate(rdfHandler);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Output: "+outFile);
		System.out.println("---------------------------------------------");
		System.out.println("-            DESCRIBE EXECUTED              -");
		System.out.println("---------------------------------------------");
	}
}
