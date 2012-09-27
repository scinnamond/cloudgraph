
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
<a4j:form id="campaign_queue_form">   
<rich:simpleTogglePanel label="Campaigns"
      switchType="client">
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="cmpgn_dtbl_pnl"
          action="#{CampaignQueueBean.clear}" 
          title="Refresh this Campaign list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
  	  <a4j:commandLink 
          action="#{CampaignEditBean.create}" 
          title="#{bundle.aplsSearch_createCampaign_tooltip}"
          reRender="admin_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.workspaceSelected}" />
      </a4j:commandLink>
      
      
      
  </h:panelGrid>     
                                                                                                                                            
  <h:panelGrid id="cmpgn_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
  <rich:dataTable id="cmpgn_dtbl" var="campaign" value="#{CampaignQueueBean.data}"
       rows="#{CampaignQueueBean.maxRows}">                                                                        
	   <rich:column sortBy="#{campaign.name}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_name_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.name}"/>                                                                                         
	   </rich:column>   
	   <rich:column sortBy="#{campaign.type}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_type_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.type}"/>                                                                                         
	   </rich:column>   
	   <rich:column id="actionsColumn">                                                                                                                              
           <f:facet name="header">
		      <h:outputText value="#{bundle.aplsCampaigns_actions_label}"                                                                    
		          title="#{bundle.aplsCampaigns_actions_tooltip_label}"/>                                                                          
           </f:facet>  
	        <a4j:commandLink 
	            title="View this campaign (read-only)"
	            action="#{CampaignEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="view"                                                                   
	                title="View this campaign (read-only)"/>                                                                         
	            <f:setPropertyActionListener value="#{campaign.id}"   
	                    target="#{CampaignEditBean.campaignId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	        <f:verbatim>&nbsp</f:verbatim>                   
	        <a4j:commandLink 
	            title="Edit this campaign"
	            rendered="#{UserBean.roleName == 'SUPERUSER'}"
	            action="#{CampaignEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="edit"                                                                   
	                title="Edit this Campaign"/>                                                                         
	            <f:setPropertyActionListener value="#{campaign.id}"   
	                    target="#{CampaignEditBean.campaignId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	    </rich:column>  
	    <rich:column sortBy="#{campaign.description}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_description_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.description}"/>                                                                                         
	    </rich:column>   
	    <rich:column sortBy="#{campaign.dispersal}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_dispersal_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.dispersal}"/>                                                                                         
	    </rich:column>   
	    <rich:column sortBy="#{campaign.startDate}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_startDate_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.startDate}"/>                                                                                         
	    </rich:column>   
	    <rich:column sortBy="#{campaign.endDate}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsCampaigns_endDate_label}" />
           </f:facet>  
	       <h:outputText value="#{campaign.endDate}"/>                                                                                         
	    </rich:column>   
                                                                                                                                                             
    </rich:dataTable>                                                                                                                          
      <rich:datascroller id="cmpgn_dtbl_data_scrlr"
          align="center"
          for="cmpgn_dtbl"
          maxPages="20"
          page="#{CampaignQueueBean.scrollerPage}"
          reRender="cmpgn_dtbl_pnl"/>
                                                                                                                                            
  </h:panelGrid> 
</rich:simpleTogglePanel>     
</a4j:form>   
 