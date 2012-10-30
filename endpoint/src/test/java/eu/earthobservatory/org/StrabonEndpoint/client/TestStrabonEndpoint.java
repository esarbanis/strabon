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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;

import eu.earthobservatory.org.StrabonEndpoint.client.EndpointResult;
import eu.earthobservatory.org.StrabonEndpoint.client.StrabonEndpoint;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class TestStrabonEndpoint {

	private StrabonEndpoint endpoint; 
	
	@Before
	public void init() {
		endpoint = new StrabonEndpoint("test.strabon.di.uoa.gr", 80, "DLR");
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.StrabonEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 */
	@Test
	public void testQuery() {
		for (TupleQueryResultFormat format : stSPARQLQueryResultFormat.values()) {
			try {
				String query = "" +
						"PREFIX teleios:<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#>\n" +
						"SELECT ?s ?g WHERE {\n" +
						"	?s teleios:hasGeometry ?g\n" +
						"}" +
						"\nLIMIT 1";
				
				if (format instanceof stSPARQLQueryResultFormat) {
					EndpointResult response = endpoint.query(query, (stSPARQLQueryResultFormat) format);
					
					if (response.getStatusCode() != 200) {
						System.err.println("Status code ("+response.getStatusCode()+"):" + response.getStatusText());
						
					}
					assertTrue(response.getStatusCode() == 200);
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
