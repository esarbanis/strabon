<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.StoreBean"/>
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
<%@ include file="teleios-header.html"%>
<!-- include TELEIOS header and description -->

<FORM method="get" action=Store>
<INPUT type=hidden name="<%=StoreBean.SRC_REQ%>" value="browser"/>

<TABLE border="0" width="100%">
<TR> 
	<TD width="90" valign="top" class="style4"> 
		<TABLE border="0" cellspacing="0" cellpadding="0" width="165" id="navigation">
			<TR><TD width="90" class="style4"><a href="query.jsp" class="navText">Query</A></TD></TR> 
			<TR><TD width="90" class="style4"><a href="describe.jsp" class="navText">Describe</A></TD></TR>
		</TABLE>
	</TD>
	<td width="*" valign="top" class="style4">
		<TABLE cellspacing="5">
<%
	if (request.getParameter(StoreBean.DATA_ERROR) != null) {
  		%>
  		<TR><TD colspan=3>
  		<CENTER><P style="color: red;">No data provided!</P></CENTER>
  		</TD></TR>
  		<%
  	}
  		
  	if (request.getParameter(StoreBean.FORMAT_ERROR) != null) {
  		%>
  		<TR><TD colspan=3>
  		<CENTER><P style="color: red;">Unknown RDF Format!</P></CENTER>
  		</TD></TR>
  		<%
  	}
  	
  	if (request.getParameter(StoreBean.STORE_ERROR) != null) {
  		%>
  		<TR><TD colspan=3>
  		<CENTER><P style="color: red;">An error occurred while storing input data!</P></CENTER>
  		</TD></TR>
  		<%
  	}
  	
  	if (request.getParameter(StoreBean.STORE_OK) != null) {
  		%>
  		<TR><TD colspan=3>
  		<CENTER><P>Data stored successfully!</P></CENTER>
  		</TD></TR>
  		<%
  	}
%>
	<tr>
	<!--  direct input form -->
		<td id="output">Direct Input:</td>
		<td id="output">
			<textarea name="<%=StoreBean.PARAM_DATA%>" rows="15" cols="100"></textarea></td>
		<td rowspan=4 id="output">
			<CENTER>RDF Format:<br/>
				<SELECT name="<%=StoreBean.PARAM_FORMAT%>">
				<% for (String format : StoreBean.registeredFormats) {%>
					<OPTION value="<%=format%>"><%=format%></OPTION>
				<%}%>
				</SELECT>
			</CENTER>
		</td>
	</tr>
	<tr>
		<td colspan=2 id="output"><br/>
		<CENTER>
			<input type="submit" value="Store Input" name="<%=StoreBean.SUBMIT_INPUT%>" style="width: 400px"/>
		</CENTER><br/>
		</td>
	</tr>
	
	<tr>
		<td id="output" >URI Input:</td>
		<td id="output">
			<textarea name="<%=StoreBean.PARAM_DATA_URL%>" rows="1" cols="100"></textarea>
		</td>
	</tr>
	
	<tr>
		<td colspan=2 id="output"><br/>
			<CENTER>
				<INPUT type="submit" value="Store from URI" name="<%=StoreBean.SUBMIT_URL%>" style="width: 400px"/>
			</CENTER><br/>
		</td>
	</tr>
	
	</TABLE>
	</td>
</tr>
</TABLE>
</FORM>
<br/><br/><br/><br/><br/>
</BODY>
</HTML>