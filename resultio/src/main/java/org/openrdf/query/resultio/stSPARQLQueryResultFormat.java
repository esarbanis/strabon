package org.openrdf.query.resultio;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the concept of an tuple query result serialization format for
 * stSPARQL/GeoSPARQL. Tuple query result formats are identified by a 
 * {@link #getName() name} and can have one or more associated MIME types, 
 * zero or more associated file extensions and can specify a (default) 
 * character encoding.
 * 
 * In contrast to formats mentioned in class {@link #TupleQueryResultFormat},
 * stSPARQL/GeoSPARQL formats do not adhere to any specification for SPARQL.
 * For example, the projected variables in a stSPARQL/GeoSPARQL query are
 * not included in the beginning of these formats. Instead, they are provided
 * as an additional description for a feature (e.g., a tuple query result with
 * a projected variable corresponding to a geometry). 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLQueryResultFormat extends TupleQueryResultFormat {

	/**
	 * KML format (see http://www.opengeospatial.org/standards/kml/)
	 */
	public static final stSPARQLQueryResultFormat KML = new stSPARQLQueryResultFormat("KML", 
			Arrays.asList("application/vnd.google-earth.kml+xml", "application/kml"), Charset.forName("UTF-8"), Arrays.asList("kml"));
	
	/**
	 * KMZ format (a zipped KML content)
	 */
	public static final stSPARQLQueryResultFormat KMZ = new stSPARQLQueryResultFormat("KMZ", 
			Arrays.asList("application/vnd.google-earth.kmz", "application/kmz"), Charset.forName("UTF-8"), Arrays.asList("kmz"));
	
	/**
	 * GeoJSON format (see http://www.geojson.org/geojson-spec.html)
	 */
	public static final stSPARQLQueryResultFormat GEOJSON = new stSPARQLQueryResultFormat("GeoJSON", 
			Arrays.asList("application/json", "application/json"), Charset.forName("UTF-8"), Arrays.asList("json"));
	
	// registers stSPARQL/GeoSPARQL formats
	static {
		register(KML);
		register(KMZ);
		register(GEOJSON);
	}
	
	public stSPARQLQueryResultFormat(String name, String mimeType, String fileExt) {
		super(name, mimeType, fileExt);
	}
	
	public stSPARQLQueryResultFormat(String name, String mimeType, Charset charset, String fileExt) {
		super(name, mimeType, charset, fileExt);
	}
	
	public stSPARQLQueryResultFormat(String name, Collection<String> mimeTypes, Charset charset, Collection<String> fileExtensions) {
		super(name, mimeTypes, charset, fileExtensions);
	}
}
