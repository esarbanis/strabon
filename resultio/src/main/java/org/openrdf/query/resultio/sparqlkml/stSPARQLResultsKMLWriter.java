/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Copyright (C) 2010, 2011, 2012,
 * Pyravlos Team http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.sparqlkml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
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
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlxml.stSPARQLXMLWriter;
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
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 * 
 */
public class stSPARQLResultsKMLWriter implements TupleQueryResultWriter
{
	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter.class);
	private static final String ROOT_TAG = "kml";
	private static final String NAMESPACE = "http://www.opengis.net/kml/2.2";
	private static final String RESULT_SET_TAG = "Folder";
	private static final String PLACEMARK_TAG = "Placemark";
	private static final String NAME_TAG = "name";
	private static final String DESC_TAG = "description";
	private static final String STYLE_TAG = "Style";
	private static final String STYLEMAP_TAG = "StyleMap";
	private static final String LINESTYLE_TAG = "LineStyle";
	private static final String POLYSTYLE_TAG = "PolyStyle";
	private static final String STYLE_ID = "resultStyle";
	private static final String TABLE_ROW_BEGIN = "<TR>";
	private static final String TABLE_ROW_END = "</TR>";
	private static final String TABLE_DATA_BEGIN = "<TD>";
	private static final String TABLE_DATA_END = "</TD>";
	private static final String NEWLINE = "\n";
	private static final String TABLE_DESC_BEGIN = "<![CDATA[<TABLE border=\"1\">" + NEWLINE;
	private static final String TABLE_DESC_END = "</TABLE>]]>" + NEWLINE;
	private static final String GEOMETRY_NAME = "Geometry";
	private static final String MULTIGEOMETRY = "MultiGeometry";
	// Styling options
	private static final int numOfStyles = 5;
	private static final String[][] styles = {
		// note that colors are encoded as "aabbggrr" strings where
		// aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff);
		// rr=red
		// (00 to ff).
		// id, line width, line color, polygon fill, mouse over line width,
		// mouse over line color mouse over polygon fill
	{STYLE_ID + "1", "1.5", "7dff0000", "adff0000", "1.5", "7d0000ff", "ad0000ff"}, {STYLE_ID + "2", "1.5", "7d00ff00", "ad00ff00", "1.5", "7d0000ff", "ad0000ff"}, {STYLE_ID + "3", "1.5", "7d550000", "ad550000", "1.5", "7d0000ff", "ad0000ff"}, {STYLE_ID + "4", "1.5", "7d005500", "ad005500", "1.5", "7d0000ff", "ad0000ff"}, {STYLE_ID + "5", "1.5", "7d000055", "ad000055", "1.5", "7d0000ff", "ad0000ff"}};
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
	 * Description string holding the projected variables of the SPARQL query
	 */
	private StringBuilder descHeader;
	/**
	 * Description string holding the values for the projected variables of the
	 * SPARQL query
	 */
	private StringBuilder descData;
	/**
	 * Indentation used in tags that are constructed manually
	 */
	private int depth;

	/**
	 * Creates an stSPARQLResultsKMLWriter that encodes the SPARQL results in
	 * KML.
	 * 
	 * @param out
	 */
	public stSPARQLResultsKMLWriter(OutputStream out)
	{
		this(new stSPARQLXMLWriter(out));
	}

	public stSPARQLResultsKMLWriter(stSPARQLXMLWriter writer)
	{
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

	@Override public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException
	{
		try
		{
			xmlWriter.startDocument();
			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);
			xmlWriter.startTag(RESULT_SET_TAG);
			// add default styles
			for(String[] style: styles)
			{
				String id = style[0];
				String lineWidth = style[1];
				String lineColor = style[2];
				String polygonFill = style[3];
				String mouseOverLineWidth = style[4];
				String mouseOverLineColor = style[5];
				String mouseOverPolygonFill = style[6];
				// append normal style
				xmlWriter.setAttribute("id", "normal_" + id);
				xmlWriter.startTag(STYLE_TAG);
				xmlWriter.startTag(LINESTYLE_TAG);
				xmlWriter.textElement("width", lineWidth);
				xmlWriter.textElement("color", lineColor);
				xmlWriter.endTag(LINESTYLE_TAG);
				xmlWriter.startTag(POLYSTYLE_TAG);
				xmlWriter.textElement("color", polygonFill);
				xmlWriter.endTag(POLYSTYLE_TAG);
				xmlWriter.endTag(STYLE_TAG);
				// append highlight style
				xmlWriter.setAttribute("id", "highlight_" + id);
				xmlWriter.startTag(STYLE_TAG);
				xmlWriter.startTag(LINESTYLE_TAG);
				xmlWriter.textElement("width", mouseOverLineWidth);
				xmlWriter.textElement("color", mouseOverLineColor);
				xmlWriter.endTag(LINESTYLE_TAG);
				xmlWriter.startTag(POLYSTYLE_TAG);
				xmlWriter.textElement("color", mouseOverPolygonFill);
				xmlWriter.endTag(POLYSTYLE_TAG);
				xmlWriter.endTag(STYLE_TAG);
				// define map style combining the above styles
				xmlWriter.setAttribute("id", id);
				xmlWriter.startTag(STYLEMAP_TAG);
				xmlWriter.startTag("Pair");
				xmlWriter.textElement("key", "normal");
				xmlWriter.textElement("styleUrl", "#normal_" + id);
				xmlWriter.endTag("Pair");
				xmlWriter.startTag("Pair");
				xmlWriter.textElement("key", "highlight");
				xmlWriter.textElement("styleUrl", "#highlight_" + id);
				xmlWriter.endTag("Pair");
				xmlWriter.endTag(STYLEMAP_TAG);
			}
			// end of default style definition
		}
		catch(IOException e)
		{
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override public void endQueryResult() throws TupleQueryResultHandlerException
	{
		try
		{
			xmlWriter.endTag(RESULT_SET_TAG);
			xmlWriter.endTag(ROOT_TAG);
			xmlWriter.endDocument();
			baos.close();
			if(ngeometries < nresults)
			{
				logger.warn("[Strabon.KMLWriter] No spatial binding found in the result. KML requires that at least one binding maps to a geometry.", nresults);
			}
		}
		catch(IOException e)
		{
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException
	{
		try
		{
			int numOfGeometries=0;
			// true if there are bindings that do not correspond to geometries
			boolean hasDesc = false;
			// increase result size
			nresults++;
			// create description table and header
			indent(descHeader, depth);
			descHeader.append(TABLE_DESC_BEGIN);
			indent(descHeader, depth);
			List<String> geometries = new ArrayList<String>();
			// parse binding set
			for(Binding binding: bindingSet)
			{
				Value value = binding.getValue();
				// check for geometry value
				if(XMLGSDatatypeUtil.isGeometryValue(value))
				{
					numOfGeometries++;
					ngeometries++;
					if(logger.isDebugEnabled())
					{
						logger.debug("[Strabon] Found geometry: {}", value);
					}
					geometries.add(getGeometry(value));
				}
				else
				{ // URI, BlankNode, or Literal other than spatial
					// literal
					if(logger.isDebugEnabled())
					{
						logger.debug("[Strabon.KMLWriter] Found URI/BlankNode/Literal ({}): {}", value.getClass(), value);
					}
					// mark that we found sth corresponding to the description
					hasDesc = true;
					// write description
					writeDesc(binding);
				}
			}
			if(numOfGeometries > 1)
			{
				// write each polygon in separate placemarks
				for(String geometry: geometries)
				{
					xmlWriter.startTag(PLACEMARK_TAG);
					xmlWriter.textElement(NAME_TAG, GEOMETRY_NAME);
					xmlWriter.textElement("styleUrl", "#" + styles[geometries.indexOf(geometry) % (numOfStyles - 2)][0]);
					xmlWriter.startTag(MULTIGEOMETRY);
					xmlWriter.unescapedText(geometry);
					xmlWriter.endTag(MULTIGEOMETRY);
					xmlWriter.endTag(PLACEMARK_TAG);
				}
			}
			// also write them in the same placemarks
			xmlWriter.startTag(PLACEMARK_TAG);
			xmlWriter.textElement(NAME_TAG, GEOMETRY_NAME);
			xmlWriter.textElement("styleUrl", "#" + styles[(numOfStyles - 1)][0]);
			xmlWriter.startTag(MULTIGEOMETRY);
			for(String geometry: geometries)
			{
				xmlWriter.unescapedText(geometry);
			}
			xmlWriter.endTag(MULTIGEOMETRY);
			// we have found and constructed a description for this result.
			// Write it down.
			if(hasDesc)
			{
				// end the placeholder for the description data
				indent(descData, depth);
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
			xmlWriter.endTag(PLACEMARK_TAG);
		}
		catch(IOException e)
		{
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private String getGeometry(Value value)
	{
		String geometry = "";
		QName geometryType = null;
		// the underlying geometry in value
		Geometry geom = null;
		// the underlying SRID of the geometry
		int srid = -1;
		// get the KML encoder
		Encoder encoder = null;
		try
		{
			encoder = new Encoder(new KMLConfiguration());
			encoder.setIndenting(true);
			if(value instanceof GeneralDBPolyhedron)
			{
				GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) value;
				geom = dbpolyhedron.getPolyhedron().getGeometry();
				srid = dbpolyhedron.getPolyhedron().getGeometry().getSRID();
			}
			else
			{ // spatial literal
				Literal spatial = (Literal) value;
				String geomRep = spatial.stringValue();
				if(XMLGSDatatypeUtil.isWKTLiteral(spatial))
				{ // WKT
					geom = jts.WKTread(WKTHelper.getWithoutSRID(geomRep));
					srid = WKTHelper.getSRID(geomRep);
				}
				else
				{ // GML
					geom = jts.GMLread(geomRep);
					srid = geom.getSRID();
				}
			}
			// transform the geometry to {@link GeoConstants#defaultSRID}
			geom = jts.transform(geom, srid, GeoConstants.defaultSRID);
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
			if(geometryType == null)
			{
				logger.warn("[Strabon.KMLWriter] Found unknown geometry type.");
			}
			else
			{
				encoder.encode(geom, geometryType, baos);
				geometry = baos.toString().substring(38).replaceAll(" xmlns:kml=\"http://earth.google.com/kml/2.1\"", "").replaceAll("kml:", "");

				if(geometryType == KML.MultiGeometry)
				{
					geometry = geometry.substring(geometry.indexOf("<MultiGeometry>") + 15, geometry.indexOf("</MultiGeometry>"));
				}
/*				if(geom instanceof Point)
				{
					geometry = geometry.substring(geometry.indexOf("<Point>"), geometry.indexOf("</Point>") + 8);
				}
				else if(geom instanceof Polygon)
				{
					geometry = geometry.substring(geometry.indexOf("<Polygon>"), geometry.indexOf("</Polygon>") + 10);
				}
				else if(geom instanceof LineString)
				{
					geometry = geometry.substring(geometry.indexOf("<LineString>"), geometry.indexOf("</LineString>") + 13);
				}
				else if(geom instanceof MultiPoint)
				{
					geometry = geometry.substring(geometry.indexOf("<MultiPoint>"), geometry.indexOf("</MultiPoint>") + 13);
				}
				else if(geom instanceof MultiLineString)
				{
					geometry = geometry.substring(geometry.indexOf("<MultiLineString>"), geometry.indexOf("</MultiLineString>") + 18);
				}
				else if(geom instanceof MultiPolygon)
				{
					geometry = geometry.substring(geometry.indexOf("<MultiPolygon>"), geometry.indexOf("</MultiPolygon>") + 15);
				}
				else if(geom instanceof GeometryCollection)
				{
					geometry = geometry.substring(geometry.indexOf("<GeometryCollection>"), geometry.indexOf("</GeometryCollection>") + 21);
				}
*/				
				baos.reset();
			}
		}
		catch(ParseException e)
		{
			logger.error("[Strabon.KMLWriter] Parse error exception of geometry: {}", e.getMessage());
		}
		catch(IOException e)
		{
			logger.error("[Strabon.KMLWriter] IOException during KML encoding of geometry: {}", e.getMessage());
		}
		catch(JAXBException e)
		{
			logger.error("[Strabon.KMLWriter] Exception during GML parsing: {}", e.getMessage());
		}
		return geometry;
	}

	/**
	 * Adds to the description table information for a binding.
	 * 
	 * @param binding
	 */
	private void writeDesc(Binding binding)
	{
		descData.append(NEWLINE);
		indent(descData, depth + 1);
		descData.append(TABLE_ROW_BEGIN);
		descData.append(TABLE_DATA_BEGIN);
		descData.append(binding.getName());
		descData.append(TABLE_DATA_END);
		descData.append(TABLE_DATA_BEGIN);
		if(binding.getValue() instanceof BNode)
		{
			descData.append("_:");
		}
		descData.append(binding.getValue().stringValue());
		descData.append(TABLE_DATA_END);
		descData.append(TABLE_ROW_END);
	}

	@Override public TupleQueryResultFormat getTupleQueryResultFormat()
	{
		return stSPARQLQueryResultFormat.KML;
	}

	/**
	 * Adds indentation to the given string builder according to the specified
	 * depth.
	 * 
	 * @param sb
	 * @param depth
	 */
	private void indent(StringBuilder sb, int depth)
	{
		for(int i = 0; i < depth; i++)
		{
			sb.append(xmlWriter.getIndentString());
		}
	}
}
