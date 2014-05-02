/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * 
 */
public class stSPARQLResultsTSVWriter extends SPARQLResultsTSVWriter {

	public stSPARQLResultsTSVWriter(OutputStream out) {
		super(out);
	}

	@Override
	protected void writeValue(Value val) throws IOException {
		if (val instanceof GeneralDBPolyhedron) {
			// catch the spatial case and create a new literal
			// constructing a new literal is the only way if we want to reuse the {@link #writeValue(Value)} method
			GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) val;
			if(String.valueOf(dbpolyhedron.getDatatype()) == GeoConstants.WKT)
				val = new LiteralImpl(dbpolyhedron.getPolyhedronStringRep()+";http://www.opengis.net/def/crs/EPSG/0/"+dbpolyhedron.getPolyhedron().getGeometry().getSRID(), dbpolyhedron.getDatatype());
			else
				val = new LiteralImpl("<http://www.opengis.net/def/crs/EPSG/0/"+dbpolyhedron.getPolyhedron().getGeometry().getSRID()+"> "+dbpolyhedron.getPolyhedronStringRep(),dbpolyhedron.getDatatype());
		}
		
		// write value
		super.writeValue(val);
		
	}
}
