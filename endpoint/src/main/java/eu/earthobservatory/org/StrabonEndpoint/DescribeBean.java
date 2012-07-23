package eu.earthobservatory.org.StrabonEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.rio.RDFFormat;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper.Entry;

public class DescribeBean extends HttpServlet{

    private static final long serialVersionUID = -7541662133934957148L;

	private StrabonBeanWrapper strabonWrapper;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
		doPost(request, response);
	}

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String query = request.getParameter("SPARQLQuery");
		if (query != null) {
			query = URLDecoder.decode(request.getParameter("SPARQLQuery"), "UTF-8");	
		}
		
		// get the RDF format (checks the "format" and then the "accept" parameters)
        String reqFormat = getFormat(request);
		
		if (reqFormat == null || reqFormat.equals("HTML")) { // browser view
			response.setContentType("text/html; charset=UTF-8");
		    reqFormat = "HTML";
		    
		    PrintWriter out = response.getWriter();

			appendHTML1a(out, "");
			appendHTMLQ(out, strabonWrapper);
			appendHTML1b(out);

			if (query != null) {
				out.write(query);
			}

			appendHTML2(out, reqFormat);

			String answer = "";
			if (query != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				try {
					// we use the default N-Triples format in case of HTML output
					strabonWrapper.describe(query, "N-Triples", bos);
					answer = bos.toString();
						
				} catch (Exception e) {
						appendHTML3(out, e.getMessage());
				}
			}

			out.append("</table></td></tr></table>");
			if (!answer.equals("")) {
				out.println("<style type=\"text/css\">");
				out.println("table.result    {border:1px solid #777777;}");
				out.println("table.result tr {border:1px dashed grey;}");
				out.println("table.result th {background-color:grey; color:black;}");
				out.println("</style>");
				out.println("<PRE>");
				out.append(StringEscapeUtils.escapeHtml(answer));
				out.println("</PRE>");
			}
			
			appendHTML4(out);
			appendHTML5(out);
		    
			response.setStatus(HttpServletResponse.SC_OK);
			out.flush();
			
	    } else {	    	
	    	RDFFormat rdfFormat = RDFFormat.valueOf(reqFormat);
	    	
	    	// assuming N-Triples
	    	if (rdfFormat == null) {
	    		rdfFormat = RDFFormat.valueOf("N-Triples");
	    	}
	    	
	    	response.setContentType(rdfFormat.getDefaultMIMEType());
		    response.setHeader("Content-Disposition", 
		    		"attachment; filename=describe." + rdfFormat.getDefaultFileExtension() + "; " + rdfFormat.getCharset());
		    
		    if (query != null) {
				try {
					strabonWrapper.describe(query, rdfFormat.getName(), response.getOutputStream());
					response.setStatus(HttpServletResponse.SC_OK);
					
				} catch (Exception e) {
					
					response.getOutputStream().print(ResponseMessages.getXMLHeader());
					response.getOutputStream().print(ResponseMessages.getXMLException(e.getMessage()));
					response.getOutputStream().print(ResponseMessages.getXMLFooter());
				}
			}
		    
		    response.getOutputStream().flush();
	    }
	}
	
    /**
     * Determines the RDF format to use. If the client has specified
     * the "format" parameter, then it is favored over the "accept"
     * parameter. If "format" is not specified, we determine the RDF 
     * format according to the "accept" parameter. 
     * 
     * @param request
     * @return
     */
    private String getFormat(HttpServletRequest request) {
        
        String reqFormat = request.getParameter("format");
        
        if (reqFormat == null) {
                String reqAccept = request.getHeader("accept");
                
                if (reqAccept != null) {
                        // check whether the "accept" parameter contains any 
                        // of the mime types of any RDF format
                        for (RDFFormat format : RDFFormat.values()) {
                                for (String mimeType : format.getMIMETypes()) {
                                        if (reqAccept.contains(mimeType)) {
                                                reqFormat = format.getName();
                                                break;
                                        }
                                }
                        }
                }
        }
                
        return reqFormat;
    }

    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get StrabonWrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}

	protected static void appendHTML1a(PrintWriter out, String pathToKML) {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");		
		out.println("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /> ");
		out.println("<style type=\"text/css\"> ");
		out.println("<!--");
		out.println(".style3 {font-size: 15px}");
		out.println(".style4 {font-size: 12px}");
		out.println(".style5 {font-size: 15px;font-weight: bold;}");
		out.println(".style6 {color: #FF0000}");
		out.println(".style7 {font-size: 14px}");
		out.println(" .hidden { visibility: hidden }");
		out.println("-->");
		out.println("</style> ");
		out.println("<title>TELEIOS: Strabon Endpoint</title>");
		out.println("</head>");
		out.println("<body topmargin=\"0\" leftmargin=\"0\" link=\"#FFFFFF\" vlink=\"#FFFFFF\" alink=\"#FFFFFF\" onload=\"initialize()\">");
		out.println("  <TABLE width=\"100%\" BORDER=0 CELLPADDING=0 CELLSPACING=0>");
		out.println("    <TR>");
		out.println("		");
		out.println("      <TD height=\"16\" background=\"images/pixi_white.gif\"><img src=\"images/pixi_white.gif\" width=\"16\" height=\"16\"></TD>");
		out.println("	</TR>");
		out.println("	<TR>");
		out.println("      <TD height=\"1\"></TD>");
		out.println("	</TR>		");
		out.println("	<TR>");
		out.println("      <TD height=\"60\" background=\"images/nav2_bg.gif\"> ");
		out.println("        <table width=\"100%\" border=\"0\">");
		out.println("         <tr>");
		out.println("            <td width=\"1\"><img src=\"images/nav2_bg.gif\" width=\"1\" height=\"60\"></td>");
		out.println("            <td valign=\"top\" width=\"80px\"><img border=\"0\" src=\"images/teleios_logo.png\" /></td>");
		out.println("            <td valign=\"top\" align=\"left\">");
		out.println("            <span class=\"logo\"> &nbsp stSPARQL Endpoint</span><br><span class=\"style4\"></span></td>");
		out.println("          </tr>");
		out.println("        </table> </TD>");
		out.println("	</TR>");
		out.println("	<TR>");
		out.println("      <TD height=\"50\" id=\"intro\">");
		out.println("On this page you can execute stSPARQL queries against the Strabon backend. " +
				"The dataset is based on  the following ontologies: " +
				"<a href=\"http://harmonisa.uni-klu.ac.at/content/land-use-land-cover-ontologies\" > Corine Land Cover </a>, " +
				"<a > Greek Administrative Geography(Kallikratis), </a>" +
				"<a href=\"http://labs.mondeca.com/dataset/lov/details/vocabulary_lgdo.html\" > Linked Geodata </a> " +
				" and <a href=\"http://www.geonames.org/search.html?q=ontology&country=\" >  geonames </a>." +
				"We also use the <a href=\"images/graph.png\">NOA ontology</a> we developed for the <a href=\"http://www.space.noa.gr/ \">NOA </a> use case of the European FP7 project " +
				"<a href=\"http://www.earthobservatory.eu/\" >TELEIOS </a>. ") ;
		out.println("<a onclick=\"return toggleMe('par')\" />(More) </a> <br>");
		out.println(" <p id=\"par\"> In this context NOA has been developing a real-time fire hotspot detection service for effectively monitoring a " +
				"fire-front. The technique is based on the use of acquisitions originating from the SEVIRI (Spinning Enhanced Visible and " +
				"Infrared Imager) sensor, on top of MSG-1 (Meteosat Second Generation satellite, renamed to Meteosat-8) and MSG-2 (renamed to " +
				"Meteosat-9) satellite platforms. Since 2007, NOA operates an MSG/SEVIRI acquisition station, and has been systematically archiving" +
				" raw satellite images on a 5 and 15 minutes basis, the respective temporal resolutions of MSG-1 and MSG-2. The acquired data are then annotated " +
				"using the stRDF model and can be queried using the stSPARQL query language. </p>  ");
		out.println("On the left sidebar, some example stSPARQL queries are provided. The NOA use case is described in more detail in the VLDB application paper " +
				"<a href=\"\"> here. </a> ");
		out.println("      </TD>");
		out.println("	</TR>");
		out.println("</TABLE>");
		out.println("<form " +
				"enctype=\"UTF-8\" " +
				"accept-charset=\"UTF-8\" "+
				"method=\"post\"" +
				">");

		out.println("<table border=\"0\" width=\"100%\"><tr> ");
		out.println("<td width=\"90\" valign=\"top\" bgcolor=\"#dfe8f0\"> ");
		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"165\" id=\"navigation\"> ");
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"Query\" class=\"navText\">Query</a></td></tr> ");
        out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"Describe?format=HTML\" class=\"navText\">Describe</a></td></tr> ");
	}

	protected static void appendHTML1b(PrintWriter out) {	
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"store.jsp\" class=\"navText\" title=\"Store triples\">Store</a></td></tr> ");;
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"javascript:history.go(0)\" class=\"navText\" title=\"Clear editor\">Clear</a></td></tr> ");
		out.println("</table>");
		out.println("</td>");
		out.println("<td width=\"*\" valign=\"top\" >"); 

		out.println("<table cellspacing=\"5\">");
		out.println("<tr>");
		out.println("<td id=\"output\" \">stSPARQL Query:</td>");
		out.println("<td id=\"output\" \"><textarea name=\"SPARQLQuery\" title=\"pose your query/update here\" rows=\"15\" cols=\"100\">");
	}

	protected static void appendHTML2(PrintWriter out, String format) {
		out.println("</textarea></td>");
		out.println("</tr>");
		out.println("<tr>");

		out.println("<td id=\"output\";\"><center>Output Format:<br/><select name=\"format\" title=\"select one of the following output format types\">");
		
		ArrayList<String> formats = new ArrayList<String>(Arrays.asList("HTML", "N-Triples", "RDF/XML", "N3", "TURTLE", "TRIG", "TRIX", "BinaryRDF"));
		
		for (String rdfFormat: formats) {
			out.print("<option ");
			if (rdfFormat.equalsIgnoreCase(format)) {
				out.print("selected");
			}
			
			out.println(" value=\"" + rdfFormat + "\">" + rdfFormat + "</option>");
		}
		
		out.println("</select></center></td>");
		out.println("<td colspan=2 \"><br/><center><input type=\"submit\" title=\"execute query\" value=\"Describe\" name=\"submit\" /></center><br/></td>");
		out.println("</tr>");
	}

	protected static void appendHTML3(PrintWriter out, String errorMessage) {
		out.println("<tr>");
		out.println("<td id=\"output\" \">Result: </td>");
		out.println("<td id=\"output\" \">");
		out.println(errorMessage);
		out.println("</td>");
		out.println("</tr>");
	}

	protected static void appendHTML4(PrintWriter out) {
		out.println("<br/><br/>");
		out.println("</form>");
	}

	protected static void appendHTML5(PrintWriter out) {
		out.println("</body>");
		out.println("</html>");
	}

	protected static void appendHTMLQ(PrintWriter out, StrabonBeanWrapper strabonWrapper) throws UnsupportedEncodingException {
		out.println("<tr><td id=\"twidth\">");
		List<Entry> entries = strabonWrapper.getEntries();
		Iterator<Entry> it = entries.iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			out.println(createLink(entry));
		}
		out.println("</td></tr> ");
	}
	
	private static String createLink(Entry entry) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer(1024);
		buf.append("<a href=\"");
		buf.append(entry.getBean());
		buf.append("?SPARQLQuery=");
		buf.append(URLEncoder.encode(entry.getStatement(), "UTF-8"));
		buf.append("&format=");
		buf.append(entry.getFormat()+"\"");
		buf.append("title="+"\""+entry.getTitle());
		buf.append("\">&nbsp;&middot;&nbsp;");
		buf.append(entry.getLabel());
		buf.append("</a><br/>");
		
		return buf.toString();
	}
}
