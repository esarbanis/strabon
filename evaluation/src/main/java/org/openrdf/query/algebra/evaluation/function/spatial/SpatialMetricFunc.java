package org.openrdf.query.algebra.evaluation.function.spatial;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * This class represents a spatial function doing a metric computation
 * (e.g., calculating the area for a geometry, or the distance between
 * two geometries).
 * 
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric}
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public abstract class SpatialMetricFunc implements Function {

	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {

		return null;
	}

	// charnik: made method (and hence the class) abstract
	public abstract String getURI();
}
