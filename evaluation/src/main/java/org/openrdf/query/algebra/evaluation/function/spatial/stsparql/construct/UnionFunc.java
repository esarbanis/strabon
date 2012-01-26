package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;

public class UnionFunc extends SpatialConstructFunc {

	@Override
	public String getURI() {
		return StrabonPolyhedron.union;
		}
}
