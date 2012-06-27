/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.sf;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlRelation;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class SimpleFeaturesCrossesFunc extends GeoSparqlRelation {

	@Override
	public String getURI() {
		return GeoConstants.sfCrosses;
	}
}
