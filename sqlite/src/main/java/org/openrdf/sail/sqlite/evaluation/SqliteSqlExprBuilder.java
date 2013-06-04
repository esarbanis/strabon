package org.openrdf.sail.sqlite.evaluation;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Assemblies an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class SqliteSqlExprBuilder extends GeneralDBSqlExprBuilder {

	public SqliteSqlExprBuilder(GeneralDBQueryBuilderFactory factory) {
		super(factory);
	}

	@Override
	public void appendBoolean(boolean booleanValue) {
		if (booleanValue) {
			where.append(" (1=1) ");
		}
		else {
			where.append(" (0=1) ");
		}
	}

	
	public void intersectsMBB() {

		//XXX
		//edw prepei na ginei allou eidous douleia!! oxi na mpei to mbbIntersects, 
		//alla na prostethei kati pou tha mou pei o kwstis
		where.append(" MbrOverlaps ");
	}
	
	public void equalsMBB() {

		where.append("MbrEqual");
	}

	public void containsMBB() {

		where.append("MbrContains");
	}

	public void insideMBB() {

		//den xerw akoma ti symbolo xreiazetai
		where.append("MbrWithin");
	}

	

	@Override
	public GeneralDBSqlExprBuilder appendNumeric(Number doubleValue) {
		where.append(" ? ");
		parameters.add(doubleValue);
		return this;
	}

	@Override
	public GeneralDBSqlExprBuilder number(Number time) {
		where.append(" ? ");
		parameters.add(time);
		return this;
	}
	
	@Override
	public GeneralDBSqlExprBuilder varchar(String stringValue) {
		if (stringValue == null) {
			appendNull();
		}
		else {
			where.append(" ? ");
			parameters.add(stringValue);
		}
		return this;
	}
	
//	// TODO should this be overriden ??
//	protected String getSqlNull() {
////		return "false"; // FIXME
//		return NULL;
//	}

}
