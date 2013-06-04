package org.openrdf.sail.sqlite.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlRegexBuilder;

public class SqliteSqlRegexBuilder extends GeneralDBSqlRegexBuilder {

	public SqliteSqlRegexBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
		super(where, factory);
	}

	@Override
	protected void appendRegExp(GeneralDBSqlExprBuilder where) {
		appendValue(where);
		where.append(" REGEXP ");//must define the corresponding function
		appendPattern(where);
	}

}