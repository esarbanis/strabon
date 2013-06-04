package org.openrdf.sail.sqlite;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.sail.generaldb.schema.ValueTableFactory;

public class SqliteTripleTableManager extends org.openrdf.sail.generaldb.managers.TripleTableManager{

	public SqliteTripleTableManager(ValueTableFactory factory) {
		super(factory);
	}
	
	//must override this method because metadata.getColumns() does not
	//work properly for sqlite jdbc driver
	@Override
	protected Set<String> findPredicateTableNames()
			throws SQLException
		{
			Set<String> names = findAllTables();
			Set<String> result=new HashSet<String>();
			
			for(String name:names){
				Statement st=conn.createStatement();
				ResultSet rs=st.executeQuery("pragma table_info ('" + name + "');");
				int matched=0;
				while(rs.next()){
					
					String colName=rs.getString(2).toLowerCase();
					if(colName.equals("ctx")||colName.equals("subj")||colName.equals("obj")){
						matched++;
						if(matched>2){
							result.add(name);
							continue;
						}
					}
				}
			}
			return result;
		}

}
