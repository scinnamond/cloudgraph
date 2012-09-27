
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
<a4j:form id="enumeration_queue_form">   
<rich:simpleTogglePanel label="Value Lists"
      switchType="client">
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="enm_dtbl_pnl"
          action="#{EnumerationQueueBean.clear}" 
          title="Refresh this list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
  	  <a4j:commandLink 
          action="#{EnumerationEditBean.create}" 
          title="#{bundle.aplsSearch_createEnumeration_tooltip}"
          reRender="admin_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.workspaceSelected}" />
      </a4j:commandLink>
      
      
      
  </h:panelGrid>     
                                                                                                                                            
  <h:panelGrid id="enm_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
  <rich:dataTable id="enm_dtbl" var="enumeration" value="#{EnumerationQueueBean.data}"
       rows="#{EnumerationQueueBean.maxRows}">                                                                        
	   <rich:column sortBy="#{enumeration.name}">                                                                                                                               
           <f:facet name="header">
              <h:outputText value="#{bundle.aplsEnumerations_name_label}" />
           </f:facet>  
	       <h:outputText value="#{enumeration.name}"/>                                                                                         
	   </rich:column>   
	   <rich:column id="actionsColumn">                                                                                                                              
           <f:facet name="header">
		      <h:outputText value="#{bundle.aplsEnumerations_actions_label}"                                                                    
		          title="#{bundle.aplsEnumerations_actions_tooltip_label}"/>                                                                          
           </f:facet>  
	        <a4j:commandLink 
	            title="View this enumeration (read-only)"
	            action="#{EnumerationEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="view"                                                                   
	                title="View this enumeration (read-only)"/>                                                                         
	            <f:setPropertyActionListener value="#{enumeration.id}"   
	                    target="#{EnumerationEditBean.enumerationId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	        <f:verbatim>&nbsp</f:verbatim>                   
	        <a4j:commandLink 
	            title="Edit this enumeration"
	            rendered="#{UserBean.roleName == 'SUPERUSER'}"
	            action="#{EnumerationEditBean.edit}"
	            reRender="admin_content_panel">                                                                               
	            <h:outputText value="edit"                                                                   
	                title="Edit this Enumeration"/>                                                                         
	            <f:setPropertyActionListener value="#{enumeration.id}"   
	                    target="#{EnumerationEditBean.enumerationId}" />                                             
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	    </rich:column>  
                                                                                                                                                             
    </rich:dataTable>                                                                                                                          
      <rich:datascroller id="enm_dtbl_data_scrlr"
          align="center"
          for="enm_dtbl"
          maxPages="20"
          page="#{EnumerationQueueBean.scrollerPage}"
          reRender="enm_dtbl_pnl"/>
                                                                                                                                            
  </h:panelGrid> 
</rich:simpleTogglePanel>     
</a4j:form>   
 