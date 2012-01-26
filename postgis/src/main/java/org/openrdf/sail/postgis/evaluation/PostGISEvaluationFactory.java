package org.openrdf.sail.postgis.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;

public class PostGISEvaluationFactory extends GeneralDBEvaluationFactory{

	@Override
	public GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new PostGISEvaluation(factory, triples, dataset, ids);
	}
}
