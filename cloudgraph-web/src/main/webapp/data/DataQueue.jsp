
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
<rich:simpleTogglePanel label="#{SearchBean.clazzName} List"
      switchType="client">
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="dataqueue_dtbl_pnl"
          action="#{InstanceQueueBean.refresh}" 
          title="Refresh the #{SearchBean.clazzName} list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
  	  <a4j:commandLink 
          action="#{InstanceEditBean.create}" 
          title="Create new #{SearchBean.clazzName}">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.administrationSelected}" />
      </a4j:commandLink>
  	  <a4j:commandLink 
          title="Personalize the #{SearchBean.clazzName} list"
		  oncomplete="#{rich:component('personalizeQueuePanel')}.show()"  
          reRender="personalizeQueueForm">
          <h:graphicImage value="/images/person2.png"/>
          <rich:spacer width="18" height="1"/>
          <f:setPropertyActionListener value="dataqueue_dtbl"               
                    target="#{InstanceQueueBean.saveActionReRender}" />                                        
      </a4j:commandLink>      
      
      
  </h:panelGrid>     
                                                                                                                                            
  <h:panelGrid id="dataqueue_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
  <rich:dataTable id="dataqueue_dtbl" 
      var="item" 
      value="#{InstanceQueueBean.data}"
      rows="#{InstanceQueueBean.maxRows}">                                                                        

      <f:facet name="header">
        <rich:columnGroup>
           <rich:column colspan="1">
               <h:outputText value="Actions"/>
           </rich:column>
           <rich:column
               rendered="#{InstanceQueueBean.identificationPropertiesCount > 0}" 
               colspan="#{InstanceQueueBean.identificationPropertiesCount}">
               <h:outputText value="Identification"/>
           </rich:column>
           <rich:column
               rendered="#{InstanceQueueBean.costPropertiesCount > 0}" 
               colspan="#{InstanceQueueBean.costPropertiesCount}">
               <h:outputText value="Cost"/>
           </rich:column>
           <rich:column 
               rendered="#{InstanceQueueBean.hostingPropertiesCount > 0}"
               colspan="#{InstanceQueueBean.hostingPropertiesCount}">
               <h:outputText value="Hosting"/>
           </rich:column>
           <rich:column
               rendered="#{InstanceQueueBean.compliancePropertiesCount > 0}" 
               colspan="#{InstanceQueueBean.compliancePropertiesCount}">
               <h:outputText value="Compliance"/>
           </rich:column>
           <rich:column
               rendered="#{InstanceQueueBean.processPropertiesCount > 0}" 
               colspan="#{InstanceQueueBean.processPropertiesCount}">
               <h:outputText value="Process"/>
           </rich:column>
           <rich:column 
               rendered="#{InstanceQueueBean.otherPropertiesCount > 0}"
               colspan="#{InstanceQueueBean.otherPropertiesCount}">
               <h:outputText value="Other"/>
           </rich:column>
        </rich:columnGroup> 
      </f:facet>
      
	  <rich:column id="actionsColumn">                                                                                                                              
	        <a4j:commandLink 
	            action="#{InstanceEditBean.edit}"
	            title="View this item (read-only)">                                                                               
	            <h:outputText value="view"                                                                   
	                title="View this item (read-only)"/>                                                                         
	            <f:setPropertyActionListener value="#{item.id}"   
	                    target="#{InstanceEditBean.instanceId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.administrationSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	        <f:verbatim>&nbsp</f:verbatim>                   
	        <a4j:commandLink 
	            title="Edit this item"
	            action="#{InstanceEditBean.edit}"
	            rendered="#{UserBean.roleName == 'SUPERUSER'}">                                                                               
	            <h:outputText value="edit"                                                                   
	                title="Edit this item"/>                                                                         
	            <f:setPropertyActionListener value="#{item.id}"   
	                    target="#{InstanceEditBean.instanceId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.administrationSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
      </rich:column>  
                                                                                                                                                                 
      <rich:columns value="#{InstanceQueueBean.properties}" 
          var="col" index="ind" 
          sortBy="#{item.data[ind]}" sortOrder="#{col.queueColumnSortOrder}">        
          <f:facet name="header">
              <h:outputText value="#{col.displayName}" />
          </f:facet>   
          <h:outputText value="#{item.data[ind]} " />
      </rich:columns>
       
  </rich:dataTable>                                                                                                                          
      <rich:datascroller id="dataqueue_dtbl_data_scrlr"
          align="center"
          for="dataqueue_dtbl"
          maxPages="20"
          page="#{InstanceQueueBean.scrollerPage}"
          reRender="dashboard_content_panel"/>
                                                                                                                                            
  </h:panelGrid> 
</rich:simpleTogglePanel>     
  