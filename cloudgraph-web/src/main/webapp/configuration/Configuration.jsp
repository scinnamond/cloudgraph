
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
</head>
<body>
<a4j:outputPanel id="body_panel">
  <%/* begin boilerplate header */%>  
  <div class="TopToolbarDiv">
      <jsp:include page="/TopToolbar.jsp" flush="false"/>
  </div>    
  <a4j:outputPanel id="left_nav_panel"
      rendered="#{ControlNavigationBean.datafiltersAction.selected}">
      <f:verbatim><div class="LeftNavDiv"></f:verbatim>
          <jsp:include page="/LeftTreeNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <a4j:outputPanel id="top_nav_Panel">
      <f:verbatim><div class="</f:verbatim><h:outputText value="#{ControlNavigationBean.datafiltersAction.selected ? 'TopNavDiv' : 'TopNavDivLeft'}"/><f:verbatim>"></f:verbatim>
      <jsp:include page="/TopNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <%/* end boilerplate header */%>
  
  <%@ include file="/ajaxloading.jsp" %>
  <a4j:outputPanel id="admin_content_panel">
  <f:verbatim><div class="</f:verbatim><h:outputText value="#{ControlNavigationBean.datafiltersAction.selected ? 'ContentDiv' : 'ContentDivLeft'}"/><f:verbatim>"></f:verbatim>
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 
      <h:panelGrid width="100%" columns="1" styleClass="DashboardTable"
          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
          cellpadding="0" cellspacing="0"> 
	          
		<rich:tabPanel switchType="ajax">

		<rich:tab title=""
		    rendered="true">
		    <f:facet name="label">
		        <h:panelGroup>
		            <h:outputText value="Attributes" />
		        </h:panelGroup>
		    </f:facet>
            <jsp:include page="/configuration/PropertyQueue.jsp" flush="false"/>
	    </rich:tab>
		<rich:tab title=""
		    rendered="true">
		    <f:facet name="label">
		        <h:panelGroup>
		            <h:outputText value="Value Lists" />
		        </h:panelGroup>
		    </f:facet>
            <jsp:include page="/configuration/EnumerationQueue.jsp" flush="false"/>
	     </rich:tab>
	          
         </rich:tabPanel>
	
      </h:panelGrid>
  </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>
 
       
  <jsp:include page="/common/SettingsModalPanel.jsp" flush="false"/>
  <jsp:include page="/configuration/PropertyDeleteConfirmModalPanel.jsp" flush="false"/>
</body>
</html>
</f:view>
