/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.lang.IllegalArgumentException;
import org.openrdf.sail.generaldb.exceptions.conversionException;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;

/**
 * A Facade to the five literal value tables. Which are labels, languages,
 * datatypes, numeric values, and dateTime values.
 * 
 * @author James Leigh
 * 
 */
public class LiteralTable {

	public static final boolean ONLY_INSERT_LABEL = false;

	private ValueTable labels;

	private ValueTable longLabels;

	private ValueTable languages;

	private ValueTable datatypes;

	private ValueTable numeric;

	private ValueTable dateTime;

	/***************************/
	private GeoValueTable geoSpatialTable;
	/***************************/

	private int version;

	private IdSequence ids;

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public ValueTable getLabelTable() {
		return labels;
	}

	public void setLabelTable(ValueTable labels) {
		this.labels = labels;
	}

	public ValueTable getLongLabelTable() {
		return longLabels;
	}

	public void setLongLabelTable(ValueTable longLabels) {
		this.longLabels = longLabels;
	}

	public ValueTable getLanguageTable() {
		return languages;
	}

	public void setLanguageTable(ValueTable languages) {
		this.languages = languages;
	}

	/****************************************/
	public void setGeoSpatialTable(GeoValueTable geospatial) {
		this.geoSpatialTable = geospatial;
	}
	/****************************************/

	public ValueTable getDatatypeTable() {
		return datatypes;
	}

	public void setDatatypeTable(ValueTable datatypes) {
		this.datatypes = datatypes;
	}

	public ValueTable getNumericTable() {
		return numeric;
	}

	public void setNumericTable(ValueTable numeric) {
		this.numeric = numeric;
	}

	public ValueTable getDateTimeTable() {
		return dateTime;
	}

	/****************************************/
	public GeoValueTable getGeoSpatialTable() {
		return geoSpatialTable;
	}
	/****************************************/
	public void setDateTimeTable(ValueTable dateTime) {
		this.dateTime = dateTime;
	}

	public void close()
	throws SQLException
	{
		labels.close();
		longLabels.close();
		languages.close();
		datatypes.close();
		numeric.close();
		dateTime.close();
		/**********/
		geoSpatialTable.close();
		/**********/
	}

	public int getBatchSize() {
		return labels.getBatchSize();
	}

	public int getIdVersion() {
		return version;
	}

	public void insertSimple(Number id, String label)
	throws SQLException, InterruptedException
	{
		//System.out.println("-------->embolimi ektypwsi se insertSimple");
		if (ids.isLong(id)) {
			longLabels.insert(id, label);
		}
		else {
			labels.insert(id, label);

		}
	}

	public void insertLanguage(Number id, String label, String language)
	throws SQLException, InterruptedException
	{
		insertSimple(id, label);
		languages.insert(id, language);
	}

	public void insertDatatype(Number id, String label, String datatype)
	throws SQLException, InterruptedException
	{
		 
		insertSimple(id, label);
		datatypes.insert(id, datatype);
	}

	/********************************************************************/
	public void insertGeoSpatial(Number id, String label, String datatype,Timestamp start,Timestamp end) throws SQLException, InterruptedException
	{
		 
		byte[] geomWKB = null;
		 
		try {
		
			/***XXX new stuff dictated by kkyzir's StrabonPolyhedron - will be added when the functionality is complete***/
			StrabonPolyhedron polyhedron = new StrabonPolyhedron(label);
			geomWKB = polyhedron.toByteArray();
			
		
		} catch (conversionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw new SQLException("An issue occurred in the underlying StrabonPolyhedron's constructor!");
			
		}

		//Removed 'value' field
		Integer srid= findSRID(label);
		geoSpatialTable.insert(id,srid/*,start,end*/, geomWKB);

		//XXX not needed currently because this method is called AFTER an insertDatatype()
		//		insertSimple(id, label);
		//		datatypes.insert(id, datatype);
	}
	
	//the new version will actually deal with WKB
	public void insertWKT(Number id, String label, String datatype,Timestamp start,Timestamp end) throws SQLException, NullPointerException,InterruptedException,IllegalArgumentException
	{
		
		byte[] geomWKB = null;
		
		try {
			
			/***XXX new stuff dictated by kkyzir's StrabonPolyhedron***/
			
			StrabonPolyhedron polyhedron = new StrabonPolyhedron(label,2);//current algorithm selected: approx convex partition
			geomWKB = polyhedron.toWKB();
			
		}  catch (conversionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Integer srid= findSRID(label);
		geoSpatialTable.insert(id,srid,/* start,end,*/ geomWKB);
		
		//XXX not needed currently because this method is called AFTER an insertDatatype()
		//		insertSimple(id, label);
		//		datatypes.insert(id, datatype);
	}
	/********************************************************************/
	public void insertNumeric(Number id, String label, String datatype, double value)
	throws SQLException, InterruptedException
	{
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		System.out.println("about to insert double value:"+value);
		numeric.insert(id, value);
	}

	public void insertDateTime(Number id, String label, String datatype, long value)
	throws SQLException, InterruptedException
	{
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		dateTime.insert(id, value);
	}

	public void optimize()
	throws SQLException
	{
		labels.optimize();
		longLabels.optimize();
		languages.optimize();
		datatypes.optimize();
		numeric.optimize();
		dateTime.optimize();
	}

	public boolean expunge(String condition)
	throws SQLException
	{
		boolean bool = false;
		bool |= labels.expunge(condition);
		bool |= longLabels.expunge(condition);
		bool |= languages.expunge(condition);
		bool |= datatypes.expunge(condition);
		bool |= numeric.expunge(condition);
		bool |= dateTime.expunge(condition);
		bool |= geoSpatialTable.expunge(condition);
		return bool;
	}
	
	public static Integer findSRID(String label){
		String[] crs=label.split(";");
		if((crs.length == 1))
		{
			System.out.println("srid not specified. 4326 will be used as default.");
			return 4326; //use this as default
		}
		String prefix="http://www.opengis.net/def/crs/EPSG/0/";
		if(crs[1].startsWith(prefix)){
			int index=crs[1].lastIndexOf('/');
			index++;
			Integer srid = Integer.parseInt(crs[1].substring(index));
			 System.out.println("The EPSG code: " + srid);
					 
			System.out.println("SRS FOUND:"+srid);
			 return srid;
		}else{
			throw new IllegalArgumentException("MALFORMED URI FOR SRID!!!");
		
	   }
}
}
