<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%
response.sendRedirect("documentation/Documentation.faces");
%>  
<f:view>
<html>
<head>
<h:outputText value="#{UserBean.principalName}"/>
</head>
<body>
</body>
</html>
</f:view>
