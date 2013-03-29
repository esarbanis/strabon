/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import org.openrdf.query.resultio.stSPARQLQueryResultFormat;

/**
 * Every SPARQL endpoint that supports storing and querying of
 * spatial RDF data should extend the {@link SpatialEndpoint}
 * abstract class. 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public abstract class SpatialEndpoint extends HTTPClient implements Endpoint<stSPARQLQueryResultFormat> {
	
	
	public SpatialEndpoint(String host, int port) {
		super(host, port);
	}
	
	public SpatialEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}
		
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String pass) {
		this.password = pass;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns a URL (actually a {@link String}) for establishing connections
	 * to an endpoint based on the information given to the constructor. 
	 * 
	 * @return
	 */
	protected String getConnectionURL() {
		return "http://" + host + ":" + port + "/" + endpointName; 
	}
}
