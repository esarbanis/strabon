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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * This class is the implementation of a java client for accessing
 * StrabonEndpoint instances.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class StrabonEndpoint extends SpatialEndpointImpl {

	public StrabonEndpoint(String host, int port) {
		super(host, port);
	}

	@Override
	public String query(String sparqlQuery, stSPARQLQueryResultFormat format) throws IOException {
		// create a post method to execute
		HttpMethod method = new PostMethod(getConnectionURL());
		
		// set the query parameter
		method.getParams().setParameter("query", sparqlQuery);
		
		// set the accept format
		method.setRequestHeader("Accept", format.getDefaultMIMEType());
		
		try {
			// execute the method
			int statusCode = hc.executeMethod(method);

			// check the status code
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary
			// data
			return new String(responseBody);

		} catch (IOException e) {
			throw e;
			
		} finally {
			// release the connection.
			method.releaseConnection();
		}
	}

	@Override
	public boolean store(String data, RDFFormat format) {
		return false;
	}

	@Override
	public boolean store(URL data, RDFFormat format) {
		return false;
	}

	@Override
	public boolean update(String sparqlUpdate) {
		return false;
	}

	@Override
	public boolean describe(String sparqlDescribe) {
		return false;
	}

	@Override
	public boolean construct(String sparqlConstruct) {
		return false;
	}

}
