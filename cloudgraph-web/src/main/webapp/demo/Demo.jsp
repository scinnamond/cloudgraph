
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
  <title><h:outputText value="#{bundle.aplsWindowTitle}"/></title>
  <script type="text/javascript">                                                          
    function theFrameLoaded(theFrame) {  
    	theFrame.style.height ="";
        theFrame.style.height = theFrame.contentWindow.document.body.scrollHeight + 'px';
        theFrame.style.width = "";
        theFrame.style.width = theFrame.contentWindow.document.body.scrollWidth + 'px';
    }                                                                                      
  </script>                                                                                
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

    <h:panelGrid columns="2" styleClass="AlignLeft">
        <h:graphicImage value="/images/caption_live_demo.png"/>
        <h:outputText value=" - #{DemoBean.modelDisplayName}" 
            style="color: 222222; font-family: sans-serif; font-style: italic; font-size:16px;"/>
    </h:panelGrid>
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

    <rich:tab id="tab_model" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Model" />
            </h:panelGroup>
        </f:facet>
        <f:subview id="model_sv">
        <f:verbatim>        
        <iframe width="600" height="800" type="text/html"
          id="modelFrame" onload="theFrameLoaded(this)"
          frameborder="0" scrolling="no"
          src="</f:verbatim><h:outputText value="#{DemoBean.modelUrl}"/><f:verbatim>"/>
        </iframe>
        </f:verbatim>
        </f:subview>
    </rich:tab>
    <rich:tab id="tab_javaDocs" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="JavaDocs" />
            </h:panelGroup>
        </f:facet>
        <f:subview id="jd_sv">
        <f:verbatim>        
        <iframe type="text/html"
          id="javadocFrame" onload="theFrameLoaded(this)"
          frameborder="0" width="600" height="800" scrolling="no"
          src="</f:verbatim><h:outputText value="#{DemoBean.javaDocUrl}"/><f:verbatim>"/>
        </iframe>
        </f:verbatim>
        </f:subview>    
    </rich:tab>
    <rich:tab id="tab_dataGraphs" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Data Graphs" />
            </h:panelGroup>
        </f:facet>
        <rich:tree id="graphTree" 
            rendered="#{DemoBean.selectedTab == 'tab_dataGraphs'}"
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
    <rich:tab id="tab_hbase" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="HBase (raw data)" />
            </h:panelGroup>
        </f:facet>
        <rich:tabPanel switchType="ajax" selectedTab="#{DemoBean.selectedTable}">  
            <c:forEach var="tableInfo" items="#{DemoBean.tables}">
            <rich:tab name="#{tableInfo.config.table.name}" title="HBase raw data for table #{tableInfo.config.table.name}">
                <f:facet name="label">
                    <h:panelGroup>
                        <h:outputText value="#{tableInfo.config.table.name}" />
                    </h:panelGroup>
                </f:facet> 
		        <h:panelGrid rowClasses="AlignCenter" columns="1" border="0">                                                            
		            <rich:dataTable id="hbase_dtbl_data" 
		                var="item" 
		                value="#{HBaseQueueBean.data}">    		                                                                                     
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
		            <rich:datascroller  
		                  align="center"
		                  for="hbase_dtbl_data"
		                  maxPages="20"
		                  page="#{HBaseQueueBean.scrollerPage}"
		                  reRender="dashboard_content_panel"/>
		                                                                                                                                                    
		        </h:panelGrid>                 

            </rich:tab>
            </c:forEach>
        </rich:tabPanel>
      </rich:tab>
    <rich:tab id="tab_cassandra" title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Cassandra (raw data)" />
            </h:panelGroup>
        </f:facet>
    </rich:tab>
    <rich:tab id="tab_codeSamples" title=""
        rendered="true" >
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Code Sample" />
            </h:panelGroup>
        </f:facet>
        <f:subview id="sample_sv">    
        <f:verbatim>
        <iframe width="600" height="800" type="text/plain"
          onload="theFrameLoaded(this)"
          frameborder="0" scrolling="no"
          src="</f:verbatim><h:outputText value="#{DemoBean.createCodeSamplesURL}"/><f:verbatim>"/>
        </iframe>
        </f:verbatim>
        </f:subview>
    </rich:tab>
    
    <rich:tab id="tab_config"  title=""
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Configuration" />
            </h:panelGroup>
        </f:facet>
        <rich:tabPanel switchType="server">  
            <c:forEach var="tableInfo" items="#{DemoBean.tables}">
            <rich:tab title="The configuration XML for table #{tableInfo.config.table.name}">
                <f:facet name="label">
                    <h:panelGroup>
                        <h:outputText value="#{tableInfo.config.table.name}" />
                    </h:panelGroup>
                </f:facet> 
                <f:verbatim><pre></f:verbatim>
                <h:outputText value="#{tableInfo.xml}" />
                <f:verbatim></pre></f:verbatim>
            </rich:tab>
            </c:forEach>
        </rich:tabPanel>
    </rich:tab>
    </rich:tabPanel>
  </h:panelGrid>
  </h:form>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>

       
  <jsp:include page="/common/SettingsModalPanel.jsp" flush="false"/>
  <jsp:include page="/common/LoginModalPanel.jsp" flush="false"/>
</body>
</html>
</f:view>