package org.openrdf.sail.sqlite.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class SqliteSqlBracketBuilder extends SqliteSqlExprBuilder implements GeneralDBSqlBracketBuilder {
	
	private SqliteSqlExprBuilder where;

	private String closing = ")";

	public SqliteSqlBracketBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
		super(factory);
		this.where = (SqliteSqlExprBuilder)where;
		append("(");
	}

	public String getClosing() {
		return closing;
	}

	public void setClosing(String closing) {
		this.closing = closing;
	}

	public SqliteSqlExprBuilder close() {
		append(closing);
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}
}
