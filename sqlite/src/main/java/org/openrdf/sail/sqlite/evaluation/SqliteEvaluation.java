/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.sqlite.evaluation;

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
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.sqlite.iteration.SqliteBindingIteration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the default strategy by accepting {@link GeneralDBSelectQuery} and evaluating
 * them on a database.
 * 
 * @author Dimitris Bilidas <grad0903@di.uoa.gr>
 * 
 */
public class SqliteEvaluation extends GeneralDBEvaluation {

	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.sail.sqlite.evaluation.SqliteEvaluation.class);
	
	public SqliteEvaluation(GeneralDBQueryBuilderFactory factory, GeneralDBTripleRepository triples, Dataset dataset,
			IdSequence ids)
	{
		super(factory, triples, dataset, ids);
		this.factory = factory;
	}

	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluate(GeneralDBSelectQuery qb, BindingSet b)
		throws UnsupportedRdbmsOperatorException, RdbmsQueryEvaluationException
	{
		List<Object> parameters = new ArrayList<Object>();
		try {
			QueryBindingSet bindings = new QueryBindingSet(b);
			String query = toQueryString(qb, bindings, parameters);
			//String q2=new String();
			//q2=query;
			//q2=q2.replaceAll("\\?", "30.0");
			try {
				Connection conn = triples.getConnection();
			//	Statement st=conn.createStatement();
			//	st.executeQuery(q2);
				PreparedStatement stmt = conn.prepareStatement(query);
				int p = 0;
				for (Object o : parameters) {
					stmt.setObject(++p, o);
				}
				Collection<GeneralDBColumnVar> proj = qb.getProjections();
//				System.out.println("In PostGIS Evaluation, query is: \n" + stmt);
				GeneralDBBindingIteration result = new SqliteBindingIteration(stmt);
				result.setProjections(proj);
				result.setBindings(bindings);
				result.setValueFactory(vf);
				result.setIdSequence(ids);
				//XXX addition
				result.setGeoNames(this.geoNames);
				result.setConstructIndexesAndNames(this.constructIndexesAndNames);
				
				if (logger.isDebugEnabled()) {
					logger.debug("In SQLite Evaluation, query is: \n{}", stmt);
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
