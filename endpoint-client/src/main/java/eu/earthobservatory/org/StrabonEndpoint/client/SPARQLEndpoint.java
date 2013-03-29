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
public class SPARQLEndpoint extends HTTPClient implements Endpoint<TupleQueryResultFormat> {

	public SPARQLEndpoint(String host, int port) {
		super(host, port);
	}
	
	public SPARQLEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}

	@Override
	public EndpointResult query(String sparqlQuery, TupleQueryResultFormat format) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean store(String data, RDFFormat format, URL namedGraph) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean store(URL data, RDFFormat format, URL namedGraph) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean update(String sparqlUpdate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EndpointResult describe(String sparqlDescribe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EndpointResult construct(String sparqlConstruct) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EndpointResult ask(String sparqlAsk) {
		throw new UnsupportedOperationException();
	}
}
