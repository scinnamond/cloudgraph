
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
  <div class="TopToolbarDiv">
      <ui:include src="/TopToolbar.xhtml" />
  </div>    
  <p:outputPanel id="top_nav_Panel">
      <f:verbatim><div class="TopNavDiv"></f:verbatim>
      <ui:include src="/TopNav.xhtml" />
      <f:verbatim></div></f:verbatim>
  </p:outputPanel>
  <%/* end boilerplate header */%> 

  <div class="ContentDiv">
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
