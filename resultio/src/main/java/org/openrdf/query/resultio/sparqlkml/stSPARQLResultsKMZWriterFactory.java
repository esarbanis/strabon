package org.openrdf.query.resultio.sparqlkml;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsKMZWriterFactory implements TupleQueryResultWriterFactory {

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.KMZ;
	}

	@Override
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new stSPARQLResultsKMZWriter(out);
	}

}
