package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * 
 */
public class stSPARQLResultsTSVWriter extends SPARQLResultsTSVWriter {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.text.stSPARQLResultsTSVWriter.class);
	
	public stSPARQLResultsTSVWriter(OutputStream out) {
		super(out);
	}

	@Override
	protected void writeValue(Value val) throws IOException {
		// catch the spatial case and create a literal
		// constructing a new literal is the only way if we want to reuse the {@link #writeValue(Value)} method
		if (val instanceof GeneralDBPolyhedron) {
			GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) val;
			if (StrabonPolyhedron.ogcGeometry.equals(dbpolyhedron.getDatatype().stringValue())) {
				// WKT
				val = new LiteralImpl(JTSWrapper.getInstance().WKTwrite(dbpolyhedron.getPolyhedron().getGeometry()), dbpolyhedron.getDatatype());
				
			} else { // TODO GML
				logger.warn("[Strabon.TSVWriter] GML is not supported yet.");
				
			}
		} 
		
		// write value
		super.writeValue(val);
		
	}
}
