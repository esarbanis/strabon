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

import java.net.URL;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
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
	public String query(String sparqlQuery, String format) {
		// create a post method to execute
		HttpMethod post = new PostMethod(getConnectionURL());
		
		// set the query parameter
		post.getParams().setParameter("query", sparqlQuery);
		
		// set the accept format
		post.setRequestHeader("Accept", "???");
		
		return null;
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
