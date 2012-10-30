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
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpMethod;
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
	
	public StrabonEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}

	@Override
	public EndpointResult query(String sparqlQuery, stSPARQLQueryResultFormat format) throws IOException {
		// create a post method to execute
		HttpMethod method = new PostMethod(getConnectionURL() + "/Query");
		
		// set the query parameter
		method.setQueryString("query="+ URLEncoder.encode(sparqlQuery, "UTF-8"));
		
		// set the content type
		method.setRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
		//System.out.println(method.getRequestHeader("Content-type"));
		
		// set the accept format
		method.addRequestHeader("Accept", format.getDefaultMIMEType());
		//System.out.println(method.getRequestHeader("Accept"));
		
		//System.out.println(method.getURI());

		try {
			// execute the method
			int statusCode = hc.executeMethod(method);
			
			return new StrabonEndpointResult(statusCode, method.getStatusText(), method.getResponseBodyAsString());

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
	public EndpointResult describe(String sparqlDescribe) {
		return null;
	}

	@Override
	public EndpointResult construct(String sparqlConstruct) {
		return null;
	}

}
