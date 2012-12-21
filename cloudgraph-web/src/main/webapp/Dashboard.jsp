<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j" %>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@taglib uri="/WEB-INF/chartcreator.tld" prefix="c"%>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<f:view>
<html>
<head>
    <title><h:outputText value="#{bundle.aplsDashboard_title}"/></title>
    <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
    	
	<link rel="stylesheet" type="text/css" href="/cloudgraph-web/css/jqcontextmenu.css" />
	
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>	
	<script type="text/javascript" src="/apls/js/jqcontextmenu.js"></script>
	
	<script type="text/javascript">	
	    jQuery(document).ready(function($){
		    jQuery.fn.initcontextmenu('contextmenu1')
	    })
	</script>
	<script type="text/javascript">	
	    function handleChartOnClick(menuid, e, params) {
	        jQuery.fn.showcontextmenu(menuid, e, params);
	        e.cancelBubble = true; // stops outer handlers which close the menu (set up on init)
	    }
	</script>
	
	<script type="text/javascript">
	    function changePointer(onOff) {
	        if (onOff == true) {
	            document.body.style.cursor = "wait";
	        }
	        else {
	            document.body.style.cursor = "default";
	        }
	    }
	</script>	
</head>
<body styleleftmargin="0" topmargin="0" marginwidth="0" marginheight="0">

<a4j:outputPanel id="body_panel">
  <rich:dragIndicator id="indicator"/>

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

<!--HTML for Context Menu -->
<ul id="contextmenu1" class="jqcontextmenu">
<li><a href="Dashboard.faces?drilldown=true">Drill-Down</a></li>
<li><a href="Dashboard.faces?drillup=true">Roll-Up</a></li>
<li><a href="ProjectQueue.faces">View Data</a></li>
<li><a href="#">Annotate</a>
	<ul>
	    <li><a href="Dashboard.faces">Add Meeting Notes</a></li>
	    <li><a onclick="Richfaces.showModalPanel('actionItemsModalPanel');" href="#">Add Action Items</a></li>
	    <li><a href="Dashboard.faces">Add Key Words</a></li>
	    <li><a href="Dashboard.faces">Add Uploads</a></li>
	</ul>
</li>
<li><a href="#">Collaborate</a>
	<ul>
	    <li><a href="#">Wiki</a></li>
	    <li><a href="#">Twitter</a></li>
	    <li><a href="#">Blog</a></li>
	    <li><a href="#">News</a></li>
	</ul>
</li>
<li><a href="#">Other</a>
</ul>
    

             
  <%@ include file="/ajaxloading.jsp" %>
  <a4j:outputPanel id="dashboard_content_panel">
  <f:verbatim><div class="</f:verbatim><h:outputText value="#{ControlNavigationBean.datafiltersAction.selected ? 'ContentDiv' : 'ContentDivLeft'}"/><f:verbatim>"></f:verbatim>
     <h:form id="dashboard_content_form">   
     
 
	     <h:panelGrid id="dashboard_content_panel" width="100%" columns="1" styleClass="DashboardTable"
	          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
	          cellpadding="0" cellspacing="0"> 
	          
	          
			  <h:panelGrid width="100%" columns="1"
			      rendered="#{SearchBean.activeParameterCount > 0}">
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


<%/*
            <h:panelGrid id="widgetDropZone" width="100%" columns="5"
                styleClass="DropZone">
              <f:facet name="header">
                <h:outputText value="Widget Drop Zone"/>
              </f:facet>
              <h:panelGrid id="widgetDropZoneCharts" width="100%" columns="1"
                  styleClass="DropZone">
                <f:facet name="header">
                  <h:outputText value="Chart Drops"/>
                </f:facet>
                <rich:dropSupport id="chartWidgetDropZone"
                                  acceptedTypes="CHART_WIDGET"
                                  dropListener="#{DashboardBean.processDrop}" 
                                  reRender="widgetDropZoneCharts">
                </rich:dropSupport>
                <h:outputText value="#{DashboardBean.chartDropCount}"/>
              </h:panelGrid>
              <h:panelGrid id="widgetDropZoneTables" width="100%" columns="1"
                  styleClass="DropZone">
                <f:facet name="header">
                  <h:outputText value="Table Drops"/>
                </f:facet>
                <rich:dropSupport id="tableWidgetDropZone"
                                  acceptedTypes="TABLE_WIDGET"
                                  dropListener="#{DashboardBean.processDrop}" 
                                  reRender="widgetDropZoneTables">
                </rich:dropSupport>
                <h:outputText value="#{DashboardBean.tableDropCount}"/>
              </h:panelGrid>
              <h:panelGrid id="widgetDropZoneAlerts" width="100%" columns="1"
                  styleClass="DropZone">
                <f:facet name="header">
                  <h:outputText value="Alert Drops"/>
                </f:facet>
                <rich:dropSupport id="alertWidgetDropZone"
                                  acceptedTypes="ALERT_WIDGET"
                                  dropListener="#{DashboardBean.processDrop}" 
                                  reRender="widgetDropZoneAlerts">
                </rich:dropSupport>
                <h:outputText value="#{DashboardBean.alertDropCount}"/>
              </h:panelGrid>
              <h:panelGrid id="widgetDropZoneEvents" width="100%" columns="1"
                  styleClass="DropZone">
                <f:facet name="header">
                  <h:outputText value="Event Drops"/>
                </f:facet>
                <rich:dropSupport id="eventWidgetDropZone"
                                  acceptedTypes="EVENT_WIDGET"
                                  dropListener="#{DashboardBean.processDrop}" 
                                  reRender="widgetDropZoneEvents">
                </rich:dropSupport>
                <h:outputText value="#{DashboardBean.eventDropCount}"/>
              </h:panelGrid>
              <h:panelGrid id="widgetDropZoneLayouts" width="100%" columns="1"
                  styleClass="DropZone">
                <f:facet name="header">
                  <h:outputText value="Layout Drops"/>
                </f:facet>
                <rich:dropSupport id="layoutWidgetDropZone"
                                  acceptedTypes="LAYOUT_WIDGET"
                                  dropListener="#{DashboardBean.processDrop}" 
                                  reRender="widgetDropZoneLayouts">
                </rich:dropSupport>
                <h:outputText value="#{DashboardBean.layoutDropCount}"/>
              </h:panelGrid>
              <f:facet name="footer">
                <a4j:commandButton value="Reset" 
                                   action="#{DashboardBean.resetWidgetDropZone}"
                                   reRender="widgetDropZone"/>
              </f:facet>
            </h:panelGrid>
*/%>

	
		     <h:panelGrid id="focus_dashboard_panel" width="100%" columns="1" 
		          rowClasses="DashboardTable" columnClasses="DashboardTable"
		          rendered="#{DashboardBean.layout.expandedComponentsContainer.hasComponents}"> 
		          
		         <h:panelGrid columns="1" width="100%" 
		              rowClasses="DashboardTable" columnClasses="DashboardTable"> 
		              <h:dataTable value="#{DashboardBean.layout.expandedComponentsContainer.components}"
		                  width="100%" 
		                  var="component">                                                                                            
		                  <h:column>
                          <a4j:outputPanel layout="block">
                            <rich:dropSupport acceptedTypes="DENY_ALL"/>
		                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
		                    </a4j:outputPanel>
						  </h:column>
				      </h:dataTable>
				      <f:verbatim>&nbsp</f:verbatim>		      
		         </h:panelGrid>
	         </h:panelGrid>
	
	
		     <h:panelGrid id="full_dashboard_panel" styleClass="DashboardTable"
		         width="100%" columns="1" 
		         cellpadding="0" cellspacing="0"
		         rowClasses="DashboardTable" columnClasses="DashboardTable"
		         rendered="#{!DashboardBean.layout.expandedComponentsContainer.hasComponents}"> 
		         <h:panelGrid styleClass="DashboardTable" columns="1" width="100%" 
		              cellpadding="0" cellspacing="0"
		              rowClasses="DashboardTable" columnClasses="DashboardTable"
		              headerClass="DashboardTable" footerClass="DashboardTable"> 
		              <h:dataTable value="#{DashboardBean.layout.headerContainer.components}" 
		                  var="component"
		                  rowClasses="DashboardTable" columnClasses="DashboardTable"
		                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
		                  <h:column>
                          <a4j:outputPanel layout="block">
                            <rich:dropSupport acceptedTypes="DENY_ALL"/>
		                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
		                    </a4j:outputPanel>
						  </h:column>
				      </h:dataTable>
		         </h:panelGrid>
	
			     <h:panelGrid width="100%" columns="2" styleClass="DashboardTable"
			         rendered="#{DashboardBean.layout.columnContainerCount == 2}" 
			         cellpadding="0" cellspacing="0"
			         rowClasses="DashboardTable" columnClasses="DashboardTable"
			         headerClass="DashboardTable" footerClass="DashboardTable"> 
	
			         <h:panelGrid columns="1" width="#{DashboardBean.layout.columnContainers[0].width}px" styleClass="DashboardTable"
			              cellpadding="0" cellspacing="0"
			              rowClasses="DashboardTable" columnClasses="DashboardTable"
			              headerClass="DashboardTable" footerClass="DashboardTable"> 
			              <h:dataTable value="#{DashboardBean.layout.columnContainers[0].components}" 
			                  var="component"
			                  rowClasses="DashboardTable" columnClasses="DashboardTable"
			                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
			                  <h:column>
                             <a4j:outputPanel layout="block">
                               <rich:dropSupport acceptedTypes="DENY_ALL"/>
			                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
			                    </a4j:outputPanel>
							  </h:column>
					      </h:dataTable>
			         </h:panelGrid>			      
			         <h:panelGrid columns="1" width="#{DashboardBean.layout.columnContainers[1].width}px" styleClass="DashboardTable"
			              cellpadding="0" cellspacing="0"
			              rowClasses="DashboardTable" columnClasses="DashboardTable"
			              headerClass="DashboardTable" footerClass="DashboardTable"> 
			              <h:dataTable value="#{DashboardBean.layout.columnContainers[1].components}" 
			                  var="component"
			                  rowClasses="DashboardTable" columnClasses="DashboardTable"
			                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
			                  <h:column>
                             <a4j:outputPanel layout="block">
                               <rich:dropSupport acceptedTypes="DENY_ALL"/>
			                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
			                    </a4j:outputPanel>
							  </h:column>
					      </h:dataTable>
			         </h:panelGrid>			      
			     </h:panelGrid>
	
			     <h:panelGrid width="100%" columns="3" styleClass="DashboardTable"
			         rendered="#{DashboardBean.layout.columnContainerCount == 3}" 
			         cellpadding="0" cellspacing="0"
			         rowClasses="DashboardTable" columnClasses="DashboardTable"
			         headerClass="DashboardTable" footerClass="DashboardTable"> 
	
			         <h:panelGrid columns="1" width="#{DashboardBean.layout.columnContainers[0].width}px" styleClass="DashboardTable"
			              cellpadding="0" cellspacing="0"
			              rowClasses="DashboardTable" columnClasses="DashboardTable"
			              headerClass="DashboardTable" footerClass="DashboardTable"> 
			              <h:dataTable value="#{DashboardBean.layout.columnContainers[0].components}" 
			                  var="component"
			                  rowClasses="DashboardTable" columnClasses="DashboardTable"
			                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
			                  <h:column>
                             <a4j:outputPanel layout="block">
                               <rich:dropSupport acceptedTypes="DENY_ALL"/>
			                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
			                    </a4j:outputPanel>
							  </h:column>
					      </h:dataTable>
			         </h:panelGrid>			      
			         <h:panelGrid columns="1" width="#{DashboardBean.layout.columnContainers[1].width}px" styleClass="DashboardTable"
			              cellpadding="0" cellspacing="0"
			              rowClasses="DashboardTable" columnClasses="DashboardTable"
			              headerClass="DashboardTable" footerClass="DashboardTable"> 
			              <h:dataTable value="#{DashboardBean.layout.columnContainers[1].components}" 
			                  var="component"
			                  rowClasses="DashboardTable" columnClasses="DashboardTable"
			                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
			                  <h:column>
                             <a4j:outputPanel layout="block">
                               <rich:dropSupport acceptedTypes="DENY_ALL"/>
			                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
			                    </a4j:outputPanel>
							  </h:column>
					      </h:dataTable>
			         </h:panelGrid>			      
			         <h:panelGrid columns="1" width="#{DashboardBean.layout.columnContainers[2].width}px" styleClass="DashboardTable"
			              cellpadding="0" cellspacing="0"
			              rowClasses="DashboardTable" columnClasses="DashboardTable"
			              headerClass="DashboardTable" footerClass="DashboardTable"> 
			              <h:dataTable value="#{DashboardBean.layout.columnContainers[2].components}" 
			                  var="component"
			                  rowClasses="DashboardTable" columnClasses="DashboardTable"
			                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
			                  <h:column>
                             <a4j:outputPanel layout="block">
                               <rich:dropSupport acceptedTypes="DENY_ALL"/>
			                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
			                    </a4j:outputPanel>
							  </h:column>
					      </h:dataTable>
			         </h:panelGrid>			      
			     </h:panelGrid>
			                                                                                               
		         <h:panelGrid columns="1" width="100%" styleClass="DashboardTable"
		               cellpadding="0" cellspacing="0"
		              rowClasses="DashboardTable" columnClasses="DashboardTable"
		              headerClass="DashboardTable" footerClass="DashboardTable"> 
		              <h:dataTable value="#{DashboardBean.layout.footerContainer.components}" 
		                  var="component"
		                  rowClasses="DashboardTable" columnClasses="DashboardTable"
		                  headerClass="DashboardTable" footerClass="DashboardTable">                                                                                            
		                  <h:column>
                          <a4j:outputPanel layout="block">
                            <rich:dropSupport acceptedTypes="DENY_ALL"/>
		                      <jsp:include page="/DashboardComponent.jsp" flush="false"/>
		                    </a4j:outputPanel>
						  </h:column>
				      </h:dataTable>
		         </h:panelGrid>
			 </h:panelGrid>                                                                                         
		  </h:panelGrid>                                                                                         

 
 
 
 
 
 
 
      </h:form>                                                                                                                        
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>  
  
    <rich:modalPanel id="settingsModalPanel"
        autosized="true" resizeable="false">
        <f:facet name="header">
            <h:panelGroup>
                <h:outputText value="Edit Settings"></h:outputText>
            </h:panelGroup>
        </f:facet>
        <%/* 
        <f:facet name="controls">
            <h:graphicImage value="/apls/images/close.png" style="cursor:pointer" onclick="Richfaces.hideModalPanel('settingsModalPanel')" />
        </f:facet> 
        */%>         
        <a4j:form  id="modalPanel1_form" 
            ajaxSubmit="true" 
            reRender="dashboard_content_panel">
            <h:panelGrid columns="1" width="95%" 
                cellpadding="2" cellspacing="2">  
                    <h:panelGrid columns="2" width="80%" 
                        columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn">
                        <h:outputText value="Username:" />
                        <h:outputText value="#{UserBean.name}" />
                        <f:verbatim>&nbsp</f:verbatim>
                        <f:verbatim>&nbsp</f:verbatim>
                        <h:outputText value="First Name:" />
                        <h:outputText value="#{UserBean.person.firstName}" />
                        <h:outputText value="Last Name:" />
                        <h:outputText value="#{UserBean.person.lastName}" />
                        <f:verbatim>&nbsp</f:verbatim>
                        <f:verbatim>&nbsp</f:verbatim>
                        
                        <h:outputText value="Chart Type:"/>
                        <h:selectOneMenu id="chartType" required="false" 
                            value="#{UserBean.chartType}" 
                            disabled="false"
                            title="Chart Type">
                            <f:selectItems value="#{UserBean.chartTypeItems}"/>
                        </h:selectOneMenu>
                    </h:panelGrid>          
                <f:verbatim>&nbsp</f:verbatim>
                <f:verbatim>&nbsp</f:verbatim>
                <a4j:commandButton 
                    action="#{UserBean.commitProfile}"
                    reRender="dashboard_content_panel"
                    onclick="Richfaces.hideModalPanel('settingsModalPanel');" value="  OK  ">
                </a4j:commandButton>
        </h:panelGrid>
        </a4j:form>
    </rich:modalPanel>
  
</body>
</html>
</f:view>
