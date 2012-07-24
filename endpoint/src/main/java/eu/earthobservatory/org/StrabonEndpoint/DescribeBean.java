package eu.earthobservatory.org.StrabonEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.rio.RDFFormat;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DescribeBean extends HttpServlet{

	private static final long serialVersionUID = -7541662133934957148L;

    /**
	 * Parameters used in the describe.jsp file
	 */
	public static final String VIEW 		= "view";
	public static final String VIEW_TYPE 	= "HTML";
	
	/**
	 * Attributes carrying values to be rendered by the describe.jsp file 
	 */
	private static final String ERROR		= "error";
	private static final String RESPONSE	= "response";
	
	/**
	 * Error returned by DescribeBean
	 */
	private static final String PARAM_ERROR = "RDF format or SPARQL query are not set or are invalid.";
	
	private StrabonBeanWrapper strabonWrapper;

    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get StrabonWrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}
    
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		if (VIEW_TYPE.equals(request.getParameter(VIEW))) {
			// HTML visual interface
			processVIEWRequest(request, response);
			
		} else {// invoked as a service
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
		// get the dispatcher for forwarding the rendering of the response
		RequestDispatcher dispatcher = request.getRequestDispatcher("describe.jsp");
		
		String query = request.getParameter("query");
		String format = request.getParameter("format");
		
		if (format == null || query == null) {
			request.setAttribute(ERROR, PARAM_ERROR);
			dispatcher.forward(request, response);
			
		} else {
			// set the query and format to be selected in the rendered page
			request.setAttribute("query", URLDecoder.decode(query, "UTF-8"));
			//request.setAttribute("format", URLDecoder.decode(reqFormat, "UTF-8"));
		
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				strabonWrapper.describe(query, format, bos);
				
				request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
				
			} catch (Exception e) {
				request.setAttribute(ERROR, e.getMessage());
			}
			
			dispatcher.forward(request, response);
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
		
		// get the RDF format (we check only the Accept header)
        RDFFormat format = getFormat(request.getHeader("accept"));
        
        // get the query
		String query = request.getParameter("query");
    	
    	// check for required parameters
    	if (format == null || query == null) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(ResponseMessages.getXMLHeader());
			out.print(ResponseMessages.getXMLException(PARAM_ERROR));
			out.print(ResponseMessages.getXMLFooter());
    		
    	} else {
    		// decode the query
    		query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
    		
	    	response.setContentType(format.getDefaultMIMEType());
		    response.setHeader("Content-Disposition", 
		    		"attachment; filename=describe." + format.getDefaultFileExtension() + "; " + format.getCharset());
		    
			try {
				strabonWrapper.describe(query, format.getName(), out);
				response.setStatus(HttpServletResponse.SC_OK);
				
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ResponseMessages.getXMLHeader());
				out.print(ResponseMessages.getXMLException(e.getMessage()));
				out.print(ResponseMessages.getXMLFooter());
			}
    	}
    	
    	out.flush();
    }
	
    /**
     * Determines the RDF format to use. We check only for "accept"
     * parameter (present in the header). 
     * 
     * The use of "format" parameter is now deprecated for using the
     * DescribeBean as a service. It is only used through the HTML
     * visual interface, provided with Strabon Endpoint.
     * 
     * @param request
     * @return
     */
    private RDFFormat getFormat(String reqAccept) {
        if (reqAccept != null) {
            // check whether the "accept" parameter contains any 
            // of the mime types of any RDF format
            for (RDFFormat format : RDFFormat.values()) {
                    for (String mimeType : format.getMIMETypes()) {
                            if (reqAccept.contains(mimeType)) {
                                    return format;
                            }
                    }
            }
        }
                
        return null;
    }
}
