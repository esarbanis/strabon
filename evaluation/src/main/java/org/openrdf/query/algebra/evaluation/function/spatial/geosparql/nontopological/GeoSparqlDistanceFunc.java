/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlNonTopologicalMetric;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeoSparqlDistanceFunc extends GeoSparqlNonTopologicalMetric {

	@Override
	public String getURI() {
		return StrabonPolyhedron.geoSparqlDistance;
	}
}
