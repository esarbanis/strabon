/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
 
/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class CoveredByFunc extends SpatialRelationshipFunc {

	@Override
	public String getURI() {
		return StrabonPolyhedron.coveredBy;
	}
}
