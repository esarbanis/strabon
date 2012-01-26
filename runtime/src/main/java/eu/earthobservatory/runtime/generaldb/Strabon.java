package eu.earthobservatory.runtime.generaldb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Encoder;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.sparqlxml.stSPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.helpers.SailBase;

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

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws SQLException, ClassNotFoundException {
		if (checkForLockTable == true) {
			checkAndDeleteLock(databaseName, user, password, port, serverName);
		}
		
		initiate(databaseName, user, password, port, serverName);
	}


	protected abstract void initiate(String databaseName, String user, String password, int port, String serverName) ;

	protected void init() {

		//Setting up store


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

		System.out.println("Closing...");


		 
			try {
				con1.commit();
				con1.close();
				repo1.shutDown();
			} catch (RepositoryException e) {
				 
				e.printStackTrace();
			}
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
				BindingSet bindingSet = result.next();				
				String test = bindingSet.toString();
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
					//System.out.println(bindingSet.toString());
	
					ret.add(bindingSet.toString());
				}
			
			//System.out.println("-------------------------------------------");
			System.out.flush();
			return ret;
		}
		else if (resultsFormat.equalsIgnoreCase("XML")) {
			System.out.println("Serializing results (XML)");
			tupleQuery.evaluate(new stSPARQLResultsXMLWriter(retStream));
		}
		else if ( resultsFormat.equalsIgnoreCase("KML") ) {
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
							geom.setSRID(4326);
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
				writeOut.write(newString);
				//out.writeBytes(newString);
				
//				out.writeBytes(sb.toString());
				
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
		
		//System.out.println("Placemark0");
		System.out.println("\n\n\nGot query: " + updateString + "\n\n\n");

		try {
			update.execute();
		} catch (UpdateExecutionException e) {
			e.printStackTrace();
		}

		//System.out.println("-------------------------------------------");
		System.out.println("-            UPDATE EXECUTED              -");
		//System.out.println("-------------------------------------------");
	}

}
