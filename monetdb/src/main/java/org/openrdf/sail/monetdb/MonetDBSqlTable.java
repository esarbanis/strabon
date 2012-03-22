/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb;

import java.sql.SQLException;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.generaldb.GeneralDBSqlTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBSqlTable extends GeneralDBSqlTable {

	public MonetDBSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
//		return "VACUUM ANALYZE " + getName();
		return null; // TODO vacuum analyze in monetdb
	}

	@Override
	protected String buildClear() {
//		return "TRUNCATE " + getName();
		return "DELETE FROM "+ getName();
	}

	@Override
	public String buildGeometryCollumn() {
		return "ALTER TABLE geo_values ADD strdfgeo GEOMETRY";
	}
	
	@Override
	public String buildIndexOnGeometryCollumn() {
		return "CREATE INDEX geoindex ON geo_values (strdfgeo)";
	}
	
	@Override
	public String buildInsertGeometryValue() {
		Integer srid=  StrabonPolyhedron.defaultSRID;
		return " (id, strdfgeo,srid) VALUES (CAST(? AS INTEGER), Transform(GeomFromWKB(CAST(? AS BLOB),CAST(? AS INTEGER)),"+srid+"), CAST(? AS INTEGER))"; 
	}
	
	@Override
	public String buildInsertValue(String type) {
		return " (id, value) VALUES (CAST(? AS INTEGER), CAST( ? AS "+type+"))";
	}
	
	@Override
	protected String buildCreateTemporaryTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TEMPORARY TABLE ").append(getName());
		sb.append(" (\n").append(columns).append(")");
		sb.append(" ON COMMIT PRESERVE ROWS ");
		return sb.toString();
	}
	
	@Override
	public String buildDummyFromAndWhere(String fromDummy) {
		return "";
	}
	
	@Override
	public String buildDynamicParameterInteger() {
			return "CAST( ? AS INTEGER)";
	}
}