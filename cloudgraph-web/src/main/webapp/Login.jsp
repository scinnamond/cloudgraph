<!doctype html public "-//w3c//dtd html 4.0 transitional//en">

<%@ page import="java.net.InetAddress"%>
<%@ page import="java.io.PrintWriter"%>


<%	
    //only allow access to backdoor login page from localhost
    //if (!request.getRemoteHost().equalsIgnoreCase("localhost") &&
	//	!request.getRemoteAddr().equals("127.0.0.1") &&
	//	!InetAddress.getLocalHost().equals(request.getRemoteAddr()) &&
	//	!InetAddress.getLocalHost().equals(request.getRemoteHost())) 
	//{   
	//	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	//	response.setContentType("text/html");
	//	//PrintWriter out = response.getWriter();
	//	out.print("<html><body>Forbidden</body></html>\r\n");
	//	out.flush();
	//	out.close();
	//	return;
	//}
%>

<html>
<head>
<meta http-equiv="expires" content="now">
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
</head>
<body style="background-color: #FFFFFF;">
<table border="0" cellspacing="18" cellpadding="0">
	<tr>
		<td valign="top">
		<blockquote>
		<h4></h4>
		<p />
		<form id="login_form" method="POST" action="j_security_check" autocomplete="off">
		<table border="0">
			<tr>
				<td><label class="label" for="j_username">Username:</label></td>
				<td><input id="j_username" name="j_username" type="text" /></td>
			</tr>
			<tr>
				<td><label class="label" for="j_password">Password:</label></td>
				<td><input id="j_password" name="j_password" type="password" /></td>
			</tr>
			<tr>
				<td colspan="2" align="right"><input type="submit"
					value="Submit"></td>
			</tr>
		</table>
		</form>
		</blockquote>
		</td>
	</tr>
</table>
</body>
</html>
