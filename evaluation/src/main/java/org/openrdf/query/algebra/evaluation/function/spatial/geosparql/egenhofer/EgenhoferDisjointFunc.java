/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.egenhofer;

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlRelation;


public class EgenhoferDisjointFunc extends GeoSparqlRelation {

	@Override
	public String getURI() {
		return GeoConstants.ehDisjoint;
		}

}
