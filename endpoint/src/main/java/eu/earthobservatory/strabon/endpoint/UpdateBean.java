/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.strabon.endpoint;

import org.openrdf.query.MalformedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;

public class UpdateBean extends StrabonAwareServlet {

  private static final long serialVersionUID = -633279376188071670L;

  private static Logger logger = LoggerFactory
      .getLogger(UpdateBean.class);

  // Check for localHost. Works with ipV4 and ipV6
  public static boolean isLocalClient(HttpServletRequest request) {
    try {
      InetAddress remote = InetAddress.getByName(request.getRemoteAddr());
      if (remote.isLoopbackAddress()) {
        return true;
      }
      InetAddress localHost = InetAddress.getLocalHost();
      String localAddress = localHost.getHostAddress();
      String remoteAddress = remote.getHostAddress();
      return (remoteAddress != null && remoteAddress.equalsIgnoreCase(localAddress));
    } catch (UnknownHostException e) {
      logger.warn(e.getMessage());
    }
    return false;
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
      throws IOException, ServletException {
    boolean authorized;

    request.setCharacterEncoding("UTF-8");
    ServletContext context = getServletContext();
    if (!isLocalClient(request)) {
      String authorization = request.getHeader("Authorization");

      authorized = getAuthenticate().authenticateUser(authorization, context);
    } else
      authorized = true;

    if (!authorized) {
      // not allowed, so report he's unauthorized
      response.setHeader("WWW-Authenticate", "BASIC realm=\"Please login\"");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } else {
      if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
        // HTML visual interface
        processVIEWRequest(request, response);

      } else {// invoked as a service
        processRequest(request, response);
      }
    }
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String query = request.getParameter("query");

    String answer = "";
    try {
      logger.info("[StrabonEndpoint.UpdateBean] Received UPDATE query.");

      if (query == null) {
        throw new MalformedQueryException("No SPARQL Update query specified.");
      }

      // decode URL
      query = URLDecoder.decode(query, "UTF-8");

     update(query);
      response.setStatus(HttpServletResponse.SC_OK);
      answer = "true";

    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      answer = ResponseMessages.getXMLException(e.getMessage());
    }

    // write response to client
    response.getWriter().append(ResponseMessages.getXMLHeader());
    response.getWriter().append(answer);
    response.getWriter().append(ResponseMessages.getXMLFooter());
  }

  private void processVIEWRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher("query.jsp");

    String query = request.getParameter("query");

    if (query == null) {
      request.setAttribute("error", "SPARQL query is not set.");

    } else {
      query = URLDecoder.decode(query, "UTF-8");

      try {
        update(query);
        request.setAttribute("info", "Update executed succesfully.");

      } catch (MalformedQueryException e) {
        request.setAttribute("error", e.getMessage());
      }
    }

    dispatcher.forward(request, response);
  }
}
