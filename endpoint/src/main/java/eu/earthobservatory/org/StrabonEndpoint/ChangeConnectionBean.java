/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChangeConnectionBean extends HttpServlet {

  private static final long serialVersionUID = 2175155067582174020L;

  /**
   * The context of the servlet
   */
  private ServletContext context;

  /**
   * Wrapper over Strabon
   */
  private StrabonBeanWrapper strabonWrapper;


  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);

    // get the context of the servlet
    context = getServletContext();

    // get the context of the application
    WebApplicationContext applicationContext =
        WebApplicationContextUtils.getWebApplicationContext(context);

    // the the strabon wrapper
    strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    strabonWrapper.populateRequest(request);

    // close the currently active connection
    strabonWrapper.closeConnection();

    // forward the request
    request.getRequestDispatcher("/connection.jsp").forward(request, response);
  }
}
