/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.earthobservatory.utils.Format;

/**
 * 
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Stella Giannakopoulou <sgian@di.uoa.gr>
 */
public class QueryBean extends HttpServlet {

	private static final long serialVersionUID = -378175118289907707L;

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.QueryBean.class);
	
	/**
	 * Attributes carrying values to be rendered by the query.jsp file 
	 */
	private static final String ERROR		= "error";
	private static final String RESPONSE	= "response";
	
	/**
	 * Error returned by QueryBean
	 */
	private static final String PARAM_ERROR = "stSPARQL Query Results Format or SPARQL query are not set or are invalid.";
	
	/**
	 * The context of the servlet
	 */
	private ServletContext context;
	
	/**
	 * Wrapper over Strabon
	 */
	private StrabonBeanWrapper strabonWrapper;
	
	/**
	 * The name of the temporary directory to store KML/KMZ files
	 * for presentation in Google Maps 
	 */
	private String tempDirectory;
	
	/**
	 * The absolute path of the temporary directory
	 */
	private String basePath;
	
	/**
	 * The name of this web application
	 */
	private String appName;
	
	private TupleQueryResult result;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get the context of the servlet
		context = getServletContext();
		
		// get the context of the application
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		// the the strabon wrapper
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
		
		// get the name of this web application
		appName = context.getContextPath().replace("/", "");
		
		// fix the temporary directory for this web application
		tempDirectory = appName + "-temp";
		
		// get the absolute path of the temporary directory
		basePath = context.getRealPath("/") + "/../ROOT/" + tempDirectory + "/";
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		// check connection details
		if (strabonWrapper.getStrabon() == null) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/connection.jsp");
			
			// pass the current details of the connection
			request.setAttribute("username", strabonWrapper.getUsername());
			request.setAttribute("password", strabonWrapper.getPassword());
			request.setAttribute("dbname", 	 strabonWrapper.getDatabaseName());
			request.setAttribute("hostname", strabonWrapper.getHostName());
			request.setAttribute("port", 	 strabonWrapper.getPort());
			request.setAttribute("dbengine", strabonWrapper.getDBEngine());
			
			// pass the other parameters as well
			request.setAttribute("query", request.getParameter("query"));
			request.setAttribute("format", request.getParameter("format"));
			request.setAttribute("handle", request.getParameter("handle"));
			
			// forward the request
			dispatcher.forward(request, response);
			
		} else {
		
			if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
				// HTML visual interface
				processVIEWRequest(request, response);
				
	
			} else {// invoked as a service
				processRequest(request, response);
		    }
		}
	}

	/**
     * Processes the request made by a client of the endpoint that uses it as a service. 
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		
		// get the stSPARQL Query Result format (we check only the Accept header)
        stSPARQLQueryResultFormat format = stSPARQLQueryResultFormat.forMIMEType(request.getHeader("accept"));
        
        // get the query
		String query = request.getParameter("query");
		String maxLimit = request.getParameter("maxLimit");
    	
    	// check for required parameters
    	if (format == null || query == null) {
    		logger.error("[StrabonEndpoint.QueryBean] {}", PARAM_ERROR);
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(ResponseMessages.getXMLHeader());
			out.print(ResponseMessages.getXMLException(PARAM_ERROR));
			out.print(ResponseMessages.getXMLFooter());
    		
    	} else {
    		// decode the query
    		query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
    		
	    	response.setContentType(format.getDefaultMIMEType());
	    	try {
				query = strabonWrapper.addLimit(query, maxLimit);
				strabonWrapper.query(query, format.getName(), out);
				response.setStatus(HttpServletResponse.SC_OK);
				
			} catch (Exception e) {
				logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ResponseMessages.getXMLHeader());
				out.print(ResponseMessages.getXMLException(e.getMessage()));
				out.print(ResponseMessages.getXMLFooter());
			}
    	}
    	
    	out.flush();
	}

	/**
     * Processes the request made from the HTML visual interface of Strabon Endpoint.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
	private void processVIEWRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher;

		// check whether Update submit button was fired
		String reqFuncionality = (request.getParameter("submit") == null) ? "" : request.getParameter("submit");	

		if (reqFuncionality.equals("Update")) {
			// get the dispatcher for forwarding the rendering of the response
			dispatcher = request.getRequestDispatcher("/Update");
			dispatcher.forward(request, response);
			
		} else {
			String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
			String format = request.getParameter("format");
			String handle = request.getParameter("handle");
			String maxLimit = request.getParameter("maxLimit");
			
			// get stSPARQLQueryResultFormat from given format name
			TupleQueryResultFormat queryResultFormat = stSPARQLQueryResultFormat.valueOf(format);
			
			if (query == null || format == null || queryResultFormat == null) {
				dispatcher = request.getRequestDispatcher("query.jsp");
				request.setAttribute(ERROR, PARAM_ERROR);
				dispatcher.forward(request, response);
				
			} else {
				query = strabonWrapper.addLimit(query, maxLimit);
				if ("download".equals(handle)) { // download as attachment
					ServletOutputStream out = response.getOutputStream();
					
					response.setContentType(queryResultFormat.getDefaultMIMEType());
				    response.setHeader("Content-Disposition", 
				    				"attachment; filename=results." + 
				    				queryResultFormat.getDefaultFileExtension() + "; " + 
				    				queryResultFormat.getCharset());
				    
				    try {
						strabonWrapper.query(query, format, out);
						response.setStatus(HttpServletResponse.SC_OK);
						
				    } catch (Exception e) {
				    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ResponseMessages.getXMLHeader());
						out.print(ResponseMessages.getXMLException(e.getMessage()));
						out.print(ResponseMessages.getXMLFooter());
				    }
				    
				    out.flush();
				    
				} else if (("map".equals(handle) || "map_local".equals(handle)) && 
						(queryResultFormat == stSPARQLQueryResultFormat.KML || 
						 queryResultFormat == stSPARQLQueryResultFormat.KMZ) ) {
					// show map (only valid for KML/KMZ)
					
					// get dispatcher
					dispatcher = request.getRequestDispatcher("query.jsp");
					
					// re-assign handle
					request.setAttribute("handle", handle);
					
					SecureRandom random = new SecureRandom();
					String temp = new BigInteger(130, random).toString(32);
					
					// the temporary KML/KMZ file to create in the server
					String tempKMLFile = temp + "." + queryResultFormat.getDefaultFileExtension();;
					
					try{
						Date date = new Date();

						FileUtils.forceMkdir(new File(basePath));

						@SuppressWarnings("unchecked")
						Iterator<File> it = FileUtils.iterateFiles(new File(basePath), null, false);
						while(it.hasNext()){
							File tbd = new File((it.next()).getAbsolutePath());
							if (FileUtils.isFileOlder(new File(tbd.getAbsolutePath()), date.getTime())){
								FileUtils.forceDelete(new File(tbd.getAbsolutePath()));
							}
						}
						
						// create temporary KML/KMZ file
						File file = new File(basePath + tempKMLFile);

						// if file does not exist, then create it
						if(!file.exists()){
							file.createNewFile();
						}
						
						try {
							// query and write the result in the temporary KML/KMZ file
							FileOutputStream fos = new FileOutputStream(basePath + tempKMLFile);
							strabonWrapper.query(query, format, fos);
							fos.close();
						
							request.setAttribute("pathToKML", 
									request.getScheme() + "://" +  
									request.getServerName() + ":" + request.getServerPort() + 
									"/" + tempDirectory + "/" + tempKMLFile);
							
						} catch (MalformedQueryException e) {
							logger.error("[StrabonEndpoint.QueryBean] Error during querying. {}", e.getMessage());
							request.setAttribute(ERROR, e.getMessage());
							
						} catch (Exception e) {
							logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
							request.setAttribute(ERROR, e.getMessage());
						}
						
						dispatcher.forward(request, response);

					} catch(IOException e) {
						logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
					}

				} else { // "plain" is assumed as the default
					dispatcher = request.getRequestDispatcher("query.jsp");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					
					try {
						strabonWrapper.query(query, format, bos);
						if (format.equals(Common.getHTMLFormat())) {
							request.setAttribute(RESPONSE, bos.toString());
						} 
						else if(format.equals(Format.CHART)){
							request.setAttribute(RESPONSE, strabonWrapper);
						}
						else {
							request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
						}
						
					} catch (MalformedQueryException e) {
						logger.error("[StrabonEndpoint.QueryBean] Error during querying. {}", e.getMessage());
						request.setAttribute(ERROR, e.getMessage());
						
					} catch (Exception e) {
						logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
						request.setAttribute(ERROR, e.getMessage());
						
					} finally {
						dispatcher.forward(request, response);
					}
				}
			}
		}
	}
}
