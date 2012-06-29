<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.StoreBean"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="style.css" type="text/css" /> 
<title>TELEIOS: Strabon Endpoint</title>
</head>
<body topmargin="0" leftmargin="0" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF">
  <TABLE width="100%" BORDER=0 CELLPADDING=0 CELLSPACING=0>
    <TR>
		
      <TD height="16" background="images/pixi_white.gif"><img src="images/pixi_white.gif" width="16" height="16"></TD>
	</TR>
	<TR>
      <TD height="1" background="images/top_bg_blue.gif"></TD>
	</TR>		
	<TR>
      <TD height="60" background="images/nav2_bg.gif"> 
        <table width="100%" border="0">
         <tr>
            <td width="1"><img src="images/nav2_bg.gif" width="1" height="60"></td>
            <td valign="top" width="80px"><img border="0" src="images/teleios_logo.png"/></td>
            <td valign="top" align="left">
            <span class="logo">Strabon Endpoint</span><br><span class="style4">based on Strabon</span></td>
          </tr>
        </table> </TD>
	</TR>
	<TR>
      <TD height="21" background="images/nav1_bg1.gif">
      </TD>
	</TR>
	<TR>
      <TD height="2" background="images/top_bg_blue.gif"></TD>
	</TR>
</TABLE>

<FORM method="get" action=Store>
<INPUT type=hidden name="<%=StoreBean.SRC_REQ%>" value="browser"/>

<TABLE border="0" width="100%">
<tr> 
	<td width="90" valign="top" bgcolor="#dfe8f0"> 
		<TABLE border="0" cellspacing="0" cellpadding="0" width="165" id="navigation">
			<tr><td width="90" class="style4"><a href="Query" class="navText">Query</a></td></tr> 
			<tr><td width="90" class="style4"><a href="javascript:history.go(0)" class="navText">Clear</a></td></tr> 
		</TABLE>
	</td>
	<td width="*" valign="top" >
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