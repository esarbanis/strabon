package org.openrdf.sail.postgis.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class PostGISSqlBracketBuilder extends PostGISSqlExprBuilder implements GeneralDBSqlBracketBuilder {
	
	private PostGISSqlExprBuilder where;

	private String closing = ")";

	public PostGISSqlBracketBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
		super(factory);
		this.where = (PostGISSqlExprBuilder)where;
		append("(");
	}

	public String getClosing() {
		return closing;
	}

	public void setClosing(String closing) {
		this.closing = closing;
	}

	public PostGISSqlExprBuilder close() {
		append(closing);
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}
}
