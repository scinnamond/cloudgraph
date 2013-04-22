
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

    <h:panelGrid width="100%" columns="1" 
        styleClass="DashboardTable"
        rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
        cellpadding="0" cellspacing="0"
        rendered="#{!DemoBean.hasModel}"> 
        <h:graphicImage value="/images/caption_live_demo.png"/>
     
        <f:verbatim>
Welcome to a live demo of CloudGraph™. Select a source (UML) model from navigation menu on the left, and several tabs will become available including set of live data graphs, rendered and expanded as a tree. These are assembled on demand using the CloudGraph™ HBase Service. The various available models span several domains such as Healthcare, Life Sciences and others.  
        </f:verbatim>
        
        <rich:spacer height="40" />          
        <rich:separator height="4" lineType="double"/>          
        <h:outputText style="font-size: 120%; font-weight: normal" 
              value="Select a UML model to the left"/>    
        <h:graphicImage value="/images/big_left_arrow.png"/>
        
    </h:panelGrid>

    <h:panelGrid columns="2" styleClass="AlignLeft"
        rendered="#{DemoBean.hasModel}">
        <h:graphicImage value="/images/caption_live_demo.png"/>
        <h:outputText value=" - #{DemoBean.modelDisplayName}" 
            style="color: 222222; font-family: sans-serif; font-style: italic; font-size:16px;"/>
    </h:panelGrid>
    <rich:tabPanel switchType="ajax" 
        rendered="#{DemoBean.hasModel}"
        selectedTab="#{DemoBean.selectedTab}">

    <rich:tab id="tab_model" 
        title="A default document generated from the UML model for #{DemoBean.modelDisplayName}"
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="UML Model"/>
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
    <rich:tab id="tab_javaDocs" 
        title="The javadocks for SDO classes generated for UML model #{DemoBean.modelDisplayName}"
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
    <rich:tab id="tab_dataGraphs" 
        
        rendered="true">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="HBase Data Graphs" 
                    title="Live data graphs assembled from HBase for UML model, #{DemoBean.modelDisplayName}. Hover over the graph root and other nodes to view #{DemoBean.modelDisplayName} graph assembly information and underlying SDO metadata."/>
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
                <h:outputText value="#{item.label}"/>
                <rich:toolTip rendered="#{item.isRoot}">
                    <h:panelGrid columns="3">
                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Graph Assemble Time:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.graphAssembleTimeMillis} (milliseconds)"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Graph Node Count:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.graphNodeCount}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Graph Depth:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.graphDepth}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="HBase Tables:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.graphTableNames}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Root Type:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.typeName}"/>
                        
                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Base Types:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.baseTypeNames}"/>
                        
                    </h:panelGrid>
                </rich:toolTip>
                <rich:toolTip rendered="#{!item.isRoot && !item.leaf}">
                    <h:panelGrid columns="3">
                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Type:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.typeName}"/>
                        
                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Base Types:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.baseTypeNames}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Source Property:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyName}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Many Valued:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyIsMany}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Read Only:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyIsReadOnly}"/>

                    </h:panelGrid>
                </rich:toolTip>
                <rich:toolTip rendered="#{!item.isRoot && item.leaf}">
                    <h:panelGrid columns="3">
                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Property Name:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyName}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Data Type:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyDataType}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Many Valued:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyIsMany}"/>

                        <h:outputText 
                            style="white-space:nowrap; font-weight: bold" 
                            value="Read Only:" />
                        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                        <h:outputText value="#{item.propertyIsReadOnly}"/>

                    </h:panelGrid>
                </rich:toolTip>

            </rich:treeNode>
        </rich:tree>
    </rich:tab>
    <rich:tab id="tab_hbase" title="The raw HBase data underlying the assembled #{DemoBean.modelDisplayName} data graphs."
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
        rendered="false">
        <f:facet name="label">
            <h:panelGroup>
                <h:outputText value="Cassandra (raw data)" />
            </h:panelGroup>
        </f:facet>
    </rich:tab>
    <rich:tab id="tab_codeSamples" title="Code samples used within unit tests to create, query, modify and delete the #{DemoBean.modelDisplayName} data graphs."
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
    
    <rich:tab id="tab_config"  
        title="The CloudGraph configuration elements for #{DemoBean.modelDisplayName} including table and data graph mappings, detailed row and column key field configuration including field hashing, formatting and XPath mappings to data graph elements"
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
  <jsp:include page="/common/ContactModalPanel.jsp" flush="false"/>
</body>
</html>
</f:view>