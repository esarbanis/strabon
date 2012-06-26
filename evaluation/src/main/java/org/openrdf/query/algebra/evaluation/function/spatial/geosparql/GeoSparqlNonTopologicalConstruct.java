/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;

/**
 * This class represents a spatial function like the one defined in its superclass 
 * {@link SpatialConstructFunc} class for the case of stSPARQL.
 * 
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological}
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public abstract class GeoSparqlNonTopologicalConstruct extends SpatialConstructFunc {

	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args)
	throws ValueExprEvaluationException {
		return null;
	}

	// charnik: made the method abstract
	public abstract String getURI();
}
