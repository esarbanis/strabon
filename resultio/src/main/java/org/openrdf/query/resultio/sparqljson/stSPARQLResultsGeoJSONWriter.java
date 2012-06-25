package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.geojson.org/geojson-spec.html/">GeoJSON Format</a>.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class stSPARQLResultsGeoJSONWriter implements TupleQueryResultWriter {

	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.sparqljson.stSPARQLResultsGeoJSONWriter.class);
	
	/**
	 * The underlying output stream to write
	 */
	private OutputStream out;
	
	/**
	 * Set a Feature Collection
	 */
	private SimpleFeatureCollection sfCollection;
	
	/**
	 * The wrapper of JTS library
	 */
	private JTSWrapper jts;
	
	/**
	 * Keep track of the number of results
	 */
	private int nresults;
	
	/**
	 * The class to use for serializing to GeoJSON
	 */
	private FeatureJSON fjson;
	
	/**
	 * True to write a warning when there are no features
	 * in the answer
	 */
	private boolean warn;
	
	public stSPARQLResultsGeoJSONWriter(OutputStream out) {
		this.out = out;
		
		// set the feature collection
		sfCollection = FeatureCollections.newCollection("geomOutput");
		
		// get the instance of JTSWrapper
		jts = JTSWrapper.getInstance();
		
		// initialize results
		nresults = 0;
		
		warn = false;
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		fjson = new FeatureJSON();
		fjson.setEncodeFeatureCRS(true);
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			fjson.writeFeatureCollection(sfCollection, out);
			out.write("\n".getBytes(Charset.defaultCharset()));
			
			if (warn) {
				logger.warn("[Strabon.GeoJSONWriter] No spatial binding found in the result, hence the result is empty eventhough query evaluation produced {} results. GeoJSON requires that at least one binding maps to a geometry.", nresults);
				
			}
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			// we might have more than one feature in the result, so we count them
			int nfeatures = 0;
			
			nresults++;
			
			// list keeping binding names that are not binded to geometries
			ArrayList<String> properties = new ArrayList<String>();
			
			// list keeping values for binding names
			ArrayList<Value> values = new ArrayList<Value>();
			
			// list keeping the features of the result
			ArrayList<SimpleFeatureTypeBuilder> features = new ArrayList<SimpleFeatureTypeBuilder>();
			
			// list keeping the geometries of features
			ArrayList<Geometry> geometries = new ArrayList<Geometry>();
			
			// parse binding set
			for (Binding binding : bindingSet) {
				Value value = binding.getValue();
				if (value instanceof Literal) {
					Literal litValue = (Literal) value;

					// it's a spatial literal
					if (litValue.getDatatype().stringValue().equals(StrabonPolyhedron.ogcGeometry)) {
						// TODO Check for GML (when added to StrabonPolyhedron)
						
						nfeatures++;
						
						if (logger.isDebugEnabled()) {
							logger.debug("[Strabon] Found geometry: {}", litValue);
						}
						
						// get the geometry and SRID
						Geometry geom = null;
						int srid = -1;
						
						// TODO add dependency to pom.xml for GeneralDBPolyhedron 
						// Regardless of our geometry's input, we need its SRID
//						if (unparsedGeometry instanceof GeneralDBPolyhedron) {
//							geom = ((GeneralDBPolyhedron) value).getPolyhedron().getGeometry();
//							srid = ((GeneralDBPolyhedron) value).getPolyhedron().getGeometry().getSRID();
//							
//						} else { // WKT
							// get the WKT as it is present in the result
							String wkt = value.stringValue();
							
							// get its geometry
							geom = jts.WKTread(WKTHelper.getWithoutSRID(wkt));
							
							// get its SRID
							srid = WKTHelper.getSRID(wkt);
								
							// TODO: GML support to be added
						//}
					
						SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
						sftb.setName("Feature_" + nresults + "_" + nfeatures);
						sftb.setCRS(CRS.decode("EPSG:" + srid));
						sftb.setSRS("EPSG:" + srid);
						sftb.add("geometry", Geometry.class);
						
						// add the feature in the list of features
						features.add(sftb);
						
						// add the geometry of the feature in the list of geometries
						geometries.add(geom);
						
					
					} else { // Literal other than geometry
						if (logger.isDebugEnabled()) {
							logger.debug("[Strabon.GeoJSONWriter] Found Literal: {}", value);
						}

						properties.add(binding.getName());
						values.add(value);
					}
					
				} else { // URI or BlankNode
					if (logger.isDebugEnabled()) {
						logger.debug("[Strabon.GeoJSONWriter] Found URI/BlankNode: {}", value);
					}
					
					properties.add(binding.getName());
					values.add(value);
				}
			}
			
			// construct the feature of the result
			for (int i = 0; i < features.size(); i++) {
				SimpleFeatureTypeBuilder sftb = features.get(i);
				
				// add the properties
				for (int p = 0; p < properties.size(); p++) {
					sftb.add(properties.get(p), String.class);
				}
				
				SimpleFeatureType featureType = sftb.buildFeatureType();
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);				
				
				// add the geometry to the builder of the feature
				featureBuilder.add(geometries.get(i));
				
				// add the values to the builder of the feature
				for (int v = 0; v < values.size(); v++) {
					featureBuilder.add(values.get(v));
				}
				
				SimpleFeature feature = featureBuilder.buildFeature(null);
				sfCollection.add(feature);	
			}
			
			if (nfeatures == 0) {
				warn = true;
			}
			
		} catch (Exception e) {
			throw new TupleQueryResultHandlerException(e);
		}
					
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.GEOJSON;
	}
}
