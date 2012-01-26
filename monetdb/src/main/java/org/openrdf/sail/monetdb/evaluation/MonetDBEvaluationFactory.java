package org.openrdf.sail.monetdb.evaluation;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.query.Dataset;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.generaldb.GeneralDBConnection;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.rdbms.schema.TableFactory;

public class MonetDBEvaluationFactory extends GeneralDBEvaluationFactory{

	@Override
	public GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new MonetDBEvaluation(factory, triples, dataset, ids);
	}
}
