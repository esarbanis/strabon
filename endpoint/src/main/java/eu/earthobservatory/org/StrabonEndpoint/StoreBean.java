/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class StoreBean extends HttpServlet {
	
	private static final long serialVersionUID = -7541662133934957148L;
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.StoreBean.class);
	
	/**
	 * Parameters used in the store.jsp file
	 */
	public static final String PARAM_DATA 		= "data";
	public static final String PARAM_FORMAT 	= "format";
	public static final String PARAM_DATA_URL	= "url";
	
	/**
	 * Error/Info parameters used in the store.jsp file
	 */
	public static final String ERROR 			= "error";
	public static final String INFO				= "info";
	
	/**
	 * Error/Info messages
	 */
	private static final String STORE_ERROR 	= "An error occurred while storing input data!";
	private static final String PARAM_ERROR 	= "RDF format or input data are not set or are invalid!";
	private static final String STORE_OK		= "Data stored successfully!";
	
	/**
	 * Submit buttons
	 */
	public static final String SUBMIT_INPUT		= "dsubmit";
	public static final String SUBMIT_URL		= "fromurl";

	/**
	 * Strabon wrapper
	 */
	private StrabonBeanWrapper strabon;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		// get strabon wrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
		strabon = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	private String getData(HttpServletRequest request) {
		// check whether we read from INPUT or URL
		boolean input = (request.getParameter(SUBMIT_URL) != null) ? false:true;
		
		// return "data" value accordingly
		return input ? request.getParameter(PARAM_DATA):request.getParameter(PARAM_DATA_URL);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// check whether the request was from store.jsp
		if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
			processVIEWRequest(request, response);
			
		} else {
			processRequest(request, response);
		}
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
		// check whether we read from INPUT or URL
		boolean input = (request.getParameter(SUBMIT_URL) != null) ? false:true;
		
    	// get the dispatcher for forwarding the rendering of the response
    	RequestDispatcher dispatcher = request.getRequestDispatcher("store.jsp");
    			
    	// RDF data to store
    	String data = getData(request);
    			
    	// the format of the data
    	RDFFormat format = (request.getParameter(PARAM_FORMAT) != null) ? RDFFormat.valueOf(request.getParameter(PARAM_FORMAT)):null;
    	
    	if (data == null || format == null) {
    		request.setAttribute(ERROR, PARAM_ERROR);
			
    	} else {
    		
    		// store data
    		try {
    			strabon.store(data, format, !input);
    			
    			// store was successful, return the respective message
    			request.setAttribute(INFO, STORE_OK);
    				
    		} catch (Exception e) {
    			request.setAttribute(ERROR, STORE_ERROR + " " + e.getMessage());
    		}
    	}
    	
		dispatcher.forward(request, response);
    }
    
    /**
     * Processes the request made by a client of the endpoint that uses it as a service. 
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// check whether we read from INPUT or URL
		boolean input = (request.getParameter(SUBMIT_URL) != null) ? false:true;
		
    	// RDF data to store
    	String data = getData(request);
    	
    	if (data == null) { 
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
    	
    	// the format of the data
    	RDFFormat format = Common.getRDFFormatFromAcceptHeader(request.getHeader("accept"));
    	
		if (format == null) { // unknown format
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return ;
		}
		
		// store data
		try {
			strabon.store(data, format, !input);
			
			// store was successful, return the respective message
			response.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			if (e instanceof RDFParseException || 
				e instanceof IllegalArgumentException || e instanceof MalformedURLException) {
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			
			logger.error("[StrabonEndpoint.StoreBean] " + e.getMessage());
		}
    }
}
