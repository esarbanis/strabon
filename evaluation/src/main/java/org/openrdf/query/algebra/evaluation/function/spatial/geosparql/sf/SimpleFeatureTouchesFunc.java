/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.sf;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlRelation;


public class SimpleFeatureTouchesFunc extends GeoSparqlRelation {

	@Override
	public String getURI() {
		return StrabonPolyhedron.sfTouches;
		}

}
