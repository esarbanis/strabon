package org.openrdf.sail.generaldb.algebra;
 
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * 
 * @author manolee
 * The following functions have been defined for returning some properties of a
 *geometry
 */

public class GeneralDBSqlSpatialProperty extends UnaryGeneralDBOperator
{


	/*CONSTRUCTOR*/

	public GeneralDBSqlSpatialProperty(GeneralDBSqlExpr arg) {
		super(arg);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
	throws X
	{
		visitor.meet(this);
	}

}
