/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.evaluation;

import info.aduna.iteration.CloseableIteration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.postgis.iteration.PostGISBindingIteration;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.slf4j.LoggerFactory;

/**
 * Extends the default strategy by accepting {@link GeneralDBSelectQuery} and evaluating
 * them on a database.
 * 
 * @author James Leigh
 * 
 */
public class PostGISEvaluation extends GeneralDBEvaluation {


	public PostGISEvaluation(GeneralDBQueryBuilderFactory factory, GeneralDBTripleRepository triples, Dataset dataset,
			IdSequence ids)
	{
		super(factory, triples, dataset, ids);
		logger = LoggerFactory.getLogger(PostGISEvaluation.class);
		this.factory = factory;
	}

	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluate(GeneralDBSelectQuery qb, BindingSet b)
		throws UnsupportedRdbmsOperatorException, RdbmsQueryEvaluationException
	{
		List<Object> parameters = new ArrayList<Object>();
		try {
			QueryBindingSet bindings = new QueryBindingSet(b);
			String query = toQueryString(qb, bindings, parameters);
			try {
				Connection conn = triples.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				int p = 0;
				for (Object o : parameters) {
					stmt.setObject(++p, o);
				}
				Collection<GeneralDBColumnVar> proj = qb.getProjections();
//				System.out.println("In PostGIS Evaluation, query is: \n" + stmt);
				GeneralDBBindingIteration result = new PostGISBindingIteration(stmt);
				result.setProjections(proj);
				result.setBindings(bindings);
				result.setValueFactory(vf);
				result.setIdSequence(ids);
				//XXX addition
				result.setGeoNames(this.geoNames);
				result.setConstructIndexesAndNames(this.constructIndexesAndNames);
				//XXX addition- constant
				if (logger.isDebugEnabled()) {
					logger.debug("In PostGIS Evaluation, query is: \n{}", stmt);
				}
				return result;
			}
			catch (SQLException e) {
				throw new RdbmsQueryEvaluationException(e.toString() + "\n" + query, e);
			}
		}
		catch (RdbmsException e) {
			throw new RdbmsQueryEvaluationException(e);
		}
	}
}
