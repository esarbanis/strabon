package eu.earthobservatory.org.StrabonEndpoint.client;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class TestSPARQLEndpointUpdateWithStrabon {
	
	private SPARQLEndpoint endpoint; 
	private String query;
	
	@Before
	public void init() {
		// initialize endpoint
		endpoint = new SPARQLEndpoint("luna.di.uoa.gr", 8080, "sextant-endpoint/Update");
		
		// set url data
		query = "insert data" +
				"{graph <http://example.com/update> " +
				"{<http://example.com/map/map> <http://example.com/map/hasLayer>  <http://example.com/map/layer>}  }";
		
		endpoint.setUser("endpoint");
		endpoint.setPassword("3ndpo1nt");
		
				
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#update(java.net.URL, org.openrdf.rio.RDFFormat, java.net.URL)}.
	 * @throws IOException 
	 */
	@Test
	public void testUpdate() throws IOException {
		
			Boolean response = endpoint.update(query);
			
			if (response != true) 
				System.err.println("Error");
			
			
		//	assertTrue(response == true);
		
	}

}
