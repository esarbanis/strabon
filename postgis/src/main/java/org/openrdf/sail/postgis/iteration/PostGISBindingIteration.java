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

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
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