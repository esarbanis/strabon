/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.strabon.endpoint;

import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BrowseBean extends QueryProcessingServlet {

  private static final long serialVersionUID = -378175118289907707L;

  private static Logger logger = LoggerFactory
      .getLogger(BrowseBean.class);

  /**
   * Attributes carrying values to be rendered by the browse.jsp file
   */
  private static final String ERROR = "error";
  private static final String RESPONSE = "response";

  /**
   * Error returned by BrowseBean
   */
  private static final String PARAM_ERROR =
      "stSPARQL Query Results Format or SPARQL query are not set or are invalid.";


  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(request, response);
  }

  private void process(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");

    // check connection details
    if (!isStrabonInitialized()) {
      populateRequest(request);

      // forward the request
      request.getRequestDispatcher("/connection.jsp").forward(request, response);

    } else {

      if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
        // HTML visual interface
        processVIEWRequest(request, response);

      } else {// invoked as a service
        processRequest(request, response);
      }
    }
  }

  void doProcessRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ServletOutputStream out = response.getOutputStream();
    // get the stSPARQL Query Result format (we check only the Accept
    // header)
    stSPARQLQueryResultFormat format =
        stSPARQLQueryResultFormat.forMIMEType(request.getHeader("accept"));
    // decode the query
    // do not decode the SPARQL query (see bugs #65 and #49)
    // query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
    String query = request.getParameter("query");

    response.setContentType(format.getDefaultMIMEType());
    try {
      query(query, format.getName(), out);
      response.setStatus(HttpServletResponse.SC_OK);

    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.print(ResponseMessages.getXMLHeader());
      out.print(ResponseMessages.getXMLException(e.getMessage()));
      out.print(ResponseMessages.getXMLFooter());
    } finally {
      out.flush();
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
  private void processVIEWRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RequestDispatcher dispatcher;

    // do not decode the SPARQL query (see bugs #65 and #49)
    // String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
    String query = request.getParameter("query");
    String format = request.getParameter("format");

    // get stSPARQLQueryResultFormat from given format name
    TupleQueryResultFormat queryResultFormat = stSPARQLQueryResultFormat.valueOf(format);

    if (query == null || format == null || queryResultFormat == null) {
      dispatcher = request.getRequestDispatcher("browse.jsp");
      request.setAttribute(ERROR, PARAM_ERROR);
      dispatcher.forward(request, response);

    } else {
      dispatcher = request.getRequestDispatcher("browse.jsp");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      request.setAttribute("resource", request.getParameter("resource"));

      try {
        query(query, format, bos);
        if (format.equals(Common.getHTMLFormat())) {
          request.setAttribute(RESPONSE, bos.toString());
        } else {
          request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
        }

      } catch (Exception e) {
        logger.error("[StrabonEndpoint.BrowseBean] Error during querying.", e);
        request.setAttribute(ERROR, e.getMessage());

      } finally {
        dispatcher.forward(request, response);
      }
    }
  }
}
