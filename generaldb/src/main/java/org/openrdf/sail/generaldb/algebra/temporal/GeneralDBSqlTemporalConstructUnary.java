/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.algebra.temporal;

import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public abstract class GeneralDBSqlTemporalConstructUnary extends UnaryGeneralDBOperator {
		
	/*In Postgres Temporal it seems that an operator is assigned to every PERIOD function 
	 * I store this information in the declaration of each function so that i can use it in the mapping
	 * of stSPARQL queries to spatiotemporally extended SQL queries
	 * */
	//This method returns the respective function of the Postgres Temporal extension
	public abstract  String getPostgresFunction();

	public GeneralDBSqlTemporalConstructUnary(GeneralDBSqlExpr arg) {
			super(arg);
		}

	
	}


