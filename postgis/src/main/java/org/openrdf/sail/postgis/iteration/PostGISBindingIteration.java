/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import eu.earthobservatory.constants.TemporalConstants;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public class PostGISBindingIteration extends GeneralDBBindingIteration {

	public PostGISBindingIteration(PreparedStatement stmt)
	throws SQLException
	{
		super(stmt);
	}

	@Override
	protected RdbmsValue createGeoValue(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id))
		{
			byte[] label = rs.getBytes(index + 1);
			int srid = rs.getInt(index + 2);
			return vf.getRdbmsPolyhedron(id, GeoConstants.WKT, label, srid);

		}

		return createResource(rs, index);
	}
	
	
	//constant- do the same for the temporal case
	@Override
	protected RdbmsValue createTemporalValue(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id))
		{	String temp = rs.getString(index + 1);
		int i = temp.indexOf("[");
		int j = temp.indexOf(")"); //postgres always returns periods in the following format: [start, end)
		String label = null;
		URI datatype = null;
		String[] periods = temp.substring(++i, j).split(",");

		SimpleDateFormat postgres = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat xsd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar now = new GregorianCalendar().getInstance();
		Calendar cal = new GregorianCalendar();
		try {
			String newStart = xsd.format(postgres.parse(periods[0])).toString();
			String newEnd = xsd.format(postgres.parse(periods[1])).toString();
			if(newStart.equalsIgnoreCase(newEnd))
			{
				label = newStart;
			    datatype = vf.createURI(TemporalConstants.INSTANT);
			}
			else
			{
				 now = new GregorianCalendar().getInstance();
				 cal.setTime(postgres.parse(periods[1]));
				 if(cal.after(now))
				 {
						label = temp.replace(periods[0], newStart).replace(periods[1], "UC");

				 }
				 else
				 {
					 label = temp.replace(periods[0], newStart).replace(periods[1], newEnd); 
				 }
				datatype = vf.createURI(TemporalConstants.PERIOD);		
			}			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// label=temp.replace(" ", "T");
			//String label = rs.getObject(index+1).toString();
		    return vf.createLiteral(label, datatype);
		    
		}
		else
		{
			System.out.println("createTemporalValue: THIS IS NOT A LITERAL!!!!!");
		}
		return createResource(rs, index);
	}


	@Override
	protected RdbmsValue createBinaryGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{		
		//Case of spatial constructs
		byte[] label = rs.getBytes(index + 1);
		int srid = rs.getInt(index + 2);
		return vf.getRdbmsPolyhedron(114, GeoConstants.WKT, label, srid);

	}
}
