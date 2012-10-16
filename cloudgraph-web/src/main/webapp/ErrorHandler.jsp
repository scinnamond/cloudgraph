
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
</head>
<body>
  <%/* begin boilerplate header */%>  
  <div class="AppTitleDiv">
      <h:panelGrid columns="2" border="0"> 
      <h:graphicImage value="/images/2009-logo-usfs.gif" style="border:0"/>
      <h:graphicImage value="/images/app_title_mirror.png" style="border:0"/>
      </h:panelGrid>
  </div>
  <div class="TopToolbarDiv">
      <jsp:include page="/TopToolbar.jsp" flush="false"/>
  </div>    
  <a4j:outputPanel id="top_nav_Panel">
      <f:verbatim><div class="TopNavDivLeft"></f:verbatim>
      <jsp:include page="/TopNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <%/* end boilerplate header */%> 

  <div class="ContentDivLeft">
    <h:panelGrid columns="1" headerClass="AlignLeft" rowClasses="AlignCenter" width="100%" border="0">
      <f:verbatim>&nbsp</f:verbatim>
      <f:verbatim>&nbsp</f:verbatim>
      <f:verbatim>&nbsp</f:verbatim>
      <f:verbatim>&nbsp</f:verbatim>
      <h:outputText styleClass="labelBold" value="#{ErrorHandlerBean.errorType}"/>
      <f:verbatim>&nbsp</f:verbatim>
      <h:outputText styleClass="labelBold" value="#{ErrorHandlerBean.errorMessage}"/>
      <f:verbatim>&nbsp</f:verbatim>
      <h:outputText value="#{ErrorHandlerBean.errorHelp}"/>
      <f:verbatim>&nbsp</f:verbatim>
    </h:panelGrid>    
  
  </div>       
</body>
</html>
</f:view>
