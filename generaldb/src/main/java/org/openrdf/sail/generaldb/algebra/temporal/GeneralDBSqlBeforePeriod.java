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

import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class GeneralDBSqlBeforePeriod extends GeneralDBSqlTemporal{

	public GeneralDBSqlBeforePeriod(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}

	/* (non-Javadoc)
	 * @see org.openrdf.sail.generaldb.algebra.temporal.GeneralDBSqlTemporal#getOperator()
	 */
	@Override
	public String getOperator() {
		
		return "<<";
	}

	/* (non-Javadoc)
	 * @see org.openrdf.sail.generaldb.algebra.temporal.GeneralDBSqlTemporal#getPostgresFunction()
	 */
	@Override
	public String getPostgresFunction() {
		
		return "before";
	}

}