package eu.earthobservatory.org.StrabonEndpoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class QueryBean extends HttpServlet {

	private static final long serialVersionUID = -378175118289907707L;

	private ServletContext context; 
	private StrabonBeanWrapper strabonWrapper;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
			{
		doPost(request, response);
			}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
			{
		final class DataHive{
			private String format;
			private String SPARQLQuery;
			private String errorMessage;

			DataHive(){
				this.format = null;
				this.SPARQLQuery = null;
				this.errorMessage = null;				
			}

			public String getSPARQLQuery() {
				return SPARQLQuery;
			}

			public void setSPARQLQuery(String sPARQLQuery) {
				SPARQLQuery = sPARQLQuery;
			}

			public String getFormat() {
				return format;
			}

			public void setFormat(String fFormat) {
				format = fFormat;
			}

			public String getErrorMessage() {
				return errorMessage;
			}

			public void setErrorMessage(String error) {
				this.errorMessage = error;
			}
		}

		DataHive hive = new DataHive(); 

		hive.setSPARQLQuery(request.getParameter("SPARQLQuery"));

		String reqFormat = (request.getParameter("format") == null) ? "" : request.getParameter("format");
		String reqAccept = (request.getHeader("accept") == null) ? "" : request.getHeader("accept");
		String reqFuncionality = (request.getParameter("submit") == null) ? "" : request.getParameter("submit");

		// check whether Update submit button was fired
		if (reqFuncionality.equals("Update")) { // only for executions from web browsers
			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			response.sendRedirect("Update?SPARQLQuery=" + hive.getSPARQLQuery());

			return;
		}

		if ((reqFormat == "") && (reqAccept == "")) {
			hive.setFormat("HTML");
			response.setContentType("text/html; charset=UTF-8");			
		} else if (reqAccept.contains("application/vnd.google-earth.kml+xml")) {
			response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
			hive.setFormat("KML");
		} else if (reqAccept.contains("application/vnd.google-earth.kmz")) {
			response.setContentType("application/vnd.google-earth.kmz; charset=UTF-8");
			hive.setFormat("KMZ");
		} else if (reqAccept.contains("application/sparql-results+xml")) {			
			response.setContentType("application/sparql-results+xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqAccept.contains("text/xml")) {
			response.setContentType("text/xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqFormat.equalsIgnoreCase("KML")) {
			response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
			hive.setFormat("KML");
		} else if (reqFormat.equalsIgnoreCase("KMZ")) {
			response.setContentType("application/vnd.google-earth.kmz; charset=UTF-8");
			hive.setFormat("KMZ");
		} else if (reqFormat.equalsIgnoreCase("SPARQLRESULTS"))  {
			response.setContentType("application/sparql-results+xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqFormat.equalsIgnoreCase("XML"))  {
			response.setContentType("text/xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqFormat.equalsIgnoreCase("KMLMAP"))  {
			response.setContentType("text/html; charset=UTF-8");
			hive.setFormat("KMLMAP");
		} else if (reqFormat.equalsIgnoreCase("KMZMAP"))  {
			response.setContentType("text/html; charset=UTF-8");
			hive.setFormat("KMZMAP");
		} else {
			response.setContentType("text/html; charset=UTF-8");
			hive.setFormat("HTML");
		}

		PrintWriter out = response.getWriter();

		if ((hive.getFormat().equalsIgnoreCase("KML")) || (hive.getFormat().equalsIgnoreCase("KMZ"))) {
			StringBuilder errorMessage = new StringBuilder ();
			String answer = evaluateQuery(strabonWrapper, hive.getFormat(), reqFuncionality, hive.getSPARQLQuery(), errorMessage);
			hive.setErrorMessage(errorMessage.toString());
			SecureRandom random = new SecureRandom();
			String temp = new BigInteger(130, random).toString(32);   
			
			String basePath = context.getRealPath("/") + "/../ROOT/tmp/";

			try{
				Date date = new Date();

				FileUtils.forceMkdir(new File(basePath));

				Iterator it = FileUtils.iterateFiles(new File(basePath), null, false);
				while(it.hasNext()){
					File tbd = new File(((File) it.next()).getAbsolutePath());
					if (FileUtils.isFileOlder( new File(tbd.getAbsolutePath()), date.getTime())){
						FileUtils.forceDelete(new File(tbd.getAbsolutePath()));
					}
				}

				File file =new File(basePath + temp + ".kml");

				//if file doesnt exists, then create it
				if(!file.exists()){
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(basePath + temp + ".kml");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(answer);
				bw.close();
				//FileUtils.forceDeleteOnExit(new File((String) context.getRealPath("/") + "/../ROOT/tmp/" + temp + ".kml"));

				//System.out.println("Done");

				if (hive.getFormat().equalsIgnoreCase("KMZ")) {
					//compress
					byte[] buf = new byte[1024];
					String outFilename = basePath + temp + ".kmz";
					ZipOutputStream kmzout = new ZipOutputStream(new FileOutputStream(outFilename));

					// Compress the files
					FileInputStream in = new FileInputStream(basePath + temp + ".kml");

					// Add ZIP entry to output stream.
					kmzout.putNextEntry(new ZipEntry("doc.kml"));

					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						kmzout.write(buf, 0, len);
					}

					// Complete the entry
					kmzout.closeEntry();
					in.close();

					// Complete the ZIP file
					kmzout.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}

			response.setDateHeader("Expires", 0);			
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

			String pathToKML = "";
			if (hive.getFormat().equalsIgnoreCase("KML")) {
				response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
				response.setHeader("Location", request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml");
				if (answer!="")
					pathToKML = request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml";
			} else {
				response.setContentType("application/vnd.google-earth.kmz; charset=UTF-8");
				response.setHeader("Location", request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kmz");
				if (answer!="")
					pathToKML = request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kmz";
			}
			
			appendHTML1a(out,pathToKML);

			appendHTMLQ(out, strabonWrapper);

			appendHTML1b(out);

			if (hive.getSPARQLQuery() != null)
				out.write(hive.getSPARQLQuery());

			appendHTML2(out);

			out.append("</table></td></tr></table>");

			appendHTML4(out);
			if (answer!="")
				//out.append("<div id=\"map_canvas\"></div>");
				out.append("");
			appendHTML5(out);

		} else if ((hive.getFormat().equalsIgnoreCase("KMLMAP")) || (hive.getFormat().equalsIgnoreCase("KMZMAP"))) {

			StringBuilder errorMessage = new StringBuilder ();
			String answer = evaluateQuery(strabonWrapper, "KML", reqFuncionality, hive.getSPARQLQuery(), errorMessage);
			hive.setErrorMessage(errorMessage.toString());
			SecureRandom random = new SecureRandom();
			String temp = new BigInteger(130, random).toString(32);   
			
			String basePath = context.getRealPath("/") + "/../ROOT/tmp/";

			try{
				Date date = new Date();

				FileUtils.forceMkdir(new File(basePath));

				Iterator it = FileUtils.iterateFiles(new File(basePath), null, false);
				while(it.hasNext()){
					File tbd = new File(((File) it.next()).getAbsolutePath());
					if (FileUtils.isFileOlder( new File(tbd.getAbsolutePath()), date.getTime())){
						FileUtils.forceDelete(new File(tbd.getAbsolutePath()));
					}
				}

				File file =new File(basePath + temp + ".kml");

				//if file doesnt exists, then create it
				if(!file.exists()){
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(basePath + temp + ".kml");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(answer);
				bw.close();
				//FileUtils.forceDeleteOnExit(new File((String) context.getRealPath("/") + "/../ROOT/tmp/" + temp + ".kml"));

				//System.out.println("Done");

				if (hive.getFormat().equalsIgnoreCase("KMZMAP")) {
					//compress
					byte[] buf = new byte[1024];
					String outFilename = basePath + temp + ".kmz";
					ZipOutputStream kmzout = new ZipOutputStream(new FileOutputStream(outFilename));

					// Compress the files
					FileInputStream in = new FileInputStream(basePath + temp + ".kml");

					// Add ZIP entry to output stream.
					kmzout.putNextEntry(new ZipEntry("doc.kml"));

					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						kmzout.write(buf, 0, len);
					}

					// Complete the entry
					kmzout.closeEntry();
					in.close();

					// Complete the ZIP file
					kmzout.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}

			response.setDateHeader("Expires", 0);			
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

			String pathToKML = "";
			if (hive.getFormat().equalsIgnoreCase("KMLMAP")) {
				//response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
				response.setHeader("Location", request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml");
				if (answer!="")
					pathToKML = request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml";
			} else {
				//response.setContentType("application/vnd.google-earth.kmz; charset=UTF-8");
				response.setHeader("Location", request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kmz");
				if (answer!="")
					pathToKML = request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kmz";
			}
			
			appendHTML1a(out,pathToKML);

			appendHTMLQ(out, strabonWrapper);

			appendHTML1b(out);

			if (hive.getSPARQLQuery() != null)
				out.write(hive.getSPARQLQuery());

			appendHTML2(out);

			out.append("</table></td></tr></table>");

			appendHTML4(out);
			if (answer!="")
				out.append("<div id=\"map_canvas\"></div>");
			appendHTML5(out);
		} else if ((hive.getFormat().equalsIgnoreCase("XML"))) {
			int status_code = HttpServletResponse.SC_OK;
			String answer = "";

			try {
				// execute query
				answer = (String) strabonWrapper.query(hive.getSPARQLQuery(), hive.getFormat());

			} catch (MalformedQueryException e) {
				status_code = HttpServletResponse.SC_BAD_REQUEST;
				answer = e.getMessage();

			} catch (RepositoryException e) {
				status_code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				answer = e.getMessage();

			} catch (QueryEvaluationException e) {
				status_code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				answer = e.getMessage();

			} catch (TupleQueryResultHandlerException e) {
				status_code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				answer = e.getMessage();

			} catch (ClassNotFoundException e) {
				status_code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				answer = e.getMessage();
			}

			// write response to client
			response.setContentType("text/xml; charset=UTF-8");
			response.setStatus(status_code);
			if (status_code == HttpServletResponse.SC_OK) {
				response.getWriter().append(answer);

			} else {
				response.getWriter().append(ResponseMessages.getXMLHeader());
				response.getWriter().append(ResponseMessages.getXMLException(answer));
				response.getWriter().append(ResponseMessages.getXMLFooter());
			}

		} else { // HTML

			appendHTML1a(out,"");

			appendHTMLQ(out, strabonWrapper);

			appendHTML1b(out);

			if (hive.getSPARQLQuery() != null)
				out.write(hive.getSPARQLQuery());

			appendHTML2(out);

			String answer = "";
			if (hive.getSPARQLQuery() != null) {
				StringBuilder errorMessage = new StringBuilder ();
				answer = evaluateQuery(strabonWrapper, hive.getFormat(), reqFuncionality, hive.getSPARQLQuery(), errorMessage);
				hive.setErrorMessage(errorMessage.toString());
				if (hive.getErrorMessage() != null) {
					appendHTML3(out, hive.getErrorMessage());
				}
			}

			out.append("</table></td></tr></table>");
			if (!answer.equals("")) {
				out.println("<style type=\"text/css\">");
				out.println("table.result    {border:1px solid #777777;}");
				out.println("table.result tr {border:1px dashed grey;}");
				out.println("table.result th {background-color:grey;color:black;}");
				out.println("</style>");
				out.println("<table class=\"result\">");
				out.append(answer);
				out.append("</table>");
			}
			appendHTML4(out);
			appendHTML5(out);
		}
		out.flush();
			}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get StrabonWrapper
		context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}

	public String evaluateQuery(StrabonBeanWrapper strabonWrapper, String resultFormat, String reqFunctionality, String SPARQLQuery, StringBuilder errorMessage) {		
		String answer = "";

		try {
			if (SPARQLQuery == null) {
				answer = "";
			} else {
				answer = (String) strabonWrapper.query(SPARQLQuery, resultFormat);
			}
		} catch (MalformedQueryException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (RepositoryException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (TupleQueryResultHandlerException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage.append(e.getMessage());
		}

		return answer;		
	}

	protected static void appendHTML1a(PrintWriter out, String pathToKML) {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		out.println("<link href=\"http://code.google.com/apis/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\" type=\"text/css\" />");
		out.println("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /> ");
		out.println("<script type=\"text/javascript\" src=\"http://maps.googleapis.com/maps/api/js?sensor=false\"></script>");
		out.println("<script type=\"text/javascript\">");
		out.println("function initialize() {");
		out.println("  var chicago = new google.maps.LatLng(41.875696,-87.624207);");
		out.println("  var myOptions = {");
		out.println("    zoom: 11,");
		out.println("    center: chicago,");
		out.println("   mapTypeId: google.maps.MapTypeId.ROADMAP");
		out.println("  }");
		out.println("");
		out.println("  var map = new google.maps.Map(document.getElementById(\"map_canvas\"), myOptions);");
		out.println("");
		out.println("  var ctaLayer = new google.maps.KmlLayer('" + pathToKML + "');");
		out.println("  ctaLayer.setMap(map);");
		out.println("}");
		out.println("</script>");
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
		out.println("      <TD height=\"1\" background=\"images/top_bg_blue.gif\"></TD>");
		out.println("	</TR>		");
		out.println("	<TR>");
		out.println("      <TD height=\"60\" background=\"images/nav2_bg.gif\"> ");
		out.println("        <table width=\"100%\" border=\"0\">");
		out.println("         <tr>");
		out.println("            <td width=\"1\"><img src=\"images/nav2_bg.gif\" width=\"1\" height=\"60\"></td>");
		out.println("            <td valign=\"top\" width=\"80px\"><img border=\"0\" src=\"images/teleios_logo.png\"/></td>");
		out.println("            <td valign=\"top\" align=\"left\">");
		out.println("            <span class=\"logo\">Strabon Endpoint</span><br><span class=\"style4\">based on Strabon</span></td>");
		out.println("          </tr>");
		out.println("        </table> </TD>");
		out.println("	</TR>");
		out.println("	<TR>");
		out.println("      <TD height=\"21\" background=\"images/nav1_bg1.gif\">");
		out.println("      </TD>");
		out.println("	</TR>");
		out.println("	<TR>");
		out.println("      <TD height=\"2\" background=\"images/top_bg_blue.gif\"></TD>");
		out.println("	</TR>");
		out.println("</TABLE>");
		out.println("<form method=\"get\">");

		out.println("<table border=\"0\" width=\"100%\"><tr> ");
		out.println("<td width=\"90\" valign=\"top\" bgcolor=\"#dfe8f0\"> ");
		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"165\" id=\"navigation\"> ");
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"Query\" class=\"navText\">Query</a></td></tr> ");
	}

	protected static void appendHTML1b(PrintWriter out) {	
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"store.jsp\" class=\"navText\">Store</a></td></tr> ");
		out.println("<tr><td width=\"90\" class=\"style4\"><a href=\"javascript:history.go(0)\" class=\"navText\">Clear</a></td></tr> ");
		out.println("</table>");
		out.println("</td>");
		out.println("<td width=\"*\" valign=\"top\" >"); 

		out.println("<table cellspacing=\"5\">");
		out.println("<tr>");
		out.println("<td style=\"border: 1px dashed #bbbbbb;\">stSPARQL Query:</td>");
		out.println("<td style=\"border: 1px dashed #bbbbbb;\"><textarea name=\"SPARQLQuery\" rows=\"15\" cols=\"100\">");
	}

	protected static void appendHTML2(PrintWriter out) {
		out.println("</textarea></td>");
		//		out.println("<td style=\"border: 1px dashed #bbbbbb;\"><input type=\"radio\" name=\"format\" value=\"KML\">KML<br/>");
		//		out.println("<input type=\"radio\" name=\"format\" value=\"HTML\">HTML</td>");
		out.println("<td style=\"border: 1px dashed #bbbbbb;\"><center>Output Format:<br/><select name=\"format\">");
		out.println("	<option value=\"HTML\">HTML</option>");
		out.println("	<option value=\"KML\">KML</option>");
		out.println("	<option value=\"XML\">XML</option>");
		out.println("</select></center></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 style=\"border: 1px dashed #bbbbbb;\"><br/><center><input type=\"submit\" value=\"Query\" name=\"submit\" style=\"width: 400px\"/><br/><input type=\"submit\" value=\"Update\" name=\"submit\" style=\"width: 400px\"/></center><br/></td>");
		out.println("</tr>");
	}

	protected static void appendHTML3(PrintWriter out, String errorMessage) {
		out.println("<tr>");
		out.println("<td style=\"border: 1px dashed #bbbbbb;\">Result: </td>");
		out.println("<td style=\"border: 1px dashed #bbbbbb;\">");
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
		out.println("<tr><td width=\"90\">");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery1(), "UTF-8")+ "&" + strabonWrapper.getFormat() + "\">&nbsp;&middot;&nbsp;Query 1</a></br> ");		
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery2(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 2</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery3(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 3</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery4(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 4</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery5(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 5</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery6(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 6</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery7(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 7</a></br>");
		out.println("<a href=\"Query?SPARQLQuery="+URLEncoder.encode(strabonWrapper.getQuery8(), "UTF-8")+ "&" + strabonWrapper.getFormat() +"\">&nbsp;&middot;&nbsp;Query 8</a> ");
		out.println("</td></tr> ");
	}
}
