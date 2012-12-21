
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
  <a4j:outputPanel id="left_nav_panel">
      <f:verbatim><div class="LeftNavDiv"></f:verbatim>
          <jsp:include page="/LeftTreeNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <a4j:outputPanel id="top_nav_Panel">
      <f:verbatim><div class="TopNavDiv"></f:verbatim>
      <jsp:include page="/TopNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <%/* end boilerplate header */%> 


  <%@ include file="/ajaxloading.jsp" %>
  <a4j:outputPanel id="demo_content_panel">
  <f:verbatim><div class="ContentDiv"></f:verbatim>
  <h:form id="demo_form">                                                                                                                
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 

    <h:panelGrid width="100%" columns="1" 
        styleClass="DashboardTable"
        rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
        cellpadding="0" cellspacing="0"
        rendered="#{!DemoBean.hasModel}"> 
     
        <a4j:include viewId="#{DemoBean.defaultUrl}" />
  
    </h:panelGrid>

    <rich:tabPanel switchType="ajax" 
        rendered="#{DemoBean.hasModel}"
        selectedTab="#{DemoBean.selectedTab}">

    <rich:tab id="model" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Model" />
            </h:panelGroup>
        </f:facet>
        <f:subview id="model_sv">
        <f:verbatim>        
        <iframe type="text/html"
          frameborder="0" width="700" height="12000" scrolling="no"
          src="</f:verbatim><h:outputText value="#{DemoBean.modelUrl}"/><f:verbatim>"/>
          <p>content not found</p>
        </iframe>
        </f:verbatim>
        </f:subview>
    </rich:tab>
    <rich:tab id="javaDocs" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="JavaDocs" />
            </h:panelGroup>
        </f:facet>
        <f:subview id="jd_sv">
        <f:verbatim>        
        <iframe type="text/html"
          frameborder="0" width="700" height="900" scrolling="no"
          src="</f:verbatim><h:outputText value="#{DemoBean.javaDocUrl}"/><f:verbatim>"/>
          <p>content not found</p>
        </iframe>
        </f:verbatim>
        </f:subview>    
    </rich:tab>
    <rich:tab id="dataGraphs" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Data Graphs" />
            </h:panelGroup>
        </f:facet>
        <rich:tree id="graphTree" 
            rendered="#{DemoBean.selectedTab == 'dataGraphs'}"
            componentState="#{GraphEditBean.graphTree.treeState}"
            switchType="ajax"
            value="#{GraphEditBean.graphTree.model}" 
            var="item" nodeFace="#{item.type}"
            nodeSelectListener="#{GraphEditBean.dataObjectSelectListener}">                       
            <rich:treeNode id="graphNodeAny"
                type="level_any"
                iconLeaf="/images/orangedotleaf.gif" 
                icon="/images/yellow-folder-open.png"
                changeExpandListener="#{GraphEditBean.graphTree.processExpansion}">
                <h:outputText value="#{item.label}"
                    title="#{item.tooltip}"/>
            </rich:treeNode>
        </rich:tree>
    </rich:tab>
    <rich:tab id="hbase" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="HBase" />
            </h:panelGroup>
        </f:facet>
		<h:panelGrid id="hbase_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
		    <rich:dataTable id="hbase_dtbl" 
		        var="item" 
		        value="#{HBaseQueueBean.data}">    
		                                                                             
              <f:facet name="header">
                  <h:outputText value="#{DemoBean.modelDisplayName}" 
                      title="#{DemoBean.modelDescription}"/>
              </f:facet>   
		                                                                                                                                                                 
		      <rich:columns value="#{HBaseQueueBean.columns}" 
		          var="col" index="ind" 
		          sortBy="#{item.data[ind]}" sortOrder="#{col.queueColumnSortOrder}">        
		          <f:facet name="header">
		              <h:outputText value="#{col.displayName}" 
		                  title="#{col.description}"/>
		          </f:facet>   
		          <h:outputText value="#{item.data[ind]} " />
		      </rich:columns>
		       
		    </rich:dataTable>                                                                                                                          
		    <rich:datascroller id="hbase_dtbl_data_scrlr"
		          align="center"
		          for="hbase_dtbl"
		          maxPages="20"
		          page="#{HBaseQueueBean.scrollerPage}"
		          reRender="dashboard_content_panel"/>
		                                                                                                                                            
		</h:panelGrid> 
      </rich:tab>
    <rich:tab id="cassandra" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Cassandra" />
            </h:panelGroup>
        </f:facet>
    </rich:tab>
    <rich:tab id="codeSamples" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Code Samples" />
            </h:panelGroup>
        </f:facet>
    </rich:tab>
    <rich:tab id="config"  title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Configuration" />
            </h:panelGroup>
        </f:facet>
    </rich:tab>
    </rich:tabPanel>
  </h:panelGrid>
  </h:form>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>

       
</body>
</html>
</f:view>