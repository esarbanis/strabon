/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;


import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class ConnectionBean extends HttpServlet {

	private static final long serialVersionUID = 2237815345608023368L;

	/**
	 * Wrapper over Strabon
	 */
	private StrabonBeanWrapper strabonWrapper;
	
	/**
	 * The context of the servlet
	 */
	private ServletContext context;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get the context of the servlet
		context = getServletContext();
		
		// get the context of the application
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		// the the strabon wrapper
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");

	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher;
		
		// set new connection details
		strabonWrapper.setConnectionDetails(
				request.getParameter("username"), 
				request.getParameter("password"), 
				request.getParameter("dbname"), 
				request.getParameter("hostname"), 
				request.getParameter("port"), 
				request.getParameter("dbengine"));
		
		// pass the query that had been issued to the dispatcher
		request.setAttribute("query", request.getAttribute("query"));
			
		// establish connection
		if (strabonWrapper.init()) { // successfully connected, go to QueryBean
			// TODO: save the new connection details in beans.xml
			dispatcher = request.getRequestDispatcher("/query.jsp");
			
		} else { // try again
			// pass the current details of the connection
			request.setAttribute("username", request.getParameter("username"));
			request.setAttribute("password", request.getParameter("password"));
			request.setAttribute("dbname", request.getParameter("dbname"));
			request.setAttribute("hostname", request.getParameter("hostname"));
			request.setAttribute("port", request.getParameter("port"));
			request.setAttribute("dbengine", request.getParameter("dbengine"));
			
			dispatcher = request.getRequestDispatcher("/connection.jsp");
			
		}
		
		dispatcher.forward(request, response);
	}

	

}
