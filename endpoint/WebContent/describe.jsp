<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.Common"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>TELEIOS: Strabon Endpoint</title>
		<link rel="stylesheet" href="style.css" type="text/css"/> 
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
	</head>
<body topmargin="0" leftmargin="0" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF">

<!-- include TELEIOS header and description -->
<jsp:include page="teleios-header.html"/>
<!-- include TELEIOS header and description -->

<FORM enctype="UTF-8" accept-charset="UTF-8" method="post" action="Describe">
<INPUT type=hidden name="view" value="HTML"/>

<table border="0" width="100%"><tr> 
<td width="90" valign="top" class="style4"> 
<table border="0" cellspacing="0" cellpadding="0" width="165" id="navigation"> 
<tr><td width="90" class="style4"><a href="query.jsp" class="navText">Query</a></td></tr> 
<tr><td width="90" class="style4"><a href="store.jsp" class="navText" title="Store triples">Store</a></td></tr> 
</table>
</td>
<td width="*" valign="top">
<table cellspacing="5">
<tr>
<td id="output">stSPARQL Query:</td>
<td id="output"><textarea name="query" title="pose your DESCRIBE query here" rows="15" cols="100">
<%=request.getParameter("query") != null ? request.getParameter("query"):""%></textarea></td>
</tr>
<tr>
<td id="output"><center>Output Format:<br/>
<SELECT name="format" title="select one of the following RDF graph format types">
	<% for (String format : Common.registeredFormats) {%>
		<OPTION value="<%=format%>"><%=format%></OPTION>
	<%}%>
</SELECT></center></td>
<td colspan=2><br/>
<center>
	<input type="submit" title="execute DESCRIBE query" value="Describe" name="submit" style="width: 400px"/><br/>
</center><br/></td>
</tr>


<% if (request.getAttribute("error") != null) {%>
	<!-- Error Message -->
	<TR>
		<TD id="output">Result: </TD><TD id="output"><%=request.getAttribute("error") %></TD>
	</TR>
	<!-- Error Message -->	
<%}%>


</table></td></tr></table><br/><br/>

<% if (request.getAttribute("response") != null) {%>
	<!-- Response -->
	<PRE><%=request.getAttribute("response") %></PRE>
	<!-- Response -->
<%}%>
</form>
</body>
</html>