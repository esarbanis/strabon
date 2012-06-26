package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlNonTopologicalConstruct;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeoSparqlIntersectionFunc extends GeoSparqlNonTopologicalConstruct {

	@Override
	public String getURI() {
		return StrabonPolyhedron.geoSparqlIntersection;
	}
}
