
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
<rich:simpleTogglePanel label="Applications"
      switchType="client">
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="dataqueue_dtbl_pnl"
          action="#{ApplicationQueueBean.clear}" 
          title="Refresh this list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
  	  <a4j:commandLink 
          action="#{ProjectEditBean.create}" 
          title="#{bundle.aplsSearch_createProject_tooltip}">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.workspaceSelected}" />
      </a4j:commandLink>
      
      
      
  </h:panelGrid>     
                                                                                                                                            
  <h:panelGrid id="dataqueue_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
  <rich:dataTable id="dataqueue_dtbl" var="item" value="#{ApplicationQueueBean.data}"
       rows="#{ApplicationQueueBean.maxRows}">                                                                        

    <f:facet name="header">
        <rich:columnGroup>
           <rich:column colspan="3">
               <h:outputText value="Identification"/>
           </rich:column> 
           <rich:column colspan="1">
               <h:outputText value="Actions"/>
           </rich:column>          
		
		   <rich:column breakBefore="true">                                                                                                                               
		      <h:outputText value="#{bundle.aplsDataQueue_parentOrgName_label}"/>                 
              <rich:toolTip value="#{bundle.aplsDataQueue_parentOrgName_tooltip}"/>
		   </rich:column> 
		
		   <rich:column>                                                                                                                               
		      <h:outputText value="#{bundle.aplsDataQueue_orgName_label}"/>                 
              <rich:toolTip value="#{bundle.aplsDataQueue_orgName_tooltip}"/>
		   </rich:column>
		    
		   <rich:column>                                                                                                                               
		      <h:outputText value="#{bundle.aplsDataQueue_name_label}"/>                 
              <rich:toolTip value="#{bundle.aplsDataQueue_name_tooltip}"/>
		   </rich:column> 
		    
		   <rich:column>                                                                                                                              
		      <h:outputText value="#{bundle.aplsDataQueue_actions_label}"/>                                                                          
              <rich:toolTip value="#{bundle.aplsDataQueue_actions_tooltip}"/>
		   </rich:column>  		 
	    </rich:columnGroup>
    </f:facet>

	   <rich:column sortBy="#{item.parentOrgName}">                                                                                                                               
	       <h:outputText value="#{item.parentOrgName}"/>                                                                                         
	   </rich:column> 	
	   <rich:column sortBy="#{item.orgName}">                                                                                                                               
	       <h:outputText value="#{item.orgName}"/>                                                                                         
	   </rich:column> 	    
	   <rich:column sortBy="#{item.applicationName}">                                                                                                                               
	       <h:outputText value="#{item.applicationName}"/>                                                                                         
	   </rich:column>   
	   <rich:column id="actionsColumn">                                                                                                                              
	        <a4j:commandLink 
	            title="View this item (read-only)">                                                                               
	            <h:outputText value="view"                                                                   
	                title="View this item (read-only)"/>                                                                         
	            <f:param name="seqid" value="#{item.id}"/>
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	        <f:verbatim>&nbsp</f:verbatim>                   
	        <a4j:commandLink 
	            title="Edit this item"
	            rendered="#{UserBean.roleName == 'SUPERUSER'}">                                                                               
	            <h:outputText value="edit"                                                                   
	                title="Edit this item"/>                                                                         
	            <f:param name="seqid" value="#{item.id}"/>
	            <f:setPropertyActionListener value="true"   
	                    target="#{NavigationBean.workspaceSelected}" />                                             
	        </a4j:commandLink>                                                                                                                    
	    </rich:column>  
                                                                                                                                                             
    </rich:dataTable>                                                                                                                          
      <rich:datascroller id="dataqueue_dtbl_data_scrlr"
          align="center"
          for="dataqueue_dtbl"
          maxPages="20"
          page="#{ApplicationQueueBean.scrollerPage}"
          reRender="dashboard_content_panel"/>
                                                                                                                                            
  </h:panelGrid> 
</rich:simpleTogglePanel>     
  