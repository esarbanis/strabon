<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
<jsp:useBean id="commonBean" class="eu.earthobservatory.org.StrabonEndpoint.Common" scope="application"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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

<FORM method=POST enctype="UTF-8" accept-charset="UTF-8" action="Store">
<INPUT type=hidden name="${commonBean.view}" value="${commonBean.viewType}"/>

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

<!-- Error Message -->
<c:if test="${error != null}">
	  		<TR><TD colspan=3>
	  		<CENTER><P style="color: red;">${error}</P></CENTER>
	  		</TD></TR>
</c:if> 
<!-- Error Message -->

<!-- Info Message -->
<c:if test="${info != null}">
  		<TR><TD colspan=3>
  		<CENTER><P>${info}</P></CENTER>
  		</TD></TR>
</c:if> 
<!-- Info Message -->

	<tr>
	<!--  direct input form -->
		<td id="output">Direct Input:</td>
		<td id="output">
			<textarea name="${commonBean.paramData}" rows="15" cols="100"></textarea></td>
		<td rowspan=4 id="output">
			<CENTER>RDF Format:<br/>
				<SELECT name="format">
				<c:forEach var="format" items="${commonBean.registeredFormats}">
					<OPTION value="${format}">${format}</OPTION>
				</c:forEach>
				</SELECT>
			</CENTER>
		</td>
	</tr>
	<tr>
		<td colspan=2 id="output"><br/>
		<CENTER>
			<input type="submit" value="Store Input" name="${commonBean.submitInput}" style="width: 400px"/>
		</CENTER><br/>
		</td>
	</tr>
	
	<tr>
		<td id="output" >URI Input:</td>
		<td id="output">
			<textarea name="${commonBean.paramDataURL}" rows="1" cols="100"></textarea>
		</td>
	</tr>
	
	<tr>
		<td colspan=2 id="output"><br/>
			<CENTER>
				<INPUT type="submit" value="Store from URI" name="${commonBean.submitURL}" style="width: 400px"/>
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