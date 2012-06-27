/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
 
/**
 * A spatial function for testing whether a geometry is simple.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class IsSimpleFunc extends SpatialPropertyFunc {

	@Override
	public String getURI() {
		return GeoConstants.isSimple;
	}
}
