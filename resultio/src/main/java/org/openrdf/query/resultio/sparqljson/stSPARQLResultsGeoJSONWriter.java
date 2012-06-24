package org.openrdf.query.resultio.sparqljson;

import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;

public class stSPARQLResultsGeoJSONWriter extends SPARQLResultsJSONWriter {

	public stSPARQLResultsGeoJSONWriter(OutputStream out) {
		super(out);
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
			throws TupleQueryResultHandlerException {
		
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
			throws TupleQueryResultHandlerException {
	}
}
