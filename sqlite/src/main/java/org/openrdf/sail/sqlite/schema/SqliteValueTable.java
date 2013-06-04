package org.openrdf.sail.sqlite.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteValueTable extends org.openrdf.sail.generaldb.GeneralDBSqlValueTable{
	public List<Long> maxIds(int shift, int mod)
			throws SQLException
			{
		String column = "id";
		StringBuilder expr = new StringBuilder();
		expr.append("((").append(column);
		expr.append(" >> ").append(shift);
		expr.append(") + ").append(mod).append(")  %  ");
		expr.append(mod);
		expr.append("");
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append("MAX(");
		sb.append(column);
		sb.append("), ").append(expr).append(" AS grp");
		sb.append("\nFROM ").append(getName());
		sb.append("\nGROUP BY grp");
		String query = sb.toString();
		PreparedStatement st = this.getRdbmsTable().prepareStatement(query);
		try {
			ResultSet rs = st.executeQuery();
			try {
				List<Long> result = new ArrayList<Long>();
				while (rs.next()) {
					result.add(rs.getLong(1));
				}
				return result;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
			}
}
