package eu.earthobservatory.org.StrabonEndpoint.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TestSPARQLEndpointUpdateWithStrabon {

  private SPARQLEndpoint endpoint;
  private String query;

  @Before
  public void init() {
    // initialize endpoint
    endpoint = new SPARQLEndpoint("geo.linkedopendata.gr", 80, "teststrabon-endpoint/Update");

    // set url data
    query =
        "insert data"
            + "{graph <http://example.com/update> "
            + "{<http://example.com/map/map> <http://example.com/map/hasLayer>  <http://example.com/map/layer>}  }";

    endpoint.setUser("endpoint");
    endpoint.setPassword("3ndpo1nt");


  }

  /**
   * Test method for
   * {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#update(java.net.URL, org.openrdf.rio.RDFFormat, java.net.URL)}
   * .
   * 
   * @throws IOException
   */
  @Test
  public void testUpdate() throws IOException {
    Boolean response = endpoint.update(query);
    assertTrue(response == true);
  }

}
