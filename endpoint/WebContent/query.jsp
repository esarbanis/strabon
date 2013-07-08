<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper"%>
<%@page import="eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapperConfiguration"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.openrdf.query.TupleQueryResult"%>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.Common"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<script type="text/javascript" src="js/more_link.js"></script>
	<link rel="stylesheet" href="style.css" type="text/css" />
	<script type="text/javascript">
		function toggleMe(a) {
			var e = document.getElementById(a);
			if (!e) {
				return true;
			}
			if (e.style.display == "none") {
				e.style.display = "block";
			} else {
				e.style.display = "none";
			}
			return true;
		}
	</script>
	<script>
		$(document).ready(function() {
		var showChar = 100;
		var ellipsestext = "...";
		var moretext = "more";
		var lesstext = "less";
		$('.more').each(function() {
			var content = $(this).html();
	
			if(content.length > showChar) {
	
				var c = content.substr(0, showChar);
				var h = content.substr(showChar-1, content.length - showChar);
	
				var html = c + '<span class="moreelipses">'+ellipsestext+'</span>&nbsp;<span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">'+moretext+'</a></span>';
	
				$(this).html(html);
			}
	
		});
	
		$(".morelink").click(function(){
			if($(this).hasClass("less")) {
				$(this).removeClass("less");
				$(this).html(moretext);
			} else {
				$(this).addClass("less");
				$(this).html(lesstext);
			}
			$(this).parent().prev().toggle();
			$(this).prev().toggle();
			return false;
		});
	});
</script>
	<%
	// get the reference to StrabonBeanWrapper
	StrabonBeanWrapper strabonWrapper;
	//String arr = new String[2];
	List<String[]> results = new ArrayList<String[]>();
	String[] arr = new String[2];
	ServletContext context;
	context = getServletContext();
	WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
	strabonWrapper=(StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	
	// get query parameter or attribute (the attribute comes from ConnectionBean)
	String query = strabonWrapper.getPrefixes();
	if (request.getParameter("query") != null) {
		query = request.getParameter("query");
		
	} else if (request.getAttribute("query") != null) {
		query = (String) request.getAttribute("query");
		
	}
	
	if ("null".equals(query)) {
		query = "";
	}
	
	// get format parameter or attribute (the attribute comes from ConnectionBean)
	String selFormat = "HTML";
	if (request.getParameter("format") != null) {
		selFormat = request.getParameter("format");
		
	} else if (request.getAttribute("format") != null) {
		selFormat = (String) request.getAttribute("format");
		
	}
		
	// get handle parameter or attribute (the attribute comes from ConnectionBean)
	String handle = "";
	if (request.getParameter("handle") != null) {
		handle = request.getParameter("handle");
		
	} else if (request.getAttribute("handle") != null) {
		handle = (String) request.getAttribute("handle");
		
	}

	if (request.getAttribute("pathToKML") != null) {
	if ("map_local".equals(request.getAttribute("handle"))) {
%>
	<script type="text/javascript" src="js/geoxml3-kmz.js"></script>
	<script type="text/javascript" src="js/ProjectedOverlay.js"></script>	
<%
	}
%>
	<link href="http://code.google.com/apis/maps/documentation/javascript/examples/default.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript">
		function initialize() {
			var myOptions = {
				zoom: 11,
				mapTypeId: google.maps.MapTypeId.ROADMAP
			};
			
			// get KML filename
			var kml = '<%=request.getAttribute("pathToKML")%>';
			
			// create map
			var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
			<% if (request.getAttribute("pathToKML") == null) {%>
				// center at Brahames
				map.setCenter(new google.maps.LatLng(37.92253, 23.72275));
			<%}%>
			
		<%if ("map_local".equals(request.getAttribute("handle"))) {%>
			// display using geoxml3
			var myParser = new geoXML3.parser({map: map});
			myParser.parse(kml);
			
		<%} else {%>
			var ctaLayer = new google.maps.KmlLayer(kml);
			ctaLayer.setMap(map);
		<%}%>
		
		<%if ("map".equals(request.getAttribute("handle")) || "map_local".equals(request.getAttribute("handle"))) {%>	
			$('html, body').animate({
				scrollTop: $("#divResultsStart").offset().top
			}, 1500);
		<%}%>
		}
	</script>
	<%	} else { %>
 	<script type="text/javascript">
		function initialize() {
	<%	
	if (query != "" || selFormat != "" || handle != "") {
	%>
	$('html, body').animate({
		scrollTop: $("#divResultsStart").offset().top
	}, 1000);
	<%}%>
		}
	</script>
	<%}%>

 	<!-- jQuery start  -->
	<link type="text/css" href="style-menu.css" rel="stylesheet" />
	<script type="text/javascript" src="js/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui-1.8.23.custom.min.js"></script>
	<script type="text/javascript">
	$(function(){
			// Accordion
			$("#accordion").accordion({ 
				header: "h3",
				fillSpace: true,
				navigation: true,
				collapsible: true
			});
			//hover states on the static widgets
			$('#dialog_link, ul#icons li').hover(
				function() { $(this).addClass('ui-state-hover'); },
				function() { $(this).removeClass('ui-state-hover'); }
			);
	});
	</script>
	<style type="text/css">
		/*demo page css*/
		body{ font: 90% "Trebuchet MS", sans-serif; margin: 50px;}
		.container { height:410px; width:165px;}
		.demoHeaders { margin-top: 1em;}
		#dialog_link {padding: .4em 1em .4em 20px;text-decoration: none;position: relative;}
		#dialog_link span.ui-icon {margin: 0 5px 0 0;position: absolute;left: .2em;top: 50%;margin-top: -8px;}
		ul#icons {margin: 0; padding: 0;}
		ul#icons li {margin: 1px; position: relative; padding: 1px 0; cursor: pointer; float: left;  list-style: none;}
		ul#icons span.ui-icon {float: left; margin: 0 1px;}
	</style>
 	<!-- jQuery end -->
 
	<title>TELEIOS: Strabon Endpoint</title>
	   <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">

      // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart']});

      google.setOnLoadCallback(drawChart);

      // Set a callback to run when the Google Visualization API is loaded.


      // Callback that creates and populates a data table,
      // instantiates the pie chart, passes in the data and
      // draws it.
      
     
        	
      function drawChart() {
 		
        // Create the data table.
        var data = new google.visualization.DataTable();
        <% if (request.getAttribute("response") != null) {
        	if (request.getParameter("format").equals("CHART")) {
        		 results= (List<String[]>)request.getAttribute("response");
        		arr[0] = results.get(0)[0];
        		arr[0] = results.get(0)[1];	
        	%>
        <%
        int i=1;
        while(i <= results.size()){
        	arr =  results.get(i);
        %>	
  
        <% i++;} %>
        // Set chart options
        var options = {'title':'Displaying results in chart',
                       'width':400,
                       'height':300};

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));
        chart.draw(data, options);
        
        <%}}%>
      }
    </script>
</head>
<body topmargin="0" leftmargin="0" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF" onload="initialize()">

<!-- include TELEIOS header and description -->
<%@ include file="teleios-header.html"%>
<!-- include TELEIOS header and description -->

<FORM enctype="UTF-8" accept-charset="UTF-8" method="post" action="Query">
<INPUT type=hidden name="view" value="HTML"/>

<table border="0" width="100%">
<tr> 
	<td width="90" valign="top"> 
		<table border="0" cellspacing="0" cellpadding="0" width="165">  
		<tr><td id="twidth">
		<div class="container">
		<div id="accordion">
		<%
							
							Iterator <StrabonBeanWrapperConfiguration> entryListIterator = strabonWrapper.getEntries().iterator();
							boolean first = true;
							String hash = "";
							while(entryListIterator.hasNext())
							{
								StrabonBeanWrapperConfiguration entry = entryListIterator.next();
								
								if (entry.isHeader()) {
									if (!first) {
										%>
										</div></div>
										<%
									} else {
										first = false;
									}
									
									String label=entry.getLabel();
									String style = "", href = "";
									hash = new Integer(Math.abs(label.hashCode())).toString();
									href="href=\"#"+hash+"\"";									
									%>
									<div><h3><a <%=style%> <%=href%>><%=label%></a></h3><div>
									<%									
								} else if (entry.isBean()) {
									String label=entry.getLabel();
									String bean=entry.getBean();
									if(bean.equals("browse.jsp") || bean.equals("ChangeConnection"))
										continue;
									String style = "", href = "";
									hash = new Integer(Math.abs(label.hashCode()*bean.hashCode())).toString();
									href = "\"" +bean + "#"+ hash+"\"";
									style = "class=\"navText\"";
									%>
									<b>&middot;</b>&nbsp;<a class="linkText" href=<%=href%>><%=label%></a><br/>
									<%
								} else {
									String href="\""+URLEncoder.encode(entry.getBean(),"utf-8")+"?view=HTML&handle="+entry.getHandle()+"&query="+URLEncoder.encode(entry.getStatement(),"utf-8")+"&format="+URLEncoder.encode(entry.getFormat(),"utf-8")+(hash == "" ? "" : "#" + hash)+"\"";
									String title="\""+entry.getTitle()+"\"";
									String label=entry.getLabel();
									%>
									<b>&middot;</b>&nbsp;<a class="linkText" href=<%=href%> title=<%=title%>><%=label%></a><br/>
									<%
			
								}
							}
		%>
		</div>
		</div>
	</td>
</tr>
<!-- 
<tr><td width="90" class="style4"><a href="describe.jsp" class="navText">Describe</a></td></tr>
<tr><td width="90" class="style4"><a href="store.jsp" class="navText" title="Store triples">Store</a></td></tr>
 --> 
</table>
</td>
<td width="*" valign="top" >
<table cellspacing="5">
<%if (request.getAttribute("info") != null) { %>
	<!-- Info Message -->
  		<TR><TD colspan=2>
  		<CENTER><P><%=request.getAttribute("info") %></P></CENTER>
  		</TD></TR>
	<!-- Info Message -->
<%}%>
<tr>
	<td id="output" colspan=2>
		<div style="font-size:13px"> 
			You must be logged in to perform update queries.
		</div>
	</td>
</tr>
<tr>
<td id="output" style="width: 150px">stSPARQL Query:</td>
<td id="output"><textarea name="query" title="pose your query/update here" rows="20" cols="100"><%=query%></textarea></td>
</tr>
<tr>
	<td id="output">Output Format:</td>
	<td id="output">
		<select name="format" title="select one of the following output format types">
		<% 
		for (String format : Common.registeredQueryResultsFormatNames) {%>
				<OPTION value="<%=format%>"<%=format.equals(selFormat) ? "selected":""%>><%=format%></OPTION>
		<%}%>
		</select>
	</td>
</tr>
<tr>
	<td id="output">View Result:</td>
	<td id="output">
	<SELECT name="handle" title="select how you would like to view the result">
		<OPTION value="plain"<%= ("plain".equals(handle)) ? "selected":""%>>Plain</OPTION>
		<OPTION value="download"<%= ("download".equals(handle)) ? "selected":""%>>Download</OPTION>
		<OPTION value="map"<%= ("map".equals(handle)) ? "selected":""%>>On a map</OPTION>
		<OPTION value="map_local"<%= ("map_local".equals(handle)) ? "selected":""%>>On a map (localhost)</OPTION>
	</SELECT>
	</td>
</tr>

<tr>	
<td colspan=2 id="output"><br/><center>
<input type="submit" title="execute query" value="Query" name="submit" style="width: 350px" />
<input type="submit" title="execute update" value="Update" name="submit" style="width: 350px"/></center><br/></td>
</tr>

<% if (request.getAttribute("error") != null) {%>
	<!-- Error Message -->
	<TR>
		<TD id="output">Result: </TD><TD id="output"><%=request.getAttribute("error") %></TD>
	</TR>
	<!-- Error Message -->	
<%}%>
</table></td></tr></table><br/><br/>
</form>
<a name="#results">&nbsp;</a>
<div id="divResultsStart"></div>
	<!-- Response -->
<% if (request.getAttribute("response") != null) {
	if (Common.getHTMLFormat().equals(request.getParameter("format"))) {%>
		<%=request.getAttribute("response")%>
	<%} else { %>
	<PRE><%=request.getAttribute("response") %></PRE>
	<%}%>
<%}%>
	<!-- Response -->
<% if (request.getAttribute("pathToKML") != null) { %>
	<div id="map_canvas"></div>
<%}%>
<div id="divResultsEnd" style="height: 1px; width 1px"></div>
</body>
</html>
