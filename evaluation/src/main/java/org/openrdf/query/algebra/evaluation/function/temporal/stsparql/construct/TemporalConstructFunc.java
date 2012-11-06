/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.temporal.stsparql.construct;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public abstract class TemporalConstructFunc implements Function {
	

	public Value evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {

				return null;
			}

		
	public abstract  String getURI();
	
	/*In Postgres Temporal it seems that an operator is assigned to every PERIOD function 
	 * I store this information in the declaration of each function so that i can use it in the mapping
	 * of stSPARQL queries to spatiotemporally extended SQL queries
	 * */
	public abstract  String getOperator();
	
	//This method returns the respective function of the Postgres Temporal extension
	public abstract  String getPostgresFunction();

}

