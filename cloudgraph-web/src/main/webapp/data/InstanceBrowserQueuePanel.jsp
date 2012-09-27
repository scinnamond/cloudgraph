
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

                                                         
  <rich:modalPanel id="instBrowsePanel" autosized="true" width="340">
        <f:facet name="header">
            <h:outputText value="Create/Edit List Value" 
                style="padding-right:15px;"/>
        </f:facet>
        <f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/images/close_window.gif"
                    styleClass="hidelink" id="inst_brs_close_link" />
                <rich:componentControl for="instBrowsePanel" 
                    attachTo="inst_brs_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
    
                                                                                                                                            
  <h:form id="instBrowseForm">         
	  <h:panelGrid id="inst_brs_queue_dtbl_pnl" rowClasses="AlignCenter" columns="1" border="0">                                                            
	  <rich:dataTable id="inst_brs_queue_dtbl" 
	      var="item" 
	      value="#{InstanceBrowserQueueBean.data}"
	      rows="#{InstanceBrowserQueueBean.maxRows}">                                                                        
	
	      <rich:columns value="#{InstanceBrowserQueueBean.properties}" 
	          var="columns"
	          index="ind" sortBy="#{item.data[ind]}">
	         
	          <f:facet name="header">
	              <h:outputText value="#{columns.name}" />
	          </f:facet>
	    
	          <h:outputText value="#{item.data[ind]} " />
	       </rich:columns>
		   <rich:column id="actionsColumn">                                                                                                                              
		        <a4j:commandLink 
		            title="select this item">                                                                               
		            <h:outputText value="select"                                                                   
		                title="select this item"/>                                                                         
		        </a4j:commandLink>                                                                                                                    
		   </rich:column>  
	                                                                                                                                                             
	    </rich:dataTable>                                                                                                                          
	      <rich:datascroller id="inst_brs_queue_dtbl_data_scrlr"
	          align="center"
	          for="inst_brs_queue_dtbl"
	          maxPages="20"
	          page="#{InstanceBrowserQueueBean.scrollerPage}"
	          reRender="instBrowsePanel"/>	                                                                                                                                            
	  </h:panelGrid> 
    <h:panelGrid columns="2" width="50%" border="0"
        cellpadding="2" cellspacing="2"> 
        <a4j:commandButton id="instBrowsePanel_create_button" 
            value="    Ok    " 
            action="#{InstanceEditBean.save}"
            ajaxSingle="false"
            reRender="instBrowseForm,#{InstanceEditBean.saveActionReRender}"
            oncomplete="javascript:closeLiteralPanel()">
        </a4j:commandButton> 
        <a4j:commandButton id="instBrowsePanel_cancel_button" value="Cancel"
            immediate="true"
            action="#{InstanceEditBean.cancelSelectInstance}"
            onclick="Richfaces.hideModalPanel('instBrowsePanel');">
        </a4j:commandButton> 
    </h:panelGrid>
  </h:form>
  </rich:modalPanel>
  <script type="text/javascript">
    //<![CDATA[
       function closeLiteralPanel(){
            if (document.getElementById('instBrowseForm:dataEntryError')==null){
                 Richfaces.hideModalPanel('instBrowsePanel');
            };
       };
    //\]\]\>
 </script>   
  