package org.openrdf.query.algebra.evaluation.function.spatial;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

public class SpatialPropertyFunc implements Function {


	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args)
	throws ValueExprEvaluationException {

		return null;
	}

	//I'll have everyone override it
	public String getURI() {

		return null;
	}

}
