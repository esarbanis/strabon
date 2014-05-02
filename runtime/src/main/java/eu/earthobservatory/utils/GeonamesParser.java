/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.utils;

import info.aduna.iteration.CloseableIteration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;

import org.openrdf.sail.memory.MemoryStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeonamesParser {

	static int pointsOut = 0;
	static int e = 0, a = 0;

	public static void main(String[] args) throws IOException, SchemaException,
			NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {

		if (args.length < 2) {
			System.err.println("Usage: eu.earthobservatory.utils.GeonamesParser <IN_FILE> <OUT_FILE>");
			System.exit(0);
		}
		final String inFile = args[0];
		final String outFile = args[1]; 
		final String baseURI = "http://www.geonames.org";

		String greece = "POLYGON((" +
				"19.2214041948412 39.915644583545,19.2214041948412 39.915644583545,19.181097387078 39.9237059450676," +
				"19.5519200182563 40.0285036451009,19.6567177185669 39.9156445835922,20.1968289424263 39.9075832219288," +
				"20.3741788961446 40.1655467910226,20.6805106346546 40.4073876369722,20.8497992263774 40.94749885984," +
				"21.6962421887069 41.0845420054562,21.8897148655894 41.2538305976083,22.5910533201724 41.2780146817553," +
				"22.5829919583984 41.4231191894311,23.1150418204718 41.4473032736882,23.5987235131646 41.5279168887397," +
				"23.896993890336 41.5682236962285,24.3967983060956 41.6568986728098,24.7434368528031 41.5359782495121," +
				"25.331916245936 41.3989351029729,25.9687638080256 41.4553646333056,26.0574387848902 41.5440396101636," +
				"25.9284569997944 41.7536350102425,26.1944819306803 41.8423099869814,26.6297954543722 41.7375122866789," +
				"26.7426545167742 41.2941374019925,26.4685682243736 41.1812783406592,26.5169363940019 40.9716829406332," +
				"26.0896842326308 40.6008603101938,25.5415116482987 40.0688104490916,26.4927523118004 39.4642083330579," +
				"26.7265317973552 39.0288948098343,26.4040773361785 38.5693972024431,27.1537839611879 37.8599973865071," +
				"28.451663172698 36.4734432006192,28.0082882886197 35.6834297705082,29.5560697054562 36.3605841370316," +
				"29.9671991450442 36.0864978436166,27.0570476273072 34.6596368562464,24.0904665776989 34.5870846050009," +
				"21.9219603198949 35.538325268111,19.2214041948412 39.915644583545" +
				"))";

		Repository myRepository = new SailRepository(new MemoryStore());
		BufferedReader dis = null;
		RepositoryConnection conn = null;
		boolean isEven = false;

		Geometry greeceGeo = null;
		try {
			//mbbInWGS84 = new WKTReader().read("POLYGON((-10.6700 34.5000, 31.5500 34.5000, 31.5500 71.0500, -10.6700 71.0500, -10.6700 34.5000))");
			greeceGeo = new WKTReader().read(greece);
			greeceGeo.setSRID(4326);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		// System.out.println("|--MBB Created--|");

		// // -- Display map of Greece and its mbb -- //
		// SimpleFeatureCollection mbbCollection = createCollection(mbbInWGS84);
		// MapContext map = new DefaultMapContext();
		// map.setTitle("Map");
		// map.addLayer(clcFeatureCollection, null);
		// map.addLayer(mbbCollection, null);
		// JMapFrame.showMap(map);

		try {
			// Initialization of classes used for RDF handling
			myRepository.initialize();
			conn = myRepository.getConnection();
			conn.setAutoCommit(true);

			dis = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(inFile))));
			BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "UTF-8"));
			// BufferedWriter osw = new BufferedWriter(new
			// OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			RDFWriter wr = new TurtleWriter(osw);
			String line = null;

			// Adding data to repository
			wr.startRDF();
			while (dis.ready()) {
				line = dis.readLine();
				if (isEven) {
					try {
						conn.clear();
						//ByteArrayInputStream bis = new ByteArrayInputStream(line.getBytes());
						ByteArrayInputStream bis = new ByteArrayInputStream(line.getBytes("UTF-8"));
						conn.add(bis, baseURI, org.openrdf.rio.RDFFormat.RDFXML);
						exportGeoname(wr, conn, greeceGeo);
					} catch (RDFParseException ex) {
						System.err.println(line);
						e++;
					}
				} else {
					a++;
				}
				isEven = !isEven;
			}
			wr.endRDF();
			dis.close();

			System.out.println("Rejected features: " + e);
			System.out.println("Parsed features: " + a);
			System.out.println("Points out of MBB of Greece: " + pointsOut);
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create a SimpleFeatureCollection with a Geometry
	 * 
	 * @param all
	 * @return
	 * @throws SchemaException
	 */
	public static SimpleFeatureCollection createCollection(Geometry g)
			throws SchemaException {

		SimpleFeatureCollection collection = FeatureCollections.newCollection();
		SimpleFeatureType TYPE = DataUtilities.createType("MBB",
				"location:Polygon:srid=4326"); // 4326 = srid of wgs84
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

		featureBuilder.add(g);
		SimpleFeature feature = featureBuilder.buildFeature(null);
		collection.add(feature);

		return collection;
	}

//	public static Geometry getMBB(String filename) throws IOException,
//			SchemaException, NoSuchAuthorityCodeException, FactoryException {
//		SimpleFeatureCollection featureCollection = getFeatureCollection(
//				filename, "ISO8859-7");
//
//		SimpleFeatureIterator featureIterator = featureCollection.features();
//
//		MultiPolygon all = null;
//		// Iterate features of shp file
//		while (featureIterator.hasNext()) {
//			SimpleFeature f = featureIterator.next();
//			List<Object> attributes = f.getAttributes();
//
//			MultiPolygon landItem = (MultiPolygon) attributes.get(0);
//
//			if (all == null)
//				all = landItem;
//			else
//				all = (MultiPolygon) all.union(landItem);
//		}
//
//		return all.getEnvelope();
//	}

	/**
	 * @param filename
	 *            shp file to be opened
	 * @param encoding
	 *            encoding of opened shp file
	 * @return a feature collection of opened shp file
	 * @throws IOException
	 * @throws FactoryException
	 * @throws NoSuchAuthorityCodeException
	 */
	public static SimpleFeatureCollection getFeatureCollection(String filename,
			String encoding) throws IOException, NoSuchAuthorityCodeException,
			FactoryException {
		// Open file
		File infile = new File(filename);

		// Parameters of ShapefileDatastore
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(ShapefileDataStoreFactory.URLP.key, infile.toURI().toURL());
		// params.put("create spatial index", Boolean.TRUE);
		params.put(ShapefileDataStoreFactory.DBFCHARSET.key, encoding);

		// -- Create ShapefileDatastore -- //
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore shapefile = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);
		// shapefile.setStringCharset(Charset.forName("ISO8859-7"));
		// DataStore shapefile = new ShapefileDataStore( infile.toURI().toURL(), false);

		// Get feature collection
		String[] typeName = shapefile.getTypeNames();
		SimpleFeatureSource featureSource = shapefile
				.getFeatureSource(typeName[0]);
		//SimpleFeatureCollection collection;
		SimpleFeatureCollection featureCollection = featureSource.getFeatures();

		// SimpleFeatureIterator clcFeatureIterator =
		// clcFeatureCollection.features();
		return featureCollection;
	}

	// public static void showMap(String filename) throws IOException {
	//
	// File file = new File(filename);
	//
	// FileDataStore store = FileDataStoreFinder.getDataStore(file);
	// SimpleFeatureSource featureSource = store.getFeatureSource();
	//
	// // Create a map context and add our shapefile to it
	// MapContext map = new DefaultMapContext();
	// map.setTitle("Quickstart");
	// map.addLayer(featureSource, null);
	//
	// // Now display the map
	// JMapFrame.showMap(map);
	// }

	/**
	 * @param featureIterator
	 *            iterator of features to print
	 * @throws IOException
	 */
	public static void printFeatures(SimpleFeatureIterator featureIterator)
			throws IOException {

		// Iterate features of shp file
		while (featureIterator.hasNext()) {
			SimpleFeature f = featureIterator.next();
			List<Object> attributes = f.getAttributes();

			for (int i = 0; i < attributes.size(); i++) {
				// MultiLineString geometry =
				// (MultiLineString)attributes.get(0);
				Object attribute = attributes.get(i);
				if (!(attribute instanceof MultiLineString))
					System.out.println(attributes.get(i).toString());
			}
			System.out.println("---------");
		}
	}

	public static void exportGeoname(RDFHandler handler,
			RepositoryConnection conn, Geometry greeceGeo)
			throws RepositoryException, RDFHandlerException {

		final URI hasGeography = new URIImpl(
				"http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeography");
		final URI geometry = new URIImpl("http://strdf.di.uoa.gr/ontology#WKT");
		URI latPredicate = new URIImpl(
				"http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		URI longPredicate = new URIImpl(
				"http://www.w3.org/2003/01/geo/wgs84_pos#long");

		handler.handleNamespace("geo", "http://www.example.org/geo#");
		handler.handleNamespace("strdf", "http://strdf.di.uoa.gr/ontology#");
		handler.handleNamespace("noa",
				"http://teleios.di.uoa.gr/ontologies/noaOntology.owl#");

		// Export namespace information
		CloseableIteration<? extends Namespace, RepositoryException> nsIter = conn
				.getNamespaces();
		try {
			while (nsIter.hasNext()) {
				Namespace ns = nsIter.next();
				handler.handleNamespace(ns.getPrefix(), ns.getName());
			}
		} finally {
			nsIter.close();
		}

		// Export statements
		CloseableIteration<? extends Statement, RepositoryException> stIter = conn
				.getStatements(null, null, null, false);

		try {
			Value latV = null, longV = null;
			Statement st = null;
			ArrayList<Statement> stL = new ArrayList<Statement>();
			Resource sub = null;
			while (stIter.hasNext()) {
				st = stIter.next();
				URI p = st.getPredicate();

				if (p.equals(latPredicate)) {
					sub = st.getSubject();
					latV = st.getObject();
				} else if (p.equals(longPredicate)) {
					longV = st.getObject();
				} else {
					stL.add(st);
				}
			}
			st = new StatementImpl(sub, hasGeography, new LiteralImpl("POINT("
					+ longV.stringValue() + " " + latV.stringValue() + ")",
					geometry) // TODO einai swsth h seira, nomizw nai ??
			);
			stL.add(st);

			GeometryFactory geometryFactory = JTSFactoryFinder
					.getGeometryFactory(null);
			Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(
					//latV.stringValue()), Double.parseDouble(longV.stringValue()))
					longV.stringValue()), Double.parseDouble(latV.stringValue()))
			);
			// TODO To parapanw nomizw einai swsto edw giati ta pairnei
			// anapoda??

			if (greeceGeo.contains(point)) {
				ListIterator<Statement> stLI = stL.listIterator();
				while (stLI.hasNext()) {
					handler.handleStatement(stLI.next());
				}
			} else
				pointsOut++;

			System.out.println(a + ": " + e + " - " + pointsOut);
		} finally {
			stIter.close();
		}
	}
}