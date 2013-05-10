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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
 *
 */
public class TestSPARQLEndpointStoreWithStrabon {

	private SPARQLEndpoint endpoint; 
	private String query;
	private Vector<stSPARQLQueryResultFormat> formats = new Vector<stSPARQLQueryResultFormat>();
	private URL data;
	
	@Before
	public void init() {
		// initialize endpoint
		endpoint = new SPARQLEndpoint("luna.di.uoa.gr", 8080, "endpoint/Store");
		
		// set url data
		try {
			data = new URL("http://luna.di.uoa.gr:8080/strabon-endpoint-gwt/mapontology/map.nt");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		endpoint.setUser("endpoint");
		endpoint.setPassword("3ndpo1nt");
		
		// initialized formats
		for (TupleQueryResultFormat format : stSPARQLQueryResultFormat.values()) {
				if (format instanceof stSPARQLQueryResultFormat) {
					formats.add((stSPARQLQueryResultFormat) format);
				}
		}
				
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 * @throws IOException 
	 */
	@Test
	public void testStoreFromUrl() throws IOException {
		
				Boolean response = endpoint.store(data, RDFFormat.NTRIPLES , null);
				
				if (response != true) 
					System.err.println("Error");
				
				
				assertTrue(response == true);
		
	}
	
	/**
	 * Test method for testing that method {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 * returns an IOException when it should do so.
	 */
	@Test(expected= IOException.class)
	public void testIOException() throws Exception {
		SPARQLEndpoint ep = new SPARQLEndpoint("blabla.dgr", 80, "bla");
		ep.query(query, formats.get(0));
	}
}