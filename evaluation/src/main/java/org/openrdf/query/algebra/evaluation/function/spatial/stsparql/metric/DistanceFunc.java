/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;

/**
 * A spatial function computing the distance between two geometries.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class DistanceFunc extends SpatialMetricFunc {

	@Override
	public String getURI() {
		return GeoConstants.distance;
		}
}
