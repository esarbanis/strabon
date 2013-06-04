package org.openrdf.sail.sqlite;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class SqliteSqlTable extends GeneralDBSqlTable {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SqliteSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
//		return "VACUUM ANALYZE " + getName();
		return null; // TODO vacuum analyze in sqliie
	}

	@Override
	protected String buildClear() {
//		return "TRUNCATE " + getName();
		return "DELETE FROM "+ getName();
	}
	
	@Override
	public String buildGeometryCollumn() {
		return "SELECT AddGeometryColumn('geo_values','strdfgeo',4326,'GEOMETRY',2)";
	}
	
	@Override
	public String buildIndexOnGeometryCollumn() {
		return "CREATE INDEX geoindex ON geo_values (strdfgeo)";
	}
	
	@Override
	public String buildInsertGeometryValue() {
		Integer srid=  GeoConstants.defaultSRID;
		return " (id, strdfgeo,srid) VALUES (?,ST_Transform(ST_GeomFromWKB(?,?),"+srid+"),?)";
	}
	
	@Override
	public String buildInsertValue(String type) {
		return " (id, value) VALUES ( ?, ?) ";
	}
	
	@Override
	protected String buildCreateTemporaryTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TEMPORARY TABLE ").append(getName());
		sb.append(" (\n").append(columns).append(")");
		return sb.toString();
	}
	
	@Override
	public String buildDummyFromAndWhere(String fromDummy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(fromDummy); 
		sb.append("\nWHERE 1=0");
		return sb.toString();
	}
	
	@Override
	public String buildDynamicParameterInteger() {
			return "?";
	}
	
	@Override
	public String buildWhere() {
		return " WHERE (1=1) ";
	}
	
	@Override
	public void primaryIndex(String... columns)
			throws SQLException
		{
		this.buildIndex(columns);
		}
	
	@Override
	public void index(String... columns)
			throws SQLException
		{
		try{
			if (columns.length == 1 && columns[0].equalsIgnoreCase("value")
					&& getName().toUpperCase().contains("LONG_"))
			{
				execute(buildLongIndex(columns));
			}
			else {
				execute(buildIndex(columns));
			}}
		catch(java.sql.SQLException e){
		if(e.getMessage().contains("already exists")){
			logger.warn(e.getMessage());
		}
		else{
			throw e;
		}
		}
		}
	
	@Override
	public void drop()
			throws SQLException
		{
			executeUpdate("INSERT INTO to_drop VALUES ('" + getName()+"')");
		}
}