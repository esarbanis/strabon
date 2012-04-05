/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.MalformedQueryException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author charnik
 *
 */
public class UpdateBean extends HttpServlet {

	private static final long serialVersionUID = -633279376188071670L;

	private StrabonBeanWrapper strabonWrapper;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
			
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		// get strabon wrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
		
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter("SPARQLQuery");
		
		String answer = "";
		try {
			strabonWrapper.getStrabon().update(query, strabonWrapper.getStrabon().getSailRepoConnection());
			response.setStatus(HttpServletResponse.SC_OK);
			answer = "true";
			
		} catch(MalformedQueryException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			answer = getExceptionError(e.getMessage());
		}
		
		// write response to client
		response.getWriter().append(UpdateBean.getUPDATEHeaderResponse());
		response.getWriter().append(answer);
		response.getWriter().append(UpdateBean.getUPDATEFooterResponse());
	}
	
	/**
	 * Used as the template answer for UPDATE queries.
	 * @return
	 */
	public static String getUPDATEHeaderResponse() {
		return "<?xml version='1.0' encoding='UTF-8'?>\n" +
			   "<response>\n" +
			   "\t";
	}
	
	/**
	 * Used as the template answer for UPDATE queries.
	 * @return
	 */
	public static String getUPDATEFooterResponse() {
		return "\n</response>\n";
	}
	
	public static String getExceptionError(String msg) {
		return "<exception>\n"+msg+"\n\t</exception>";
	}

}
