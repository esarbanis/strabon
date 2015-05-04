package eu.earthobservatory.strabon.endpoint;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author efthymis
 */
public class StrabonAwareServlet extends HttpServlet {

    /**
     * Wrapper over Strabon
     */
    private StrabonBeanWrapper strabonWrapper;

    StrabonBeanWrapper getStabonWrapper() {
        return strabonWrapper;
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        strabonWrapper = StrabonBeanWrapper.resolve(getServletContext());
    }
}
