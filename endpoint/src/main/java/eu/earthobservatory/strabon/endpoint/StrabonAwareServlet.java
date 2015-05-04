package eu.earthobservatory.strabon.endpoint;

import eu.earthobservatory.runtime.generaldb.Strabon;
import org.openrdf.query.MalformedQueryException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;

/**
 * An abstract servlet responsible for strabon related operations.
 */
public abstract class StrabonAwareServlet extends HttpServlet {

    /**
     * Wrapper over Strabon
     */
    private StrabonBeanWrapper strabonWrapper;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        strabonWrapper = StrabonBeanWrapper.resolve(getServletContext());
    }


    boolean atemptConnection() {
        return strabonWrapper.init();
    }

    void populateRequest(HttpServletRequest request) {
        strabonWrapper.populateRequest(request);
    }

    boolean isStrabonInitialized() {
        return strabonWrapper.getStrabon() != null;
    }

    void query(String query, String name, OutputStream out)
        throws Exception {
        strabonWrapper.query(query, name, out);
    }

    void describe(String query, String format, OutputStream out) throws Exception {
        strabonWrapper.describe(query, format, out);
    }

    void closeConnection() {
        strabonWrapper.closeConnection();
    }

    void setConnectionDetails(String dbname, String username, String password, String port,
        String hostname, String dbengine) {
        strabonWrapper.setConnectionDetails(dbname, username, password, port, hostname, dbengine);
    }

    String addLimit(String query, String maxLimit) {
        return strabonWrapper.addLimit(query, maxLimit);
    }

    String getgChartString() {
        return strabonWrapper.getgChartString();
    }

    void store(String data, String graph, String name, Boolean inference, boolean url) throws Exception {
        strabonWrapper.store(data, graph, name, inference, url);
    }

    void update(String query) throws MalformedQueryException {
        Strabon strabon = strabonWrapper.getStrabon();
        strabon
            .update(query, strabon.getSailRepoConnection());
    }
}
