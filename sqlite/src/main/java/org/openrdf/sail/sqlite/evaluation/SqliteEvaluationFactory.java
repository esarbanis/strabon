package org.openrdf.sail.sqlite.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;

public class SqliteEvaluationFactory extends GeneralDBEvaluationFactory{

	@Override
	public GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new SqliteEvaluation(factory, triples, dataset, ids);
	}
}
