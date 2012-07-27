/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class ConfigureCommon implements ServletContextListener {

	private Common commonBean;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		//event.getServletContext().setAttribute("commonBean", (Common) applicationContext.getBean("commonBean"));
		commonBean = (Common) applicationContext.getBean("commonBean");
		event.getServletContext().setAttribute("commonBean", this.commonBean);
		
		System.out.println("HOOLA HOOLA HOOLA HOOLA HOOLA HOOLA HOOLA HOOLA");
		System.out.println(commonBean);
	}
}
