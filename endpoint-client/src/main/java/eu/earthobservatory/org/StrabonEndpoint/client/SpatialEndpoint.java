/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import java.io.IOException;
import java.net.URL;

import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * Every SPARQL endpoint that supports storing and querying of
 * spatial RDF data should implement the {@link SpatialEndpoint}
 * interface. 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public interface SpatialEndpoint {

	public EndpointResult query(String sparqlQuery, stSPARQLQueryResultFormat format) throws IOException;
	
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
}
