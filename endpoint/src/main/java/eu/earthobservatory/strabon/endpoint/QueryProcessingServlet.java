package eu.earthobservatory.strabon.endpoint;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author efthymis
 */
public abstract class QueryProcessingServlet extends HttpServlet {

    /**
     * Error returned by BrowseBean
     */
    private static final String PARAM_ERROR =
        "stSPARQL Query Results Format or SPARQL query are not set or are invalid.";

    /**
     * Processes the request made by a client of the endpoint that uses it as a service.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    void processRequest(HttpServletRequest request, HttpServletResponse response) throws
        IOException{
        String acceptHeader = request.getHeader("accept");
        String query = request.getParameter("query");

        if (acceptHeader == null || query == null) {
            ServletOutputStream out = response.getOutputStream();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(ResponseMessages.getXMLHeader());
            out.print(ResponseMessages.getXMLException(PARAM_ERROR));
            out.print(ResponseMessages.getXMLFooter());
            out.close();
        } else {
            doProcessRequest(request, response);
        }
    }

    abstract void doProcessRequest(HttpServletRequest request,
        HttpServletResponse response) throws IOException;
}
