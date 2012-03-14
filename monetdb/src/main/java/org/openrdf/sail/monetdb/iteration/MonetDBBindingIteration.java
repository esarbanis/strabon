/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb.iteration;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBBindingIteration extends GeneralDBBindingIteration {

	public MonetDBBindingIteration(PreparedStatement stmt)
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
			Blob labelBlob = rs.getBlob(index + 1);
    		byte[] label = labelBlob.getBytes((long)1, (int)labelBlob.length());
    		int srid = rs.getInt(index + 2);
			return vf.getRdbmsPolyhedron(id, StrabonPolyhedron.ogcGeometry, label, srid);

		}

		return createResource(rs, index);
	}


	@Override
	protected RdbmsValue createBinaryGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{

		//Case of spatial constructs
		Blob labelBlob = rs.getBlob(index + 1); 
		byte[] label = labelBlob.getBytes((long)1, (int)labelBlob.length());
		int srid = rs.getInt(index + 2);
		return vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label, srid);

	}
}