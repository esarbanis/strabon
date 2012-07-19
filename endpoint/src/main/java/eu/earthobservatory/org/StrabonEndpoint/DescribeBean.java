package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.PrintWriter;


public class DescribeBean extends HttpServlet{

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.DescribeBean.class);

        private static final long serialVersionUID = -7541662133934957148L;

	/**
	* @param args
	* @throws Exception
	*/
	private StrabonBeanWrapper strabonWrapper;

        @Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		// get strabon wrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
		
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}

        @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
		doPost(request, response);
	}

        @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{

        	PrintWriter out = response.getWriter();
                out.println("Build a form or sth?");
        	out.flush();
        /*
        String[] args = NULL;

	if (args.length < 7) {
 		out.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <QUERY> <OUTPUT>");
 		out.println(" where <HOST> is the postgis database host to connect to");
 		out.println(" <PORT> is the port to connect to on the database host");
 		out.println(" <DATABASE> is the spatially enabled postgis database that Strabon will use as a backend, ");
 		out.println(" <USERNAME> is the username to use when connecting to the database ");
 		out.println(" <PASSWORD> is the password to use when connecting to the database");
 		out.println(" <QUERY> is the stSPARQL query to evaluate.");
 		out.println(" <OUTPUT> is the output file.");
 		System.exit(0);
		}
        else{

	String host = args[0];
	Integer port = new Integer(args[1]);
	String db = args[2];
	String user = args[3];
	String passwd = args[4];
	String queryString = args[5];
	String outFile = args[6];
        }

	Strabon strabon = null;
	try {
  		strabon = new Strabon(db, user, passwd, port, host, true);
  		strabon.describe(queryString, strabon.getSailRepoConnection(), outFile);
	} catch (Exception e) {
		logger.error("[Strabon.DescribeOp] Error during execution of DESCRIBE query.", e);
	} finally {
		if (strabon != null) {
			strabon.close();
		}
	}*/
	}
} 
