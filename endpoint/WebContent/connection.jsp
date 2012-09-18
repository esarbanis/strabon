<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.Common"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
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
		<title>TELEIOS: Strabon Endpoint</title>
	</head>
<body topmargin="0" leftmargin="0" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF">

<!-- include TELEIOS header and description -->
<jsp:include page="teleios-header.html"/>
<!-- include TELEIOS header and description -->

<FORM method=POST enctype="UTF-8" accept-charset="UTF-8" action="DBConnect">
<input type="hidden" name="query" value="<%=request.getAttribute("query")%>"/>
<input type="hidden" name="handle" value="<%=request.getAttribute("handle")%>"/>
<input type="hidden" name="format" value="<%=request.getAttribute("format")%>"/>
<TABLE class="style4">
	<TR> 
		<TD valign="top" class="style4">Database Name:</TD>
		<TD><input type="text" name="dbname" value="<%=request.getAttribute("dbname")%>"/></TD>
	</TR>
	<TR> 
		<TD valign="top" class="style4">Username:</TD>
		<TD><input type="text" name="username" value="<%=request.getAttribute("username")%>"/></TD>
	</TR>
		<TR> 
		<TD valign="top" class="style4">Password:</TD>
		<TD><input type="password" name="password" value="<%=request.getAttribute("password")%>"/></TD>
	</TR>
	<TR> 
		<TD valign="top" class="style4">Port:</TD>
		<TD><input type="text" name="port" value="<%=request.getAttribute("port")%>"/></TD>
	</TR>
	<TR> 
		<TD valign="top" class="style4">Hostname:</TD>
		<TD><input type="text" name="hostname" value="<%=request.getAttribute("hostname")%>"/></TD>
	</TR>
	<TR> 
		<TD valign="top" class="style4">Database Backend:</TD>
		<TD>
			<SELECT name="dbengine">
				<OPTION value="<%=Common.DBBACKEND_POSTGIS%>" <%=Common.DBBACKEND_POSTGIS.equals(request.getAttribute("dbengine")) ? "selected":""%>><%=Common.DBBACKEND_POSTGIS%></OPTION>
				<OPTION value="<%=Common.DBBACKEND_MONETDB%>" <%=Common.DBBACKEND_MONETDB.equals(request.getAttribute("dbengine")) ? "selected":""%>><%=Common.DBBACKEND_MONETDB%></OPTION>
			</SELECT>
		</TD>
	</TR>
	<TR>
		<TD colspan=2><input type="submit" value="Connect"/></TD>
	</TR>
</TABLE>
</FORM>
<br/><br/><br/><br/><br/>
</BODY>
</HTML>