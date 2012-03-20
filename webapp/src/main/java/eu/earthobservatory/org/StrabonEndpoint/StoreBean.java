/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author charnik
 *
 */
public class StoreBean extends HttpServlet {
	
	private static final long serialVersionUID = -7541662133934957148L;
	
	/**
	 * Parameters used in the store.jsp file
	 */
	public static final String PARAM_DATA 		= "data";
	public static final String PARAM_FORMAT 	= "format";
	public static final String PARAM_DATA_URL	= "url";
	public static final String SRC_REQ			= "source_request";
	
	/**
	 * Error parameters used in the store.jsp file
	 */
	public static final String DATA_ERROR 		= "edata";
	public static final String FORMAT_ERROR 	= "eformat";
	public static final String STORE_ERROR 		= "estore";
	
	/**
	 * Submit buttons
	 */
	public static final String SUBMIT_INPUT		= "dsubmit";
	public static final String SUBMIT_URL		= "fromurl";
	
	/**
	 * Parameter for successful store used in the store.jsp file
	 */
	public static final String STORE_OK			= "storeOK";
	
	/**
	 * Keeps the registered and available RDF formats.
	 */
	public static ArrayList<String> registeredFormats;
	
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
		
		// initialize registered and available formats
		registeredFormats = new ArrayList<String>();
		for (RDFFormat format : RDFFormat.values()) {
			registeredFormats.add(format.getName());
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// check whether we read from INPUT or URL
		boolean input = (request.getParameter(SUBMIT_URL) != null) ? false:true;
		
		// check if the request was from store.jsp
		boolean browser = (request.getParameter(SRC_REQ) != null) ? true:false;
		
		// RDF data to store
		String data = null;
		
		// the format of the data
		String format = null;
		
		format = request.getParameter(PARAM_FORMAT);
		data = input ? request.getParameter(PARAM_DATA):request.getParameter(PARAM_DATA_URL); 
		
		if (data == null) { 
			if (browser) {
				redirect(response, DATA_ERROR);
				
			} else {
				response.sendError(HttpServletResponse.SC_NO_CONTENT);
			}
			return ;
		}
		
		// get input format
		RDFFormat rdfFormat = RDFFormat.valueOf(format); 
		
		if (rdfFormat == null) { // unknown format
			if (browser) {
				redirect(response, FORMAT_ERROR);
				
			} else {
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			}
			
			return ;
		}		
		
		// store data
		try {
			strabon.store(data, rdfFormat, !input);
			
			// store was successful, return the respective message
			if (browser) {
				redirect(response, STORE_OK);
				
			} else {
				response.sendError(HttpServletResponse.SC_OK);
			}
		} catch (Exception e) {
			if (browser) {
				redirect(response, STORE_ERROR);
			} else {
				if (e instanceof RDFParseException) {
					response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				} else {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
			System.err.println(e.getMessage());
		}
	}
	
	private void redirect(HttpServletResponse response, String error) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", "store.jsp?" + error + "=");
	}
}
