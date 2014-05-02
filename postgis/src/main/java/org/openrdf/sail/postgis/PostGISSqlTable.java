/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class PostGISSqlTable extends GeneralDBSqlTable {

	public PostGISSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
		return "VACUUM ANALYZE " + getName();
	}

	@Override
	protected String buildClear() {
		return "TRUNCATE " + getName();
	}
	
	@Override
	public String buildGeometryCollumn() {
		return "SELECT AddGeometryColumn('','geo_values','strdfgeo',4326,'GEOMETRY',2)";
	}
	
	@Override
	public String buildIndexOnGeometryCollumn() {
		return "CREATE INDEX geoindex ON geo_values USING GIST (strdfgeo)";
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
}