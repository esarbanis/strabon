/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.sqlite.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.query.BindingSet;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author Dimitris Bilidas <grad0903@di.uoa.gr>
 * 
 */
public class SqliteBindingIteration extends GeneralDBBindingIteration {

	public SqliteBindingIteration(PreparedStatement stmt)
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