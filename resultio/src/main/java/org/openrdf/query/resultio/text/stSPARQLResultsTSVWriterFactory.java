package org.openrdf.query.resultio.text;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriterFactory;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsTSVWriterFactory extends SPARQLResultsTSVWriterFactory {

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.TSV;
	}

	@Override
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new stSPARQLResultsTSVWriter(out);
	}

}
