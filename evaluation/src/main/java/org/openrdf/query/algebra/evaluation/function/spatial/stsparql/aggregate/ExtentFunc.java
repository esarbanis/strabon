package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class ExtentFunc extends SpatialConstructFunc {

	@Override
	public String getURI() {
		return GeoConstants.extent;
	}
}
