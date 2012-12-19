/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.iteration;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsValue;

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

	/**
	 * XXX additions
	 */
	/**
	 * 
	 * my addition
	 * 
	 */
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
		String[] periods = temp.substring(++i, j).split(",");

		SimpleDateFormat postgres = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
		SimpleDateFormat xsd = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
		try {
			
			String newStart = xsd.format(postgres.parse(periods[0])).toString();
			String newEnd = xsd.format(postgres.parse(periods[1])).toString();
			label = temp.replace(periods[0], newStart).replace(periods[1], newEnd);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// label=temp.replace(" ", "T");
			//String label = rs.getObject(index+1).toString();
		    URI datatype = vf.createURI(TemporalConstants.PERIOD);
		    return vf.createLiteral(label, datatype);
		    
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
