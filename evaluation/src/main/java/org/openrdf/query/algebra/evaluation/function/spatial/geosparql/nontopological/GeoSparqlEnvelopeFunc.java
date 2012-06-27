/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlNonTopologicalConstruct;
 
/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeoSparqlEnvelopeFunc extends GeoSparqlNonTopologicalConstruct {

	@Override
	public String getURI() {
		return GeoConstants.geoSparqlEnvelope;
	}
}
