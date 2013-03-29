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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * This class is the implementation of a java client for accessing
 * StrabonEndpoint instances.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class StrabonEndpoint extends SpatialEndpoint {

	public StrabonEndpoint(String host, int port) {
		super(host, port);
	}
	
	public StrabonEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}

	@Override
	public EndpointResult query(String sparqlQuery, stSPARQLQueryResultFormat format) throws IOException {
		assert(format != null);
		
		// create a post method to execute
		HttpPost method = new HttpPost(getConnectionURL() + "/Query");
		
		// set the query parameter
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("query", sparqlQuery));
		UrlEncodedFormEntity encodedEntity = new UrlEncodedFormEntity(params, Charset.defaultCharset());
		method.setEntity(encodedEntity);
		
		// set the content type
		method.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		// set the accept format
		method.addHeader("Accept", format.getDefaultMIMEType());
		
		try {
			// response that will be filled next
			String responseBody = "";
			
			// execute the method
			HttpResponse response = hc.execute(method);
			int statusCode = response.getStatusLine().getStatusCode();
			
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				try {

					BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					StringBuffer strBuf = new StringBuffer();
					
					// do something useful with the response
					String nextLine;
					while ((nextLine = reader.readLine()) != null) {
						strBuf.append(nextLine + "\n");
					}
					
					// remove last newline character
					if (strBuf.length() > 0) {
						strBuf.setLength(strBuf.length() - 1);
					}
					
					responseBody = strBuf.toString();

				} catch (IOException ex) {
					// In case of an IOException the connection will be released
					// back to the connection manager automatically
					throw ex;

				} catch (RuntimeException ex) {
					// In case of an unexpected exception you may want to abort
					// the HTTP request in order to shut down the underlying
					// connection and release it back to the connection manager.
					method.abort();
					throw ex;

				} finally {
					// Closing the input stream will trigger connection release
					instream.close();
				}
			}
			 
			return new StrabonEndpointResult(statusCode, response.getStatusLine().getReasonPhrase(), responseBody);

		} catch (IOException e) {
			throw e;
			
		} finally {
			// release the connection.
			method.releaseConnection();
		}
	}

	@Override
	public boolean store(String data, RDFFormat format, URL namedGraph) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean store(URL data, RDFFormat format, URL namedGraph) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean update(String sparqlUpdate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EndpointResult describe(String sparqlDescribe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EndpointResult construct(String sparqlConstruct) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EndpointResult ask(String sparqlAsk) {
		throw new UnsupportedOperationException();
	}
	
	public static void main(String args[]) {
		if (args.length < 4) {
			System.err.println("Usage: eu.earthobservatory.org.StrabonEndpoint.client.StrabonEndpoint <HOST> <PORT> <APPNAME> [<FORMAT>]");
			System.err.println("       where <HOST>       is the hostname of the Strabon Endpoint");
			System.err.println("             <PORT>       is the port to connect to on the host");
			System.err.println("             <APPNAME>    is the application name of Strabon Endpoint as deployed in the Tomcat container");
			System.err.println("             <QUERY>      is the query to execute on the endpoint");
			System.err.println("             [<FORMAT>]   is the format of your results. Should be one of XML (default), KML, KMZ, GeoJSON, TSV, or HTML.");
			System.exit(1);
		}
		
		String host = args[0];
		Integer port = new Integer(args[1]);
		String appName = args[2];
		String query = args[3];
		String format = "";
		
		if (args.length == 5) {
			format = args[4];
			
		} else {
			format = "XML";
		}
		
		StrabonEndpoint endpoint = new StrabonEndpoint(host, port, appName);
		
		try {
			EndpointResult result = endpoint.query(query, stSPARQLQueryResultFormat.valueOf(format));
			
			System.out.println("Status code: " + result.getStatusCode());
			System.out.println("Status text: " + result.getStatusText());
			System.out.println("<----- Result ----->");
			System.out.println(result.getResponse().replaceAll("\n", "\n\t"));
			System.out.println("<----- Result ----->");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
