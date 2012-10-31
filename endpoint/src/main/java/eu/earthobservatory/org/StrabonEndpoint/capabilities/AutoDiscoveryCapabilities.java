/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.capabilities;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link Capabilities} interface and
 * shall be used only for versions of Strabon Endpoint prior to
 * version 3.2.5.
 * 
 * The purpose of this implementation is to attempt to find out the
 * capabilities of old Strabon Endpoints based on two simple heuristics:
 * 
 * 1) what has been changed in the code (addition of methods/classes adding
 *    specific functionality)
 * 2) response messages or HTTP codes that old Strabon Endpoints give on wrong
 *    parameters.    
 * 
 * The result may not be accurate in every case.
 * 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class AutoDiscoveryCapabilities implements Capabilities {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.capabilities.AutoDiscoveryCapabilities.class);
	
	/**
	 * The host of the endpoint to discovery its capabilities
	 */
	private String host;
	
	/**
	 * The port to use
	 */
	private int port;
	
	/**
	 * The name of the web application
	 */
	private String appName;
	
	@Override
	public boolean supportsLimit() {
		@SuppressWarnings("rawtypes")
		Class strabonWrapper;
		try {
			strabonWrapper = Class.forName("eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper");

			strabonWrapper.getDeclaredField("maxLimit");
			
			return true;
				
		} catch (ClassNotFoundException e1) {
			// No StrabonBeanWrapper? How come?
			logger.warn("[StrabonEndpoint.AutoDiscoveryCapabilities] Didn't find StrabonEndpoint class!!! How come?");
			
		} catch (SecurityException e) {
			logger.info("[StrabonEndpoint.AutoDiscoveryCapabilities] Could not determine limit support due to security exception. ", e);
			
		} catch (NoSuchFieldException e) {
			// this exception is OK. Strabon Endpoint does not support limit of results
		}
		
		return false;
	}

	@Override
	public boolean supportsAuthentication() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.Authenticate");
	}

	@Override
	public boolean supportsConnectionModification() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.ChangeConnectionBean");
	}

	@Override
	public String getVersion() {
		return "<= 3.2.4";
	}

	@Override
	public RequestCapabilities getQueryCapabilities() {
		String query = "SELECT * WHERE {?s ?p ?o. FILTER(regex(str(?p), \"geometry\"))} LIMIT 1";
		String[] queryParams = {"sparqlQuery", "query"};
		String[] formatParams = {"format", null};
		String[] formatValues = {"XML", "KML", "KMZ", "GeoJSON", "HTML", "TSV"};
		String[] acceptValues = {null, "application/sparql-results+xml", "text/tab-separated-values", 
				"application/vnd.google-earth.kml+xml", "application/vnd.google-earth.kmz", "text/html", 
				"application/json"};
		
		for (int q = 0; q < queryParams.length; q++) {
			for (int f = 0; f < formatParams.length; f++) {
				for (int v = 0; v < formatValues.length; v++) {
					for (int a = 0; a < acceptValues.length; a++) {
						HttpClient hc = new HttpClient();
						
						// create a post method to execute
						PostMethod method = new PostMethod(getConnectionURL() + "/Query");
						
						// set the query parameter
						method.setParameter(queryParams[q], query);
						if (formatParams[f] != null) {
							method.setParameter(formatParams[f], formatValues[v]);
							
						} else { // check for accept value as well, but only when
								 // we are not using the format parameter 
						
							// set the accept format
							if (acceptValues[a] != null) {
								method.addRequestHeader("Accept", acceptValues[a]);
							}
						}
						
						try {
							// execute the method
							int statusCode = hc.executeMethod(method);
							
							//System.out.println(method.getResponseBodyAsString());
							System.out.println(statusCode);
							
							if (statusCode == 200) {
								if (formatParams[f] != null) {
									System.err.println(queryParams[q] + ", " + formatParams[f] + ", " + formatValues[v]);
									
								} else {
									System.err.println(queryParams[q] + ", " + acceptValues[a]);	
								}
								
							}
	
						} catch (IOException e) {
							e.printStackTrace();
							
						} finally {
							// release the connection.
							method.releaseConnection();
						}
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public RequestCapabilities getUpdateCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getStoreCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getBrowseCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getConnectionCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsQuerying() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.QueryBean");
	}

	@Override
	public boolean supportsUpdating() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.UpdateBean");
	}

	@Override
	public boolean supportsStoring() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.StoreBean");
	}

	@Override
	public boolean supportsDescribing() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.DescribeBean");
	}

	@Override
	public boolean supportsBrowsing() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.BrowseBean");
	}
	
	private boolean canBeLoaded(String className) {
		try {
			Class.forName(className);
			return true;
			
		} catch (ClassNotFoundException e1) {
			return false;
		}
	}

	public void setEndpointDetails(String host, int port, String appName) {
		this.host = host;
		this.port = port;
		this.appName = appName;
	}
	
	private String getConnectionURL() {
		return "http://" + host + ":" + port + "/" + appName;
	}
}
