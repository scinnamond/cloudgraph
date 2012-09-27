
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/apls/css/apls.css" rel="stylesheet" type="text/css" />
</head>
<body>
<a4j:outputPanel id="body_panel">
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
  <a4j:outputPanel id="dashboard_content_panel">
  <f:verbatim><div class="</f:verbatim><h:outputText value="#{ControlNavigationBean.datafiltersAction.selected ? 'ContentDiv' : 'ContentDivLeft'}"/><f:verbatim>"></f:verbatim>
  <h:form id="project_list">                                                                                                                
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 
	     <h:panelGrid 
	          width="100%" columns="1" styleClass="DashboardTable"
	          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
	          cellpadding="0" cellspacing="0"> 
	          
	          
			  <h:panelGrid width="100%" columns="1"
			      rendered="false">
			      <rich:panel style="width: 100%; height: 80px;" >
			          <f:facet name="header">
			              <h:outputText value="Current Data Filter Criteria"/>
			          </f:facet>
			      
			      <f:subview id="crit_sv">	              
			          <f:subview id="prm_sv_0" rendered="#{SearchBean.activeParameterCount > 0}">
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[0].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[0].displayName}:"
						      action="#{SearchBean.activeParameters[0].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[0].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[0].index < SearchBean.activeParameterCount}" />                              
					  </f:subview>
			          <f:subview id="prm_sv_1" rendered="#{SearchBean.activeParameterCount > 1}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[1].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[1].displayName}:"
						      action="#{SearchBean.activeParameters[1].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[1].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[1].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
			          <f:subview id="prm_sv_2" rendered="#{SearchBean.activeParameterCount > 2}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[2].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[2].displayName}:"
						      action="#{SearchBean.activeParameters[2].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[2].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[2].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>				  
			          <f:subview id="prm_sv_3" rendered="#{SearchBean.activeParameterCount > 3}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[3].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[3].displayName}:"
						      action="#{SearchBean.activeParameters[3].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[3].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[3].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
			          <f:subview id="prm_sv_4" rendered="#{SearchBean.activeParameterCount > 4}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[4].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[4].displayName}:"
						      action="#{SearchBean.activeParameters[4].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[4].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[4].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
			          <f:subview id="prm_sv_5" rendered="#{SearchBean.activeParameterCount > 5}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[5].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[5].displayName}:"
						      action="#{SearchBean.activeParameters[5].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[5].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[5].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
			          <f:subview id="prm_sv_6" rendered="#{SearchBean.activeParameterCount > 6}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[6].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[6].displayName}:"
						      action="#{SearchBean.activeParameters[6].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[6].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[6].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
			          <f:subview id="prm_sv_7" rendered="#{SearchBean.activeParameterCount > 7}">                  
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="#{SearchBean.activeParameters[7].valueIconName}"/>                              
						  <a4j:commandLink 
						      value="#{SearchBean.activeParameters[7].displayName}:"
						      action="#{SearchBean.activeParameters[7].removeValue}"
						      actionListener="#{DashboardBean.clearDataAction}"	    
					          reRender="dashboard_content_panel,invst_panel,seg_panel,app_panel"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:outputText value="#{SearchBean.activeParameters[7].displayValue}"
			                  styleClass="GlobalSearchCritValueDiv"/>
					      <f:verbatim>&nbsp</f:verbatim>
			              <h:graphicImage value="/images/arrow_right.png" 
			                  rendered="#{SearchBean.activeParameters[7].index < SearchBean.activeParameterCount}" />                              
				      </f:subview>
				      
			     </f:subview>
			     </rich:panel>
			 </h:panelGrid>

          <jsp:include page="/data/DataQueue.jsp" flush="false"/>


      </h:panelGrid>
  </h:panelGrid>
  </h:form>     
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>
  <jsp:include page="/data/PersonalizeQueuePanel.jsp" flush="false"/>
</body>
</html>
</f:view>
