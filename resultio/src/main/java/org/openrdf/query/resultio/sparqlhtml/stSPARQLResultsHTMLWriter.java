/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
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

	public static final String TABLE				= "TABLE";
	public static final String TABLE_ROW_TAG		= "TR";
	public static final String TABLE_HEADER_TAG 	= "TH";
	public static final String TABLE_DATA_TAG		= "TD";
	public static final String STYLE				= "class";
	public static final String TABLE_HEADER_CLASS	= "query_results_header";
	public static final String TABLE_DATA_CLASS		= "query_results_data";
	public static final String TABLE_CLASS			= "query_results_table";
	
	/**
	 * The underlying XML formatter.
	 */
	private stSPARQLXMLWriter xmlWriter;
	
	/**
	 * The ordered list of binding names of the result.
	 */
	private List<String> bindingNames;
	
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
			// keep the order of binding names
			this.bindingNames = bindingNames;
			
			// set style for table
			xmlWriter.setAttribute(STYLE, TABLE_CLASS);
			
			// write start of table
			xmlWriter.startTag(TABLE);
			
			// write Table header containing the bindings
			xmlWriter.startTag(TABLE_ROW_TAG);
			for (String bindingName: bindingNames) {
				// set style for header
				xmlWriter.setAttribute(STYLE, TABLE_HEADER_CLASS);
				
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
			
			// write end of table
			xmlWriter.endTag(TABLE);
						
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
			for (String bindingName : bindingNames) {
				Binding binding = bindingSet.getBinding(bindingName);
				value.append(binding.getValue().stringValue());
				
				if (binding.getValue() instanceof BNode) {
					value.insert(0, "_:");
				}
				xmlWriter.setAttribute(STYLE, TABLE_DATA_CLASS);
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
