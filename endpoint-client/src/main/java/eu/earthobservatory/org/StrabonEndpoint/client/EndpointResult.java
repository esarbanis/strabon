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

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public interface EndpointResult {

	/**
	 * Returns the HTTP status code as returned by the endpoint.
	 * @return
	 */
	public int getStatusCode();
	
	/**
	 * Returns the status text corresponding to the status code.
	 * @return
	 */
	public String getStatusText();
	
	/**
	 * Returns the response of the endpoint.
	 * @return
	 */
	public String getResponse();
}
