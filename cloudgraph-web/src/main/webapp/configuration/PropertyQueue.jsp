
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
<a4j:form id="property_queue_form">   
<rich:simpleTogglePanel label="Data Attributes"
      switchType="client">
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="prop_dtbl_pnl"
          action="#{PropertyQueueBean.clear}" 
          title="Refresh this list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
  	  <a4j:commandLink 
          action="#{PropertyEditBean.create}" 
          title="#{bundle.aplsSearch_createProperty_tooltip}"
          reRender="admin_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.workspaceSelected}" />
      </a4j:commandLink>
   </h:panelGrid>     
                                                                                                                                            
  <h:panelGrid id="prop_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
  <rich:dataTable id="prop_dtbl" var="prop" value="#{PropertyQueueBean.data}"
       rows="#{PropertyQueueBean.maxRows}">                                                                        
	   <rich:column sortBy="#{prop.className}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_className_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.className}"/>                                                                                         
	   </rich:column>   
	   <rich:column sortBy="#{prop.cat}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_cat_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.cat}"/>                                                                                         
	   </rich:column>   
	   <rich:column sortBy="#{prop.name}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_name_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.name}"/>                                                                                         
	   </rich:column>   
	   <rich:column id="actionsColumn">                                                                                                                              
           <f:facet name="header">
		      <h:outputText value="#{bundle.aplsProperties_actions_label}"                                                                    
		          title="#{bundle.aplsProperties_actions_tooltip_label}"/>                                                                          
           </f:facet>  
	        <a4j:commandLink 
	            title="View this prop (read-only)"
	            action="#{PropertyEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="view"                                                                   
	                title="View this Property (read-only)"/>                                                                         
	            <f:setPropertyActionListener value="#{prop.propertyId}"   
	                    target="#{PropertyEditBean.propertyId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	        <f:verbatim>&nbsp</f:verbatim>                   
	        <a4j:commandLink 
	            title="Edit this Property"
	            rendered="#{UserBean.roleName == 'SUPERUSER'}"
	            action="#{PropertyEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="edit"                                                                   
	                title="Edit this Property"/>                                                                         
	            <f:setPropertyActionListener value="#{prop.propertyId}"   
	                    target="#{PropertyEditBean.propertyId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
            <f:verbatim>&nbsp</f:verbatim>                   
            <a4j:commandLink 
                title="Delete this Property"
                rendered="#{UserBean.roleName == 'SUPERUSER'}"
                action="#{PropertyEditBean.deleteConfirm}"
                oncomplete="#{rich:component('deletePropertyConfirmModalPanel')}.show()"
                reRender="prop_delete_confirm_panel_form">                                                                               
                <h:outputText value="delete"                                                                   
                    title="Delete this Property"/>                                                                         
                <f:setPropertyActionListener value="#{prop.propertyId}"   
                        target="#{PropertyEditBean.propertyId}" />                                             
            </a4j:commandLink>                                                                                                                    
	    </rich:column>  
	   <rich:column sortBy="#{prop.definition}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_definition_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.definition}"/>                                                                                         
	   </rich:column>   
	   <rich:column sortBy="#{prop.dataType}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_dataType_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.dataType}"/>                                                                                         
	   </rich:column>   
	   <rich:column sortBy="#{prop.cardinality}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsProperties_cardinality_label}" />
           </f:facet>  
	       <h:outputText value="#{prop.cardinality}"/>                                                                                         
	   </rich:column>   
                                                                                                                                                             
    </rich:dataTable>                                                                                                                          
      <rich:datascroller id="prop_dtbl_data_scrlr"
          align="center"
          for="prop_dtbl"
          maxPages="20"
          page="#{PropertyQueueBean.scrollerPage}"
          reRender="prop_dtbl_pnl"/>
                                                                                                                                            
  </h:panelGrid> 
</rich:simpleTogglePanel>     
</a4j:form>   
 