/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
 
/**
 * A spatial function computing the area of a geometry.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class AreaFunc extends SpatialMetricFunc {

	@Override
	public String getURI() {
		return StrabonPolyhedron.area;
	}
}
