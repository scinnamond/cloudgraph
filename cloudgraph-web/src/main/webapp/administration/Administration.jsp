
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <title><h:outputText value="#{bundle.aplsWindowTitle}"/></title>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
</head>
<body>
<a4j:outputPanel id="dashboard_content_panel">
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
  <a4j:outputPanel id="admin_content_panel">
  <f:verbatim><div class="ContentDiv"></f:verbatim>
  <h:form id="admin_form">                                                                                                                
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 
	  <h:panelGrid width="100%" columns="1" styleClass="DashboardTable"
          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
          cellpadding="0" cellspacing="0"> 
	          
        <rich:tabPanel switchType="ajax"
            rendered="#{!(TaxonomyEditBean.hasSelectedTaxonomy || PropertyEditBean.hasProperty || ClassEditBean.hasClazz || PackageEditBean.hasPackage || EnumerationEditBean.hasEnumeration || InstanceEditBean.hasInstance || CampaignEditBean.hasCampaign)}">

        <rich:tab title=""
            rendered="true">
            <f:facet name="label">
                <h:panelGroup>
                    <h:outputText value="Data" />
                </h:panelGroup>
            </f:facet>
            <jsp:include page="/data/DataQueue.jsp" flush="false"/>
        </rich:tab>
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

	    <rich:simpleTogglePanel label="#{TaxonomyEditBean.title}"
	        rendered="#{TaxonomyEditBean.hasSelectedTaxonomy}"
	          switchType="client">          
	       <jsp:include page="/administration/TaxonomyEditPanel.jsp" flush="false"/>          
	    </rich:simpleTogglePanel>           
	
	    <rich:simpleTogglePanel label="#{PropertyEditBean.title}"
	        rendered="#{PropertyEditBean.hasProperty}"
	          switchType="client">          
	       <jsp:include page="/configuration/PropertyEditPanel.jsp" flush="false"/>       
	    </rich:simpleTogglePanel>  
	             
	    <rich:simpleTogglePanel label="#{ClassEditBean.title}"
	        rendered="#{ClassEditBean.hasClazz}"
	          switchType="client">          
	       <jsp:include page="/configuration/ClassEditPanel.jsp" flush="false"/>          
	    </rich:simpleTogglePanel> 
	    
	    <rich:simpleTogglePanel label="#{PackageEditBean.title}"
	        rendered="#{PackageEditBean.hasPackage}"
	          switchType="client">          
	       <jsp:include page="/configuration/PackageEditPanel.jsp" flush="false"/>        
	    </rich:simpleTogglePanel> 
	
	    <rich:simpleTogglePanel label="#{EnumerationEditBean.title}"
	        rendered="#{EnumerationEditBean.hasEnumeration}"
	          switchType="client">          
	       <jsp:include page="/configuration/EnumerationEditPanel.jsp" flush="false"/>        
	    </rich:simpleTogglePanel> 
	    
	    <rich:simpleTogglePanel label="#{InstanceEditBean.title}"
	        rendered="#{InstanceEditBean.hasInstance && !InstanceEditBean.isDelete}"
	          switchType="client">          
	       <jsp:include page="/data/InstanceEditPanel.jsp" flush="false"/>        
	    </rich:simpleTogglePanel> 
	
	    <rich:simpleTogglePanel label="#{CampaignEditBean.title}"
	        rendered="#{CampaignEditBean.hasCampaign}"
	          switchType="client">          
	       <jsp:include page="/campaign/CampaignEditPanel.jsp" flush="false"/>        
	    </rich:simpleTogglePanel> 

	
      </h:panelGrid>
  </h:panelGrid>
  </h:form>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>

  <jsp:include page="/common/SettingsModalPanel.jsp" flush="false"/>
  <jsp:include page="/common/ContactModalPanel.jsp" flush="false"/>
  <jsp:include page="/administration/TaxonomyDeleteConfirmModalPanel.jsp" flush="false"/>
  <jsp:include page="/administration/TaxonomyExportModalPanel.jsp" flush="false"/>
  <jsp:include page="/data/PersonalizeQueuePanel.jsp" flush="false"/>
  <jsp:include page="/configuration/PropertyDeleteConfirmModalPanel.jsp" flush="false"/>
   <jsp:include page="/common/CreateEditCategorizationPanel.jsp" flush="false"/>
  <jsp:include page="/configuration/CreateEditEnumerationLiteralPanel.jsp" flush="false"/>
  <jsp:include page="/data/InstanceBrowserQueuePanel.jsp" flush="false"/>      
  <jsp:include page="/data/InstanceDeleteConfirmModalPanel.jsp" flush="false"/>
       
</body>
</html>
</f:view>
