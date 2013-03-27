/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.SQLException;
import java.sql.Timestamp;

import javax.xml.bind.JAXBException;

import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * A Facade to the five literal value tables. Which are labels, languages,
 * datatypes, numeric values, and dateTime values.
 * 
 * @author James Leigh
 * 
 * @author Konstantina Bereta (aka constant) 
 *  Added table and functions for storing valid time literalsl with period datatype
 * 
 */
public class LiteralTable {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.generaldb.schema.LiteralTable.class);
	
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

	/***************************/
	private PeriodTable temporalTable;
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
	public void setTemporalTable(PeriodTable temporalTable) {
		this.temporalTable = temporalTable;
	}

	public void setDatatypeTable(ValueTable datatypes) {
		this.datatypes = datatypes;
	}
	/****************************************/

	
	public ValueTable getDatatypeTable() {
		return datatypes;
	}

	public PeriodTable getTemporalTable() {
		return temporalTable;
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
		temporalTable.close();
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

	/**
	 * @throws InterruptedException 
	 * @throws SQLException 
	 * @throws NullPointerException ******************************************************************/
	public void insertTemporal(Number id, String label) throws NullPointerException, SQLException, InterruptedException
	{

		//TODO the label should be validated as a period before reaching this point. Not sure yet on which level this should be done
//		System.out.println("LABEL: "+label);
//		System.out.println("NUMBER: "+id.toString());
		String postgresPeriodLabel = label.replace("T"," ");
		temporalTable.insert(id, postgresPeriodLabel);
	}
	
	
	/********************************************************************/
	public void insertGeoSpatial(Number id, String label, String datatype,Timestamp start,Timestamp end) throws SQLException, InterruptedException
	{
		 
		byte[] geomWKB = null;
		 
		try {
		
			/***XXX new stuff dictated by kkyzir's StrabonPolyhedron - will be added when the functionality is complete***/
			StrabonPolyhedron polyhedron = new StrabonPolyhedron(label);
			geomWKB = polyhedron.toByteArray();
			
		
		} catch (IllegalArgumentException e) {
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
	public void insertWKT(Number id, String label, String datatype, Timestamp start,Timestamp end) throws SQLException, NullPointerException,InterruptedException,IllegalArgumentException
	{
		try {
			AbstractWKT awkt = new AbstractWKT(label, datatype);
			Geometry geom = JTSWrapper.getInstance().WKTread(awkt.getWKT());
			geoSpatialTable.insert(id, awkt.getSRID(),/* start,end,*/ JTSWrapper.getInstance().WKBwrite(geom));
			
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Inserts a the given geometry represented in GML into the geo_values table.
	 * 
	 * @param id
	 * @param label
	 * @param datatype
	 * @param start
	 * @param end
	 * @throws SQLException
	 * @throws NullPointerException
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 */
	public void insertGML(Number id, String gml, String datatype, Timestamp start, Timestamp end) throws SQLException, NullPointerException,InterruptedException,IllegalArgumentException {
		Geometry geom;
		try {
			geom = JTSWrapper.getInstance().GMLread(gml);
			geoSpatialTable.insert(id, geom.getSRID(),/* start,end,*/ JTSWrapper.getInstance().WKBwrite(geom));
			
		} catch (JAXBException e) {
			logger.error("[Strabon.insertGML] Error during insertion of GML literal.", e);
		}
		
	}
	
	public void insertNumeric(Number id, String label, String datatype, double value)
	throws SQLException, InterruptedException
	{
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		if (logger.isDebugEnabled()) {
			logger.debug("about to insert double value: {}", value);
		}
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
		bool |= temporalTable.expunge(condition);
		
		return bool;
	}
	
	
	
	public static Integer findSRID(String label){
		String[] crs=label.split(";");
		String crsUri=null;
		
		if((crs.length == 1))
		{
			if(label.contains("gml"))
			{
				try {
					StrabonPolyhedron poly = new StrabonPolyhedron(label);
					if(poly.getGeometry().getSRID()>0)
						return poly.getGeometry().getSRID();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else
			{
				return 4326;
			} 
		}
		else
			crsUri = crs[1];
		
		String prefix="http://www.opengis.net/def/crs/EPSG/0/";
		if(crsUri.startsWith(prefix)){
			int index=crsUri.lastIndexOf('/');
			index++;
			Integer srid = Integer.parseInt(crsUri.substring(index));
			//System.out.println("The EPSG code: " + srid);
					 
			//System.out.println("SRS FOUND:"+srid);
			 return srid;
		}else{
			throw new IllegalArgumentException("MALFORMED URI FOR SRID!!!");
		
	   }
}
}
