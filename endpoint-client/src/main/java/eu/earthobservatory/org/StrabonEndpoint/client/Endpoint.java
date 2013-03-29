/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import java.io.IOException;
import java.net.URL;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public interface Endpoint<T extends TupleQueryResultFormat> {
	/**
	 * Executes a SPARQL query on the Endpoint and get the results
	 * in the format specified by <code>T</code>. Format <code>T</code>
	 * should be an instance of class (or a subclass) {@link TupleQueryResultFormat}.   
	 * 
	 * @param sparqlQuery
	 * @param format
	 * @return
	 * @throws IOException
	 */
	public EndpointResult query(String sparqlQuery, T format) throws IOException;
	
	/**
	 * Stores the RDF <code>data</code> which are in the RDF format
	 * <code>format</code> in the named graph specified by the URL
	 * <code>namedGraph</code>.
	 * 
	 * @param data 
	 * @param format
	 * @param namedGraph
	 * @return <code>true</code> if store was successful, <code>false</code> otherwise
	 */
	public boolean store(String data, RDFFormat format, URL namedGraph);
	
	/**
	 * Stores the RDF data located at <code>data</code> which are in the
	 * RDF format <code>format</code> in the named graph specified by the
	 * URL <code>namedGraph</code>.
	 * 
	 * @param data
	 * @param format
	 * @param namedGraph
	 * @return <code>true</code> if store was successful, <code>false</code> otherwise
	 */
	public boolean store(URL data, RDFFormat format, URL namedGraph);
	
	/**
	 * Executes the SPARQL Update query specified in <code>sparqlUpdate</code>.
	 * 
	 * @param sparqlUpdate
	 * @return <code>true</code> if store was successful, <code>false</code> otherwise
	 */
	public boolean update(String sparqlUpdate);
	
	public EndpointResult describe(String sparqlDescribe);
	
	public EndpointResult construct(String sparqlConstruct);
	
	public EndpointResult ask(String sparqlAsk);
}
