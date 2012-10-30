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
	
	public boolean store(String data, RDFFormat format);
	
	public boolean store(URL data, RDFFormat format);
	
	public boolean update(String sparqlUpdate);
	
	public EndpointResult describe(String sparqlDescribe);
	
	public EndpointResult construct(String sparqlConstruct);
}
