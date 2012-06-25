package org.openrdf.query.resultio.sparqlhtml;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsHTMLWriterFactory implements TupleQueryResultWriterFactory {

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.HTML;
	}

	@Override
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new stSPARQLResultsHTMLWriter(out);
	}

}
