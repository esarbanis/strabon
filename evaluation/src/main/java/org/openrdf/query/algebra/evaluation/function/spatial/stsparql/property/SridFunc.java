/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
 
/**
 * A spatial function returning the SRID of a geometry.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class SridFunc extends SpatialPropertyFunc {

	@Override
	public String getURI() {
		return GeoConstants.srid.toString(); //changed this-constant
	}
}
