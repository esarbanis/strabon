package org.openrdf.sail.sqlite.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlCastBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlRegexBuilder;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public class SqliteQueryBuilderFactory extends GeneralDBQueryBuilderFactory {

	@Override
	public GeneralDBQueryBuilder createQueryBuilder() {
		GeneralDBQueryBuilder query = new SqliteQueryBuilder(createSqlQueryBuilder());
		query.setValueFactory(vf);
		query.setUsingHashTable(usingHashTable);
		return query;
	}

	@Override
	public GeneralDBSqlExprBuilder createSqlExprBuilder() {
		return new SqliteSqlExprBuilder(this);
	}

	@Override
	public GeneralDBSqlRegexBuilder createSqlRegexBuilder(GeneralDBSqlExprBuilder where) {
		return new SqliteSqlRegexBuilder(where, this);
	}
	
	@Override
	public GeneralDBSqlBracketBuilder createSqlBracketBuilder(GeneralDBSqlExprBuilder where) {
		return new SqliteSqlBracketBuilder(where, this);
	}

	@Override
	public GeneralDBSqlCastBuilder createSqlCastBuilder(GeneralDBSqlExprBuilder where, int type) {
		return new SqliteSqlCastBuilder(where, this, type);
	}
	
}