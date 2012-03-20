package eu.earthobservatory.org.StrabonEndpoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
 

public class QueryBean extends HttpServlet {
	
	private static final long serialVersionUID = -378175118289907707L;
	
	public QueryBean() {}
	
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
			
			public String getTinyURL(String fullURL){
				String tinyUrl = null;
				
				try {
					HttpClient httpclient = new HttpClient();
					HttpMethod method = new GetMethod("http://tinyurl.com/api-create.php"); 
					method.setQueryString(new NameValuePair[]{new NameValuePair("url",fullURL)});
					httpclient.executeMethod(method);
					tinyUrl = method.getResponseBodyAsString();
					method.releaseConnection();
					
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return tinyUrl;
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
		
//		System.out.println("\n\n\n\n\n\n\nrequest: ");
//		System.out.println("HEADERS");
//	    Enumeration headerNames = request.getHeaderNames();
//	    while(headerNames.hasMoreElements()) {
//	      String headerName = (String)headerNames.nextElement();
//	      System.out.println(headerName + ":" +request.getHeader(headerName));
//	    }
//	    System.out.println("ATTRIBUTES");
//	    Enumeration attributeNames = request.getAttributeNames();
//	    while(attributeNames.hasMoreElements()) {
//	      String attributeName = (String)attributeNames.nextElement();
//	      System.out.println(attributeName + ":" +request.getAttribute(attributeName).toString());
//	    }
//	    Enumeration paramNames = request.getParameterNames();
//	    while(paramNames.hasMoreElements()) {
//	      String paramName = (String)paramNames.nextElement();
//	      System.out.print(paramName + ":");
//	      String[] paramValues = request.getParameterValues(paramName);
//	      if (paramValues.length == 1) {
//	        String paramValue = paramValues[0];
//	        if (paramValue.length() == 0)
//	          System.out.print("No Value");
//	        else
//	          System.out.print(paramValue + " ");
//	      } else {
//	        for(int i=0; i<paramValues.length; i++) {
//	          System.out.println(":" + paramValues[i]);
//	        }
//	      }	    
//	    }
//		
//		System.out.println("\n\n\n\n\nREQUEST.CONTENTTYPE='"+request.getContentType()+"'\n\n\n\n\n");
		
		DataHive hive = new DataHive(); 
		
		hive.setSPARQLQuery(request.getParameter("SPARQLQuery"));
		//System.out.println("SPARQLQuery = " + this.SPARQLQuery);
		
		String reqFormat = (request.getParameter("format") == null) ? "" : request.getParameter("format");
		String reqAccept = (request.getHeader("accept") == null) ? "" : request.getHeader("accept");
		String reqFuncionality = (request.getParameter("submit") == null) ? "" : request.getParameter("submit");
		
		// check whether Update submit button was fired
		boolean isUpdate = (reqFuncionality.equals("Update") ? true:false);
		
		if ((reqFormat == "") && (reqAccept == "")) {
			hive.setFormat("HTML");
			response.setContentType("text/html; charset=UTF-8");			
		} else if (reqAccept.contains("application/vnd.google-earth.kml+xml")) {
			response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
			hive.setFormat("KML");
		} else if (reqAccept.contains("application/sparql-results+xml")) {			
			response.setContentType("application/sparql-results+xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqAccept.contains("text/xml")) {
			response.setContentType("text/xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqFormat.equalsIgnoreCase("KML")) {
			response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
			hive.setFormat("KML");
		} else if (reqFormat.equalsIgnoreCase("SPARQLRESULTS"))  {
			response.setContentType("application/sparql-results+xml; charset=UTF-8");
			hive.setFormat("XML");
		} else if (reqFormat.equalsIgnoreCase("XML"))  {
			response.setContentType("text/xml; charset=UTF-8");
			hive.setFormat("XML");
		} else {
			response.setContentType("text/html; charset=UTF-8");
			hive.setFormat("HTML");
		}
		//System.out.println("\n\n\n\n\format='"+this.format+"'\n\n\n\n\n");
		
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
		StrabonBeanWrapper strabonWrapper = (StrabonBeanWrapper)applicationContext.getBean("strabonBean");
		
		if (isUpdate) {
			try {
				strabonWrapper.getStrabon().update(hive.getSPARQLQuery(), strabonWrapper.getStrabon().getSailRepoConnection());
				response.setStatus(HttpServletResponse.SC_OK);
				
			} catch(MalformedQueryException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				PrintWriter out = response.getWriter();
				e.printStackTrace(out);
			}
			
			return;
		}

		PrintWriter out = response.getWriter();
		
		if ((hive.getFormat().equalsIgnoreCase("KML"))) {
			//try {
		    //     String url = "http://www.google.com";
		    //     java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		    //     }
		    //  catch (java.io.IOException e) {
		    //     System.out.println(e.getMessage());
		    //     }
			
			StringBuilder errorMessage = new StringBuilder ();
			String answer = evaluateQuery(strabonWrapper, hive.getFormat(), reqFuncionality, hive.getSPARQLQuery(), errorMessage);
			hive.setErrorMessage(errorMessage.toString());
            SecureRandom random = new SecureRandom();
            String temp = new BigInteger(130, random).toString(32);        
            
            try{
                Date date = new Date();
                
                FileUtils.forceMkdir(new File((String) context.getRealPath("/") + "/../ROOT/tmp/"));
            	
            	Iterator it = FileUtils.iterateFiles(new File((String) context.getRealPath("/") + "/../ROOT/tmp/"), null, false);
                while(it.hasNext()){
                	File tbd = new File(((File) it.next()).getAbsolutePath());
                	if (FileUtils.isFileOlder( new File(tbd.getAbsolutePath()), date.getTime())){
                        FileUtils.forceDelete(new File(tbd.getAbsolutePath()));
                		}
                }
                
            	File file =new File((String) context.getRealPath("/") + "/../ROOT/tmp/" + temp + ".kml");
 
    	        //if file doesnt exists, then create it
    	        if(!file.exists()){
    		       file.createNewFile();
    	        }
 
    	        FileWriter fw = new FileWriter((String) context.getRealPath("/") + "/../ROOT/tmp/" + temp + ".kml");
    	        BufferedWriter bw = new BufferedWriter(fw);
    	        bw.write(answer);
    	        bw.close();
    	        //FileUtils.forceDeleteOnExit(new File((String) context.getRealPath("/") + "/../ROOT/tmp/" + temp + ".kml"));
 
	            //System.out.println("Done");
 
    	     }catch(IOException e){
    		        e.printStackTrace();
    	           }
    	     
 			 response.setContentType("application/vnd.google-earth.kml+xml; charset=UTF-8");
 			 response.setDateHeader("Expires", 0);
 			 InetAddress thisIp =InetAddress.getLocalHost();
 			 response.setHeader("Location", request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml");
 			 response.setStatus(301);
             
 			 //out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
             //out.println("<html>");
             //out.println("<head>");
             //out.println("<title>:)</title>");
             //out.println("<meta http-equiv=\"REFRESH\" content=\"0;url=http://maps.google.com/maps?q=http://localhost:8080/tmp/temp.kml\"></HEAD>");
             //out.println("<BODY>");
             //out.println("Redirection WILL occur!");
             //out.println("</BODY>");
             //out.println("</HTML>");
			//out.println(answer);
			 
			 
    	    String pathToKML = "";
    	    //InetAddress thisIp = InetAddress.getLocalHost();
    	    
    	    if (answer!="")
			   pathToKML = request.getScheme() + "://" +  request.getServerName() +":" + request.getServerPort() +"/tmp/" + temp + ".kml";//"http://dl.dropbox.com/u/19752551/dlr.kml";
			
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
		} 
		else if ((hive.getFormat().equalsIgnoreCase("XML"))) {
			response.setContentType("text/xml; charset=UTF-8");
			StringBuilder errorMessage = new StringBuilder ();
			String answer = evaluateQuery(strabonWrapper, hive.getFormat(), reqFuncionality, hive.getSPARQLQuery(), errorMessage);
			hive.setErrorMessage(errorMessage.toString());
			out.println(answer);

		}
		else {
			
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
	}

	public String evaluateQuery(StrabonBeanWrapper strabonWrapper, String resultFormat, String reqFunctionality, String SPARQLQuery, StringBuilder errorMessage) {		
		String answer = "";

//		System.out.println("evaluateQuery: will call wrapper. Query  = " + this.SPARQLQuery);
//		System.out.println("evaluateQuery: will call wrapper. result = " + resultFormat);

		try {
			if (SPARQLQuery == null) {
				answer = "";
			} else {
				//System.out.println("evaluateQuery: Calling...");
				if (reqFunctionality.equals("Update")) {
				//if (((String)this.SPARQLQuery).toLowerCase().contains("insert") || ((String)this.SPARQLQuery).toLowerCase().contains("delete"))  { 
				   answer = (String)strabonWrapper.update(SPARQLQuery, resultFormat);
				   }
				else{
				   answer = (String)strabonWrapper.query(SPARQLQuery, resultFormat);
				}
				//System.out.println("evaluateQuery: Called...");
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

		//System.out.println("evaluateQuery: called wrapper. answer  = " + answer);
		//System.out.println("evaluateQuery: called wrapper. error   = " + ((this.errorMessage == null) ? "" : this.errorMessage));

		
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
	
	protected static void appendHTMLQ(PrintWriter out, StrabonBeanWrapper strabonWrapper) {
		out.println("<tr><td width=\"90\">");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery1()+"\">&nbsp;&middot;&nbsp;Query 1</a></br> ");		
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery2()+"\">&nbsp;&middot;&nbsp;Query 2</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery3()+"\">&nbsp;&middot;&nbsp;Query 3</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery4()+"\">&nbsp;&middot;&nbsp;Query 4</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery5()+"\">&nbsp;&middot;&nbsp;Query 5</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery6()+"\">&nbsp;&middot;&nbsp;Query 6</a></br> ");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery7()+"\">&nbsp;&middot;&nbsp;Query 7</a></br>");
		out.println("<a href=\"Query?SPARQLQuery="+strabonWrapper.getQuery8()+"\">&nbsp;&middot;&nbsp;Query 8</a> ");
		out.println("</td></tr> ");
	}
}
