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

import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructBinary;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class GeneralDBSqlPeriodEnd extends GeneralDBSqlTemporalConstructUnary{

	public GeneralDBSqlPeriodEnd(GeneralDBSqlExpr arg) {
		super(arg);
	}


	/* (non-Javadoc)
	 * @see org.openrdf.sail.generaldb.algebra.temporal.GneralDBSqlTemporalConstructBinary#getPostgresFunction()
	 */
	@Override
	public String getPostgresFunction() {

		return "last";
	}
	/* (non-Javadoc)
	 * @see org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelNodeBase#visit(org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase)
	 */
	@Override
	public <X extends Exception> void visit(
			GeneralDBQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
		
	}

}