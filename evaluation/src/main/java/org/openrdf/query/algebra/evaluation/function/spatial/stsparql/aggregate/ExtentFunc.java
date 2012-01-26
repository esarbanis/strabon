package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;

public class ExtentFunc extends SpatialConstructFunc {

	@Override
	public String getURI() {
		return StrabonPolyhedron.extent;
		}
}
