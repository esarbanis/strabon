package org.openrdf.query.resultio.sparqlhtml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlxml.stSPARQLXMLWriter;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * 
 */
public class stSPARQLResultsHTMLWriter implements TupleQueryResultWriter {

	public static final String TABLE_ROW_TAG	= "TR";
	public static final String TABLE_HEADER_TAG = "TH";
	public static final String TABLE_DATA_TAG	= "TD";
	
	/**
	 * The underlying XML formatter.
	 */
	private stSPARQLXMLWriter xmlWriter;
	
	public stSPARQLResultsHTMLWriter(OutputStream out) {
		this(new stSPARQLXMLWriter(out));
	}
	
	public stSPARQLResultsHTMLWriter(stSPARQLXMLWriter writer) {
		xmlWriter = writer;
		xmlWriter.setPrettyPrint(true);
	}
	
	@Override
	public void startQueryResult(List<String> bindingNames)
			throws TupleQueryResultHandlerException {
		
		try {
			// write Table header containing the bindings
			xmlWriter.startTag(TABLE_ROW_TAG);
			for (String bindingName: bindingNames) {
				xmlWriter.textElement(TABLE_HEADER_TAG, bindingName);
			}
			
			xmlWriter.endTag(TABLE_ROW_TAG);
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			// needed to flush data
			xmlWriter.endDocument();
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			StringBuilder value = new StringBuilder();
			
			xmlWriter.startTag(TABLE_ROW_TAG);
			for (Binding binding : bindingSet) {
				value.append(binding.getValue().stringValue());
				
				if (binding.getValue() instanceof BNode) {
					value.insert(0, "_:");
				}
				xmlWriter.textElement(TABLE_DATA_TAG, value.toString());
				
				value.setLength(0);
			}
			
			xmlWriter.endTag(TABLE_ROW_TAG);
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.HTML;
	}

}
