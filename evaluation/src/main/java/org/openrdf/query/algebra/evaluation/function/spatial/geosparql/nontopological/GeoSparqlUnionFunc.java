package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlNonTopologicalConstruct;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeoSparqlUnionFunc extends GeoSparqlNonTopologicalConstruct {

	@Override
	public String getURI() {
		return GeoConstants.geoSparqlUnion;
	}
}
