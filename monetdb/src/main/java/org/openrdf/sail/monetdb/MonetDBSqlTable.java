/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;

import eu.earthobservatory.constants.GeoConstants;

/**
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
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
		Integer srid=  GeoConstants.defaultSRID;
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
	
	@Override
	public String buildWhere() {
		return " WHERE (1=1 OR 1=1) ";
	}



	@Override
	public String buildIndexOnPeriodColumn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String buildInsertPeriodValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String buildPeriodCollumn() {
		// TODO Auto-generated method stub
		return null;
	}
}