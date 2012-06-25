package org.openrdf.query.resultio.sparqlxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Encoder;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.generaldb.model.XMLGSDatatypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsKMLWriter implements TupleQueryResultWriter {

	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.sparqlxml.stSPARQLResultsKMLWriter.class);
	
	private static final String ROOT_TAG 			= "kml";
	private static final String NAMESPACE 			= "http://www.opengis.net/kml/2.2";
	private static final String RESULT_SET_TAG		= "Folder";
	
	private static final String PLACEMARK_TAG		= "Placemark";
	private static final String NAME_TAG			= "name";
	private static final String DESC_TAG			= "description";
	
	private static final String TABLE_ROW_BEGIN		= "<TR>";
	private static final String TABLE_ROW_END		= "</TR>";
	private static final String TABLE_DATA_BEGIN	= "<TD>";
	private static final String TABLE_DATA_END		= "</TD>";
	
	private static final String NEWLINE				= "\n";
	
	private static final String TABLE_DESC_BEGIN	= "<![CDATA[<TABLE border=\"1\">" + NEWLINE;
	private static final String TABLE_DESC_END		= "</TABLE>]]>" + NEWLINE;
	
	private static final String GEOMETRY_NAME		= "Geometry";
	
	/**
	 * The underlying XML formatter.
	 */
	private stSPARQLXMLWriter xmlWriter;
	
	/**
	 * The number of results seen.
	 */
	private int nresults;
	
	/**
	 * The number of geometries seen.
	 */
	private int ngeometries;

	/**
	 * The JTS wrapper
	 */
	private JTSWrapper jts;
	
	/**
	 * Stream for manipulating geometries
	 */
	private ByteArrayOutputStream baos;
	
	/**
	 * Description string holding the projected variables
	 * of the SPARQL query
	 */
	private StringBuilder descHeader;
	
	/**
	 * Description string holding the values for the
	 * projected variables of the SPARQL query
	 */
	private StringBuilder descData;
	
	/**
	 * Indentation used in tags that are constructed manually
	 */
	private int depth;
	
	/**
	 * Creates an stSPARQLResultsKMLWriter that encodes the SPARQL
	 * results in KML.
	 * 
	 * @param out
	 */
	public stSPARQLResultsKMLWriter(OutputStream out) {
		this(new stSPARQLXMLWriter(out));
	}
	
	public stSPARQLResultsKMLWriter(stSPARQLXMLWriter writer) {
		xmlWriter = writer;
		xmlWriter.setPrettyPrint(true);
		
		depth = 4;
				
		jts = JTSWrapper.getInstance();
		
		baos = new ByteArrayOutputStream();
		
		descHeader = new StringBuilder();
		descData = new StringBuilder();
		
		nresults = 0;
		ngeometries = 0;
	}
	
	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		try {
			xmlWriter.startDocument();

			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);
			xmlWriter.startTag(RESULT_SET_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			xmlWriter.endTag(RESULT_SET_TAG);
			xmlWriter.endTag(ROOT_TAG);

			xmlWriter.endDocument();
			
			baos.close();
			
			if (ngeometries < nresults) {
				logger.warn("[Strabon.KMLWriter] No spatial binding found in the result. KML requires that at least one binding maps to a geometry.", nresults);
			}
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			// true if there are bindings that do not correspond to geometries
			boolean hasDesc = false;
			
			// increase result size
			nresults++;
			
			// create description table and header
			indent(descHeader, depth);
			descHeader.append(TABLE_DESC_BEGIN);
			indent(descHeader, depth);
			descHeader.append(TABLE_ROW_BEGIN);
			
			// create description table data row
			descData.append(NEWLINE);
			indent(descData, depth);
			descData.append(TABLE_ROW_BEGIN);

			// write placemark tag
			xmlWriter.startTag(PLACEMARK_TAG);
			xmlWriter.textElement(NAME_TAG, GEOMETRY_NAME + nresults + "_" + ngeometries);
			
			// parse binding set
			for (Binding binding : bindingSet) {
				Value value = binding.getValue();

				// check for geometry value
				if (XMLGSDatatypeUtil.isGeometryValue(value)) {
					ngeometries++;
						
					if (logger.isDebugEnabled()) {
						logger.debug("[Strabon] Found geometry: {}", value);
					}
					
					xmlWriter.unescapedText(getKML(value));
				
				} else { // URI, BlankNode, or Literal other than spatial literal 
					if (logger.isDebugEnabled()) {
						logger.debug("[Strabon.KMLWriter] Found URI/BlankNode/Literal: {}", value);
					}
					
					// mark that we found sth corresponding to the description
					hasDesc = true;
					
					// write description
					writeDesc(binding);
				}
			}
			
			// we have found and constructed a description for this result. Write it down.
			if (hasDesc) {
				// close the header of the description
				descHeader.append(NEWLINE);
				indent(descHeader, depth);
				descHeader.append(TABLE_ROW_END);
				
				// end the placeholder for the description data
				descData.append(NEWLINE);
				indent(descData, depth);
				descData.append(TABLE_ROW_END);
				
				// append to the table header the actual content from
				// the bindings
				descHeader.append(descData);
				
				// close the table for the description
				descHeader.append(NEWLINE);
				indent(descHeader, depth);
				descHeader.append(TABLE_DESC_END);
				
				// begin the "description" tag
				xmlWriter.startTag(DESC_TAG);
				
				// write the actual description
				xmlWriter.unescapedText(descHeader.toString());
				
				// end the "description" tag
				xmlWriter.endTag(DESC_TAG);
			}
			
			// clear description string builders
			descHeader.setLength(0);
			descData.setLength(0);

			// write the placemark
			xmlWriter.endTag(PLACEMARK_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private String getKML(Value value) {
		String kml = "";
		QName geometryType = null;
		
		// the underlying geometry in value
		Geometry geom = null;
		
		// the underlying SRID of the geometry
		int srid = -1;
		
		// get the KML encoder
		Encoder encoder = null;
		
		try {
			encoder = new Encoder(new KMLConfiguration());
			encoder.setIndenting(true);
			
			if (value instanceof GeneralDBPolyhedron) {
				GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) value;
				
				geom = dbpolyhedron.getPolyhedron().getGeometry();
				srid = dbpolyhedron.getPolyhedron().getGeometry().getSRID();
				
			} else { // spatial literal
				Literal spatial = (Literal) value;
				
				if (XMLGSDatatypeUtil.isWKTLiteral(spatial)) { // WKT
					String wkt = spatial.stringValue();
					
					geom = jts.WKTread(WKTHelper.getWithoutSRID(wkt));
					srid = WKTHelper.getSRID(wkt);
					
				} else { // GML
					logger.warn("[Strabon.KMLWriter] GML is not supported yet");
				}
			}
			
			// transform the geometry to 4326
			geom = jts.transform(geom, srid, StrabonPolyhedron.defaultSRID);
			
			if (geom instanceof Point) {
				geometryType = KML.Point;
				
			} else if (geom instanceof Polygon) {
				geometryType = KML.Polygon;
				
			} else if (geom instanceof LineString) {
				geometryType = KML.LineString;
				
			} else if (geom instanceof MultiPoint) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof MultiLineString) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof MultiPolygon) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof GeometryCollection) {
				geometryType = KML.MultiGeometry;
				
			} 
			
			if (geometryType == null) {
				logger.warn("[Strabon.KMLWriter] Found unknown geometry type.");
				
			} else {
			
				encoder.encode(geom, geometryType, baos);
				kml = baos.toString().substring(38).replaceAll(" xmlns:kml=\"http://earth.google.com/kml/2.1\"","").replaceAll("kml:","");
				baos.reset();
			}
			
		} catch (ParseException e) {
			logger.error("[Strabon.KMLWriter] Parse error exception of geometry: {}", e.getMessage());
			
		} catch (IOException e) {
			logger.error("[Strabon.KMLWriter] IOException during KML encoding of geometry: {}", e.getMessage());
		}
		
		return kml;
	}

	/**
	 * Adds to the description table information for a binding.
	 * 
	 * @param binding
	 */
	private void writeDesc(Binding binding) {
		descHeader.append(NEWLINE);
		indent(descHeader, depth + 1);
		descHeader.append(TABLE_DATA_BEGIN);
		descHeader.append(binding.getName());
		descHeader.append(TABLE_DATA_END);
		
		descData.append(NEWLINE);
		indent(descData, depth + 1);
		descData.append(TABLE_DATA_BEGIN);
		if (binding.getValue() instanceof BNode) {
			descData.append("_:");
		}
		descData.append(binding.getValue().stringValue());
		descData.append(TABLE_DATA_END);
		
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.KML;
	}
	
	/**
	 * Adds indentation to the given string builder according to 
	 * the specified depth.
	 * 
	 * @param sb
	 * @param depth
	 */
	private void indent(StringBuilder sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append(xmlWriter.getIndentString());
		}
	}
}
