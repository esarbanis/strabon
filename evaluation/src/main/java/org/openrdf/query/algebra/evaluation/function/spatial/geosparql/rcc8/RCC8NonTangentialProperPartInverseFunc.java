/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.rcc8;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlRelation;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.EqualsFunc;


public class RCC8NonTangentialProperPartInverseFunc extends GeoSparqlRelation {

	@Override
	public String getURI() {
		return StrabonPolyhedron.rccNonTangentialProperPartInverse;
		}

}
