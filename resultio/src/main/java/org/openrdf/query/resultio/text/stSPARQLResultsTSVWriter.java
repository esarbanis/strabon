package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;

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
		// catch the spatial case and create a new literal
		// constructing a new literal is the only way if we want to reuse the {@link #writeValue(Value)} method
		if (val instanceof GeneralDBPolyhedron) {
			GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) val;
			val = new LiteralImpl(dbpolyhedron.getPolyhedronStringRep(), dbpolyhedron.getDatatype());
		} 
		
		// write value
		super.writeValue(val);
		
	}
}
