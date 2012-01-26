package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlNonTopologicalConstruct;

public class GeoSparqlUnionFunc extends GeoSparqlNonTopologicalConstruct {

	@Override
	public String getURI() {
		return StrabonPolyhedron.geoSparqlUnion;
		}
}
