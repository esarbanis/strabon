/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
 
/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class TransformFunc extends SpatialConstructFunc {

	@Override
	public String getURI() {
		return StrabonPolyhedron.transform;
	}
}
