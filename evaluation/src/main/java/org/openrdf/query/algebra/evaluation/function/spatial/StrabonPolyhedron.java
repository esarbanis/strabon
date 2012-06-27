package org.openrdf.query.algebra.evaluation.function.spatial;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;

/**
 * A {@link StrabonPolyhedron} is a @{link Value} that is used to represent geometries.
 * Therefore, a {@link StrabonPolyhedron} wraps around the construct of an RDF @{link Value}
 * the notion of geometry. This geometry can be expressed in different kinds of
 * representations, such as linear constraints over the reals with addition
 * (Semi-linear point sets), Well-Known Text (WKT), or Geography Markup Language (GML).
 * 
 * The former kind of representation, i.e., Semi-linear point sets, was the first
 * representation to be supported by StrabonPolyhedron and now has been deprecated and
 * not supported any more. It can be enabled by setting the value for variable
 * {@link #EnableConstraintRepresentation} to <tt>true</tt>. However, this is hardly 
 * suggested and it is discouraged. 
 * 
 * The other two kinds of representation is WKT and GML which are representations
 * standardized by the Open Geospatial Consortium (OGC). Both representations can be
 * used to represent a geometry and they are enabled by default.
 * 
 * {@link StrabonPolyhedron} does not store a specific representation for a geometry. In
 * contrast, it stores the plain geometry as a byte array using a {@link Geometry} object.
 * However, {@link StrabonPolyhedron} offers constructors and methods for getting a
 * {@link StrabonPolyhedron} instance through any kind of representation and of course
 * getting a {@link StrabonPolyhedron} instance in a specific representation.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Kostis Kyzirakos <kk@di.uoa.gr>
 *
 */
public class StrabonPolyhedron implements Value {

	private static final long serialVersionUID = 894529468109904724L;
	
	public static String CACHEPATH = "";
	public static String TABLE_COUNTS = "counts.bin";
	public static String TABLE_SUBJ_OBJ_TYPES = "tableProperties.bin";
	public static String TABLE_SHIFTING = "groupbys.bin";


	public static final boolean EnableConstraintRepresentation = false;

	/**
	 * The namespace for stRDF data model
	 */
	public static final String stRDF					= "http://strdf.di.uoa.gr/ontology#";
	
	/**
	 * The URI for the datatype SemiLinearPointSet
	 * (linear constraint-based representation of geometries)
	 */
	public static final String stRDFSemiLinearPointset	= stRDF + "SemiLinearPointSet";
	
	/**
	 * The URI for the datatype Well-Known Text (WKT)
	 */
	public static final String WKT 						= stRDF + "WKT";
	
	/**
	 * The URI for the datatype Geography Markup Langague (GML)
	 */
	public static final String GML						= stRDF + "GML";
	
	//private static final String GML					= "http://www.opengis.net/def/geometryType/OGC-GML/3.2/";
	
	/**
	 * The namespace for geometry functions declared by GeoSPARQL
	 */
	public static final String geof						= "http://www.opengis.net/def/queryLanguage/OGC-GeoSPARQL/1.0/function/";
	
	/* 						Extended functions 							*/
	
	// Spatial Relationships
	public static final String anyInteract 		= stRDF + "anyInteract";
	public static final String contains 		= stRDF + "contains";
	public static final String coveredBy 		= stRDF + "coveredBy";
	public static final String covers 			= stRDF + "covers";
	public static final String disjoint 		= stRDF + "disjoint";
	public static final String equals 			= stRDF + "equals";
	public static final String inside 			= stRDF + "inside";
	public static final String overlap 			= stRDF + "overlap";
	public static final String touch 			= stRDF + "touch";
	public static final String relate 			= stRDF + "relate";
	public static final String left 			= stRDF + "left";
	public static final String right			= stRDF + "right";
	public static final String above 			= stRDF + "above";
	public static final String below			= stRDF + "below";
	
	// Spatial Constructs
	public static final String union 			= stRDF + "union";
	public static final String buffer 			= stRDF + "buffer";
	public static final String envelope 		= stRDF + "envelope";
	public static final String convexHull		= stRDF + "convexHull";
	public static final String boundary 		= stRDF + "boundary";
	public static final String intersection 	= stRDF + "intersection";
	public static final String difference 		= stRDF + "difference";
	public static final String symDifference	= stRDF + "symDifference";
	public static final String transform 		= stRDF + "transform";
	
	// Spatial Metric Functions
	public static final String distance 		= stRDF + "distance";
	public static final String area 			= stRDF + "area";
	
	// Spatial Properties
	public static final String dimension 		= stRDF + "dimension";
	public static final String geometryType 	= stRDF + "geometryType";
	public static final String asText 			= stRDF + "asText";
	public static final String asGML 			= stRDF + "asGML";
	public static final String srid 			= stRDF + "srid";
	public static final String isEmpty 			= stRDF + "isEmpty";
	public static final String isSimple 		= stRDF + "isSimple";
	public static final Integer defaultSRID 	= 4326; //default srid.
	
	// Spatial Aggregate Functions
	public static final String extent 			=stRDF + "extent";

	// GEOSPARQL

	// Non-topological
	public static final String geoSparqlDistance 			= geof + "distance"; //3 arguments
	public static final String geoSparqlBuffer 				= geof + "buffer"; //3 arguments
	public static final String geoSparqlConvexHull 			= geof + "convexHull";
	public static final String geoSparqlIntersection 		= geof + "intersection";
	public static final String geoSparqlUnion 				= geof + "union";
	public static final String geoSparqlDifference 			= geof + "difference";
	public static final String geoSparqlSymmetricDifference = geof + "symmetricDifference";
	public static final String geoSparqlEnvelope 			= geof + "envelope";
	public static final String geoSparqlBoundary 			= geof + "boundary";

	// Simple Features - 8 functions - all with 2 arguments + boolean
	public static final String sfEquals 					= geof + "sf-equals";  
	public static final String sfDisjoint 					= geof + "sf-disjoint";  
	public static final String sfIntersects 				= geof + "sf-intersects";
	public static final String sfTouches 					= geof + "sf-touches";
	public static final String sfCrosses 					= geof + "sf-crosses";
	public static final String sfWithin 					= geof + "sf-within";
	public static final String sfContains 					= geof + "sf-contains";
	public static final String sfOverlaps 					= geof + "sf-overlaps";

	// Egenhofer - 8 functions - all with 2 arguments + boolean
	public static final String ehEquals 					= geof + "eh-equals";  
	public static final String ehDisjoint 					= geof + "eh-disjoint";  
	public static final String ehMeet 						= geof + "eh-meet";
	public static final String ehOverlap 					= geof + "eh-overlap";
	public static final String ehCovers 					= geof + "eh-covers";
	public static final String ehCoveredBy 					= geof + "eh-coveredBy";
	public static final String ehInside 					= geof + "eh-inside";
	public static final String ehContains 					= geof + "eh-contains";

	// RCC8 - 8 functions - all with 2 arguments + boolean
	public static final String rccEquals 						 = geof + "rcc8-eq";  
	public static final String rccDisconnected 					 = geof + "rcc8-dc";  
	public static final String rccExternallyConnected 			 = geof + "rcc8-ec";
	public static final String rccPartiallyOverlapping 			 = geof + "rcc8-po";
	public static final String rccTangentialProperPartInverse 	 = geof + "rcc8-tppi";
	public static final String rccTangentialProperPart 			 = geof + "rcc8-tpp";
	public static final String rccNonTangentialProperPart 		 = geof + "rcc8-ntpp";
	public static final String rccNonTangentialProperPartInverse = geof + "rcc8-ntppi";

	public static final String geoSparqlRelate 					 = geof + "relate";

	private static int MAX_POINTS = Integer.MAX_VALUE;//40000;//Integer.MAX_VALUE;//10000;

	/**
	 * Get Java Topology Suite wrapper instance.
	 */
	private static JTSWrapper jts = JTSWrapper.getInstance();
	
	/**
	 * The underlying geometry
	 */
	private Geometry geometry;

	public StrabonPolyhedron() {
		this.geometry = null;

	}
	
	public StrabonPolyhedron(Polygon polygon) throws Exception {
		this.geometry = new StrabonPolyhedron(polygon, 1).geometry;
	}

	public StrabonPolyhedron(String geometry) throws Exception {
		int geomSRID = 4326;
		if(geometry.contains(";"))
		{
			int whereToCut = geometry.lastIndexOf('/');
			geomSRID = Integer.parseInt(geometry.substring(whereToCut+1));
			whereToCut = geometry.indexOf(';');
			geometry.substring(0,whereToCut);
		}
		if (geometry.startsWith("POINT") || 
				geometry.startsWith("LINESTRING") || 
				geometry.startsWith("POLYGON") || 
				geometry.startsWith("MULTIPOINT") || 
				geometry.startsWith("MULTILINESTRING") || 
				geometry.startsWith("MULTIPOLYGON") || 
				geometry.startsWith("GEOMETRYCOLLECTION")) {
			Geometry geo = jts.WKTread(geometry);
			this.geometry = new StrabonPolyhedron(geo).geometry;
			//Default 
			this.geometry.setSRID(geomSRID);
		} else {
		
			if(geometry.contains("gml"))
			{
				Geometry geo = GMLReader(geometry);
				this.geometry = new StrabonPolyhedron(geo).geometry;
			}
			
			//Polyhedron polyhedron = new Polyhedron(geometry);
			//String polyhedronWKT = polyhedron.toWKT();
			//Geometry geo = jts.WKTread(polyhedronWKT);
			//
			//if (!EnableConstraintRepresentation) {
			//	this.geometry = geo.union(geo);
			//}
		}
	}

	public StrabonPolyhedron(String WKT, int algorithm) throws Exception {
		if(WKT.contains("gml"))
		{
			Geometry geo = GMLReader(WKT);
			this.geometry = new StrabonPolyhedron(geo).geometry;
		}
		else
		{
			//System.out.println("	new StrabonPolyhedron: before WKTReader");
			Geometry geo = jts.WKTread(WKT);
			//System.out.println("	new StrabonPolyhedron: after WKTReader");
			this.geometry = new StrabonPolyhedron(geo, algorithm).geometry;
		}
	
	}

	public StrabonPolyhedron(String WKT, int algorithm, int maxPoints) throws Exception {

		if(WKT.contains("gml"))
		{
			System.err.println("**************** THIS IS NOT A GOOD PLACE/WARY FOR THIS CONVERSION /////////");
			Geometry geo = GMLReader(WKT);
			this.geometry = new StrabonPolyhedron(geo).geometry;
		}
		else
		{

			Geometry geo = jts.WKTread(WKT);
			this.geometry = new StrabonPolyhedron(geo, algorithm).geometry;	
		}
	}

	public StrabonPolyhedron(byte[] byteArray) throws ParseException {
		this.geometry = jts.WKBread(byteArray);
	}

	public StrabonPolyhedron(byte[] byteArray, int srid) throws ParseException {

		this.geometry = jts.WKBread(byteArray);
		this.geometry.setSRID(srid);
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public static StrabonPolyhedron ConstructFromWKB(byte[] byteArray) throws Exception {
		return new StrabonPolyhedron(jts.WKBread((byteArray)));
	}

	//public StrabonPolyhedron(int partitionAlgorithmIgnored, String constraints) throws Exception {
	//	Polyhedron poly = new Polyhedron(constraints);
	//	this.geometry = jts.WKTread(poly.toWKT());
	//}

	public StrabonPolyhedron(Geometry geo) throws Exception {
		this.geometry = new StrabonPolyhedron(geo, 1).geometry;
		this.geometry.setSRID(geo.getSRID());
	}

	public StrabonPolyhedron(Geometry geo, int algorithm) throws Exception {
		this.geometry = new StrabonPolyhedron(geo, algorithm, MAX_POINTS).geometry;
	}

	@SuppressWarnings("unused")
	public StrabonPolyhedron(Geometry geo, int algorithm, int maxPoints) throws Exception {		
		if (geo.isEmpty()) {
			this.geometry = geo;
			return;
		}

		if (!EnableConstraintRepresentation) {
			this.geometry = geo;
			return;
		}

		//always returns true...
		//if (!geo.isSimple())
		//	throw new Exception("The polygon is not simple. Only simple polygons are supported.");

		if (Point.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (LineString.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (Polygon.class.isInstance(geo)) {
			//if (!geo.isValid()) {
			//	System.out.println("Non valid " + FindGeoType(geo) + " found. ("+ geo.toString() +")");
			//	geo = geo.buffer(0.0);
			//	System.out.println("Converted to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid. ("+geo.toString()+")");
			//	this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//} else {
			this.geometry = new StrabonPolyhedron((Polygon) geo, algorithm, maxPoints).geometry;
			//}
		} else if (MultiPoint.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (MultiLineString.class.isInstance(geo)) {
			//throw new Exception("MultiLineString not implemented yet.");
			MultiLineString mline = (MultiLineString)geo;
			ArrayList<LineString> collection = new ArrayList<LineString>(mline.getNumGeometries());

			for (int i = 0; i < mline.getNumGeometries(); i++) {
				System.out.println("[1] " + mline.getNumGeometries());
				StrabonPolyhedron line = new StrabonPolyhedron(mline.getGeometryN(i), algorithm, maxPoints);
				System.out.println("[2] " + line.geometry.getNumGeometries());
				for (int j = 0; j < line.geometry.getNumGeometries(); j++) {
					collection.add((LineString)line.geometry.getGeometryN(j));
				}
			}

			LineString[] linecollection = new LineString[collection.size()];
			int k = 0;
			for (LineString line : collection) {
				linecollection[k] = line;
				k++;
				assert (!line.isEmpty());
			}
			this.geometry = new MultiLineString(linecollection, new GeometryFactory());
		} else if (MultiPolygon.class.isInstance(geo)) {
			//			if (!geo.isValid()) {
			////				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			////				geo = geo.buffer(0.0);
			////				
			////				Geometry[] geometries = new Geometry[geo.getNumGeometries()];
			////				for (int i = 0; i < geo.getNumGeometries(); i++) {
			////					boolean before = geo.getGeometryN(i).isValid();
			////					geometries[i] = geo.getGeometryN(i).buffer(0.0);
			////					boolean after = geometries[i].isValid();
			////					//System.out.println("Geometry " + i + " was " + (before ? "" : "not ") + "valid and now it is " + (after ? "still " : "not ") + "valid.");
			////				}			
			////				
			////				Geometry col = new GeometryCollection(geometries, new GeometryFactory()).buffer(0.0);
			////				System.out.println("Converted to a "+FindGeoType(col)+" that is "+(col.isValid() ? "" : "not ")+"valid.");
			////				this.geometry = new StrabonPolyhedron(col, algorithm, maxPoints).geometry;
			//				
			////				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			////				
			////				System.out.println("Number of geometries: " + geo.getNumGeometries());
			////				MultiPolygon multipoly = (MultiPolygon)geo;
			////				Geometry newPoly = multipoly.getGeometryN(0);
			////				
			////				for (int i = 1; i < geo.getNumGeometries(); i++) {
			////					newPoly = newPoly.union(geo.getGeometryN(i));
			////				}			
			////				
			////				newPoly.buffer(0.0);
			////				
			////				//Geometry col = new GeometryCollection(geometries, new GeometryFactory()).buffer(0.0);
			////				System.out.println("Converted to a "+FindGeoType(newPoly)+" that is "+(newPoly.isValid() ? "" : "not ")+"valid.");
			////				this.geometry = new StrabonPolyhedron(newPoly, algorithm, maxPoints).geometry;
			//				
			//				//System.out.println("Non valid " + FindGeoType(geo) + " found. (coordinates:"+geo.getCoordinates().length+")");
			//				//geo = TopologyPreservingSimplifier.simplify(geo, 0.2);
			//				while (true) {
			//					if (geo.getCoordinates().length > 300000) {
			//						geo = TopologyPreservingSimplifier.simplify(geo, 0.1);
			//						System.out.println("Simplified to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid (coordinates:"+geo.getCoordinates().length+").");
			//					}
			//					geo = geo.buffer(0.0);
			//					System.out.println("Buffered to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid (coordinates:"+geo.getCoordinates().length+").");
			//					
			//					if (geo.isValid() && (geo.getCoordinates().length < 300000))
			//						break;
			//				}								
			//				
			//				this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//				
			//				//System.out.println("Are the geometries the same? Answer: " + (geo.equals(this.geometry) ? "true" : "false"));
			//				
			//			} else {
			MultiPolygon mpoly = (MultiPolygon)geo;
			ArrayList<Polygon> collection = new ArrayList<Polygon>(mpoly.getNumGeometries());

			for (int i = 0; i < mpoly.getNumGeometries(); i++) {
				System.out.println("[1] " + mpoly.getNumGeometries());
				StrabonPolyhedron poly = new StrabonPolyhedron(mpoly.getGeometryN(i), algorithm, maxPoints);
				System.out.println("[2] " + poly.geometry.getNumGeometries());
				for (int j = 0; j < poly.geometry.getNumGeometries(); j++) {
					collection.add((Polygon)poly.geometry.getGeometryN(j));
				}
			}

			Polygon[] polycollection = new Polygon[collection.size()];
			int k = 0;
			for (Polygon polygon : collection) {
				polycollection[k] = polygon;
				k++;
				assert (!polygon.isEmpty());
			}
			this.geometry = new MultiPolygon(polycollection, new GeometryFactory());
			//			}
		} else {
			//			if (!geo.isValid()) {
			//				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			//				geo = geo.buffer(0.0);
			//				System.out.println("Converted to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid+.");
			//				this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//			} else {
			for (int i = 0; i < geo.getNumGeometries(); i++) {
				StrabonPolyhedron smallGeo = new StrabonPolyhedron(geo.getGeometryN(i), algorithm, maxPoints);

				if (this.geometry == null) {
					this.geometry = smallGeo.geometry;
				} else {
					this.geometry.union(smallGeo.geometry);
				}
			}
			//			}
		}
	}

	public static StrabonPolyhedron ParseBigPolyhedron(Geometry polygon, int algorithm, boolean horizontal, int maxPoints) throws Exception {
		assert (Polygon.class.isInstance(polygon) || (MultiPolygon.class.isInstance(polygon)));

		if (polygon.getCoordinates().length > maxPoints) {
			//			if (polygon.isValid()){
			//				System.out.println("Found big polyhedron. Coordinates: " + polygon.getCoordinates().length + " (valid="+polygon.isValid()+").");
			//			} else {
			//				System.out.println("Found invalid big polyhedron. Coordinates: " + polygon.getCoordinates().length + ".");
			//				//IsValidOp err = new IsValidOp(polygon);
			//				//System.out.println("Validation error: " + err.getValidationError());
			//				//new Point(new CoordinateArraySequence(new Coordinate[] {polygon.getCoordinates()[0]}), new GeometryFactory());
			//				//polygon = polygon.union(onePoint);
			//				polygon = polygon.buffer(0.0);
			//				System.out.println("After conversion, coordinates: " + polygon.getCoordinates().length + " (valid="+polygon.isValid()+").");
			//			}
			double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE, 
					maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;

			Geometry bbox = polygon.getEnvelope();
			for (int i = 0; i < bbox.getCoordinates().length; i++) {
				Coordinate c = bbox.getCoordinates()[i];
				if (c.x > maxx) maxx = c.x;
				if (c.x < minx)	minx = c.x;
				if (c.y > maxy)	maxy = c.y;
				if (c.y < miny)	miny = c.y;
			}

			Polygon firsthalf = new Polygon(new LinearRing(new CoordinateArraySequence( 
					new Coordinate[] {
							new Coordinate(minx, 										miny),
							new Coordinate(horizontal ? (minx + (maxx-minx)/2) : maxx, 	miny),
							new Coordinate(horizontal ? (minx + (maxx-minx)/2) : maxx, 	horizontal ? maxy : (miny + (maxy-miny)/2)),
							new Coordinate(minx, 										horizontal ? maxy : (miny + (maxy-miny)/2)),
							new Coordinate(minx, 										miny)}
					), new GeometryFactory()), null, new GeometryFactory());

			firsthalf.normalize();

			Polygon secondhalf = (Polygon) bbox.difference(firsthalf);
			secondhalf.normalize();

			//			double a = polygon.getArea();
			//			double b = polygon.getEnvelope().getArea();
			//			double c = firsthalf.getArea();
			//			double d = bbox.difference(firsthalf).getArea();
			//			
			//			double e = b-c-d;
			//			double f = c-d;
			//			
			//			double kk = firsthalf.difference(bbox).difference(firsthalf).getArea();
			//			
			//			boolean g = firsthalf.equals(bbox.difference(firsthalf));
			//			boolean h = firsthalf.disjoint(bbox.difference(firsthalf));
			//			boolean i = bbox.equals(firsthalf.union(bbox.difference(firsthalf)));
			//			
			//			boolean j = firsthalf.intersects(polygon);
			//			boolean k = bbox.difference(firsthalf).intersects(polygon);

			Geometry A = polygon.intersection(firsthalf);
			System.out.println("First half  : " + A.getCoordinates().length + " coordinates.");
			//Geometry B = polygon.intersection(bbox.difference(firsthalf));
			Geometry B = polygon.intersection(secondhalf);
			System.out.println("Second half : " + B.getCoordinates().length + " coordinates.");

			StrabonPolyhedron polyA = ParseBigPolyhedron(A, algorithm, !horizontal, maxPoints);			
			StrabonPolyhedron polyB = ParseBigPolyhedron(B, algorithm, !horizontal, maxPoints);

			return StrabonPolyhedron.quickUnion(polyA, polyB);
		} else {
			System.out.println("Found small polyhedron. Coordinates: " + polygon.getCoordinates().length);
			return new StrabonPolyhedron(polygon, algorithm, maxPoints);
		}
	}

	public StrabonPolyhedron(Polygon polygon, int algorithm, int maxPoints) throws Exception {
		//		if (!polygon.isSimple())
		//			throw new Exception(
		//			"The polygon is not simple. Only simple polygons are supported");

		Coordinate[] coordinates = polygon.getCoordinates();

		if (coordinates.length > maxPoints) {
			this.geometry = ParseBigPolyhedron(polygon, algorithm, true, maxPoints).geometry;
			return;
		}		

		int distinctCoordinates = 0;
		boolean fix = false;
		for (int i = 0; i <= coordinates.length - 1; i++) {
			Coordinate c1 = coordinates[i];

			if (i == (coordinates.length - 1)) {
				// eimaste sto teleutaio simeio
				if ((c1.x != coordinates[0].x) || (c1.y != coordinates[0].y)) {
					// and den einai to idio me to 1o error
					//throw new Exception("Problem in  geometry. First and last point (i="+i+") do not match (coordinates: "+coordinates.length+", isValid:"+polygon.isValid()+").");
					distinctCoordinates++;
					fix = true;
				} else 
					if ((c1.x == coordinates[i-1].x) && (c1.y == coordinates[i-1].y)) {
						//einai to idio me to proigoumeno opote den kanoume tipota giati
						//exoun hdh auksithei ta dinstinct
					} else {				
						// den einai to idio me to proigoumeno opote auksise ta distinct
						distinctCoordinates++;
					}
				continue;
			} 

			Coordinate c2 = coordinates[i+1];

			if ((c1.x != c2.x) || (c1.y != c2.y)) {
				distinctCoordinates++;
			}
		}

		//System.out.println("---\n---\n---\n---\n---\n");
		//System.out.println("--- Coordinates.length   = " + coordinates.length);
		//System.out.println("--- Distinct coordinates = " + distinctCoordinates);
		//System.out.println("---\n---\n---\n---\n---\n");

		// cgal wants counter clockwise order
		//double[][] c = new double[coordinates.length - 1][2];
		int counter = 0;
		double[][] c = new double[(fix ? distinctCoordinates : (distinctCoordinates - 1))][2];
		for (int i = 0; i <= coordinates.length - 2; i++) {
			Coordinate c1 = coordinates[i];
			Coordinate c2 = coordinates[i+1];

			if ((c1.x != c2.x) || (c1.y != c2.y)) {
				c[counter][0] = c1.x;
				c[counter][1] = c1.y;
				counter++;
			}			
		}

		if (fix) {
			c[distinctCoordinates-1][0] = coordinates[coordinates.length-1].x;
			c[distinctCoordinates-1][1] = coordinates[coordinates.length-1].y;
		}

		//System.out.println("--- Counter              = " + counter);
		//System.out.println("---\n---\n---\n---\n---\n");

		//		BufferedWriter bww = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/cfunction.dat")));
		//		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/original.dat")));
		//		bww.write("void make_polygon(Polygon_2& polygon) {");
		//		for (int i = 0; i < coordinates.length - 1; i++) {
		//			Coordinate coordinate = coordinates[i];
		//			bww.write("\tpolygon.push_back(Point_2(");
		//			bww.write(new Double(coordinate.x).toString());
		//			bww.write(",");
		//			bww.write(new Double(coordinate.y).toString());
		//			bww.write("));\n");
		//			
		//			bw2.write(new Double(coordinate.x).toString());
		//			bw2.write(" ");
		//			bw2.write(new Double(coordinate.y).toString());
		//			bw2.write("\n");
		//		}
		//		bww.write("}\n");
		//		bww.flush();
		//		bww.close();
		//		
		//		bw2.flush();
		//		bw2.close();

		double start = System.nanoTime();
		//		double[][][] convexified = Polyhedron.ConvexifyPolygon(c, algorithm);
		double[][][] convexified = new double[1][2][3];		

		//		if (convexified == null) {
		//			throw new ParseGeometryException("Invalid geometry. Only simple geometries are supported.");
		//		}

		System.out.println("ConvexifyTime " + (System.nanoTime()-start));

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < convexified.length; i++) {
			double[][] convexCoordinates = convexified[i];
			for (int j = 0; j < convexCoordinates.length; j++) {
				if (convexCoordinates[j][0] > max)
					max = convexCoordinates[j][0];
				if (convexCoordinates[j][0] < min)
					min = convexCoordinates[j][0];
			}

		}

		//		String gnuPlotScript = "";
		//		
		//		for (int i = 0; i < convexified.length; i++) {
		//			double[][] convexCoordinates = convexified[i];
		//			sizes[convexCoordinates.length]++;
		//			
		//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/data-" + i + ".dat")));
		//			bw2 = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/script-" + i + ".gnuplot")));
		//			for (int j = 0; j < convexCoordinates.length; j++) {
		//				bw.write(new Double(convexCoordinates[j][0]).toString());
		//				bw.write(" ");
		//				bw.write(new Double(convexCoordinates[j][1]).toString());
		//				bw.write("\n");
		//
		//			}
		//			bw.flush();
		//			bw.close();
		//			
		//			gnuPlotScript += "'data-" + i + ".dat' with lines,";
		//			
		//			bw2.write("set terminal postscript eps color\n");
		//			bw2.write("set out '/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/geo-"+i+".eps'\n");
		//			bw2.write("set key bmargin left horizontal Right noreverse enhanced autotitles box linetype -1 linewidth 1.000\n");
		//			bw2.write("plot ["+0.95*min+":"+1.05*max+"] 'data-" + i +".dat' with lines, 'original.dat' with lines\n");
		//			bw2.flush();
		//			bw2.close();
		//		}
		//			
		//		gnuPlotScript = "plot ["+0.95*min+":"+1.05*max+"] " + gnuPlotScript.substring(0, gnuPlotScript.length()-1);
		//		gnuPlotScript = "set terminal postscript eps color\n" +
		//						"set out '/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/all.eps'\n" +
		//						"set key bmargin left horizontal Right noreverse enhanced autotitles box linetype -1 linewidth 1.000\n" + 
		//						gnuPlotScript;
		//		
		//		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/script-all.gnuplot")));
		//		bw.write(gnuPlotScript);
		//		bw.flush();
		//		bw.close();
		//		
		//		for (int i = 0; i < convexified.length; i++) {
		//			Runtime.getRuntime().exec("gnuplot /home/kkyzir/Desktop/Spatial\\ data/ssg4env/geometries/gnuplot/script-"+i+".gnuplot");
		//		}
		//		
		//		Runtime.getRuntime().exec("gnuplot /home/kkyzir/Desktop/Spatial\\ data/ssg4env/geometries/gnuplot/script-all.gnuplot");
		//		

		//Geometry[] collection = new Geometry[convexified.length];
		Polygon[] collection = new Polygon[convexified.length];
		System.out.println("Convex parts: " + convexified.length);		
		for (int i = 0; i < convexified.length; i++) {
			GeometryFactory factory = new GeometryFactory();
			double[][] convexCoordinates = convexified[i];
			Coordinate[] jtsCoordinates = new Coordinate[convexCoordinates.length];
			for (int j = 0; j < convexCoordinates.length; j++) {
				Coordinate co = new Coordinate(convexCoordinates[j][0],
						convexCoordinates[j][1]);
				jtsCoordinates[j] = co;
			}

			CoordinateSequence points = new CoordinateArraySequence(
					jtsCoordinates);
			//System.out.println("Points: " + points.size());			
			LinearRing ring = new LinearRing(points, factory);
			Polygon poly = new Polygon(ring, null, factory);

			collection[i] = poly;
			//			if (this.geometry == null) {
			//				this.geometry = poly;
			//			} else {
			//				this.geometry = this.geometry.union(poly);
			//			}
		}

		//this.geometry = new GeometryCollection(collection, new GeometryFactory());
		//this.geometry.normalize();
		this.geometry = new MultiPolygon(collection, new GeometryFactory());
		this.geometry.normalize();
	}
	
	public String toConstraints() //throws ConversionException 
	{
		if (this.geometry.isEmpty())
			return "";

		if (!EnableConstraintRepresentation) {
			return "Constraint representation is disabled.";
		}

		//Polyhedron poly = new Polyhedron(this.geometry);
		//return poly.toConstraints();
		return "";
	}

	public String toString() {
		return this.geometry.toString();
	}

	public String toText() {
		return this.geometry.toText();
	}

	public byte[] toWKB() {
		return jts.WKBwrite(this.geometry);		
	}

	public String toWKT() {
		return jts.WKTwrite(this.geometry);		
	}

	public byte[] toByteArray() {
		return jts.WKBwrite(this.geometry);
	}

	public static StrabonPolyhedron union(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);

		poly.geometry = A.geometry.union(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	public static StrabonPolyhedron buffer(StrabonPolyhedron A, double B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		poly.geometry = A.geometry.buffer(B);

		return poly;
	}

	public static StrabonPolyhedron envelope(StrabonPolyhedron A) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		poly.geometry = A.geometry.getEnvelope();

		return poly;
	}

	public static StrabonPolyhedron convexHull(StrabonPolyhedron A) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		poly.geometry = A.geometry.convexHull();

		return poly;
	}

	public static StrabonPolyhedron boundary(StrabonPolyhedron A) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		poly.geometry = A.geometry.getBoundary();

		return poly;
	}

	public static StrabonPolyhedron intersection(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		Geometry geo = A.geometry.intersection(x);
		geo.setSRID(targetSRID);
		return new StrabonPolyhedron(geo);
	}

	public static StrabonPolyhedron difference(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);

		poly.geometry = A.geometry.difference(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	public static StrabonPolyhedron symDifference(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		poly.geometry = A.geometry.symDifference(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	public static double area(StrabonPolyhedron A) throws Exception {
		return A.geometry.getArea();
	}

	public static double distance(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		return A.geometry.distance(x);
	}

	public static StrabonPolyhedron project(StrabonPolyhedron A, int[] dims) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron();
		ProjectionsFilter filter = new ProjectionsFilter(dims);
		A.geometry.apply(filter);
		A.geometry.geometryChanged();
		poly.geometry = A.geometry;
		return poly;
	}
	
	public static StrabonPolyhedron transform(StrabonPolyhedron A, URI srid) throws Exception {
		
		int parsedSRID = Integer.parseInt(srid.toString().substring(srid.toString().lastIndexOf('/')+1));
		Geometry converted = JTSWrapper.getInstance().transform(A.getGeometry(), A.getGeometry().getSRID(), parsedSRID);
		return new StrabonPolyhedron(converted);
	}

	/**
	 * Performs quick union between polygons or multipolygons.
	 * 
	 * @param A
	 * @param B
	 * @return
	 * @throws Exception
	 */
	public static StrabonPolyhedron quickUnion(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		System.out.println("Merging polyhedrons: A.coordinates=" + A.getGeometry().getCoordinates().length + 
				", B.coordinates=" + B.getGeometry().getCoordinates().length);

		StrabonPolyhedron poly = new StrabonPolyhedron();
		int polygons = 0;
		if (Polygon.class.isInstance(A.geometry)) {			
			polygons++;
		} else if (MultiPolygon.class.isInstance(A.geometry)) {
			polygons += ((MultiPolygon)(A.geometry)).getNumGeometries();
		}
		if (Polygon.class.isInstance(B.geometry)) {
			polygons++;
		} else if (MultiPolygon.class.isInstance(B.geometry)) {
			polygons += ((MultiPolygon)(B.geometry)).getNumGeometries();
		}

		assert (polygons >= 2);

		int index = 0;
		Polygon[] polys = new Polygon[polygons];

		if (Polygon.class.isInstance(A.geometry)) {
			polys[index] = (Polygon)(A.geometry);
			index++;
		}
		if (Polygon.class.isInstance(B.geometry)) {
			polys[index] = (Polygon)(B.geometry);
			index++;
		}
		if (MultiPolygon.class.isInstance(A.geometry)) {
			MultiPolygon multi = (MultiPolygon)(A.geometry);
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				polys[index] = (Polygon)multi.getGeometryN(i);
				index++;
			}
		}
		if (MultiPolygon.class.isInstance(B.geometry)) {
			MultiPolygon multi = (MultiPolygon)(B.geometry);
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				polys[index] = (Polygon)multi.getGeometryN(i);
				index++;
			}
		}

		poly.geometry = new MultiPolygon(polys, new GeometryFactory());

		return poly;
	}


	public StrabonPolyhedron getBuffer(double distance) throws Exception {
		Geometry geo = this.geometry.buffer(distance);
		return new StrabonPolyhedron(geo);
	}

	public StrabonPolyhedron getBoundary() throws Exception {
		Geometry geo = this.geometry.getBoundary();
		return new StrabonPolyhedron(geo);
	}

	public StrabonPolyhedron getEnvelope() throws Exception {
		Geometry geo = this.geometry.getEnvelope();
		return new StrabonPolyhedron(geo);
	}

	public double getArea() throws Exception {
		return this.getArea();
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

	public int getNumPoints() {
		return this.geometry.getNumPoints();
	}

	private static String FindGeoType(Geometry geo) {
		return 
				Point.class.isInstance(geo) ? "Point" :
					MultiPoint.class.isInstance(geo) ? "MultiPoint" :
						LineString.class.isInstance(geo) ? "LineString" :
							MultiLineString.class.isInstance(geo) ? "MultiLineString" :
								Polygon.class.isInstance(geo) ? "Polygon" :
									MultiPolygon.class.isInstance(geo) ? "MultiPolygon" :
										GeometryCollection.class.isInstance(geo) ? "GeometryCollection" : 
											"Unknown";
	}

	public String stringValue() {
		return this.toWKT();
	}

	@Override
	public boolean equals(Object other) {

		if(other instanceof StrabonPolyhedron)
		{
			if (((StrabonPolyhedron) other).geometry.equals(this.getGeometry()))
			{
				return true;
			}

		}
		return false;
	}
	
	public Geometry GMLReader(String GML) throws IOException, SAXException, ParserConfigurationException, JAXBException
	{
        StringReader reader = new StringReader(GML);
		JAXBContext context=JAXBContext.newInstance("org.jvnet.ogc.gml.v_3_1_1.jts");	
		//Point point = (Point) context.createUnmarshaller().unmarshal(getClass().getResourceAsStream(inputstream));
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Geometry geometry = (Geometry) unmarshaller.unmarshal(reader);
		if(geometry.getSRID()>0)
			 System.out.println("GML Geometry SRID: "+geometry.getSRID());
		reader.close();
        return  geometry;
	}
}
