<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

  <rich:modalPanel id="personalizeQueuePanel" autosized="true" width="340">
        <f:facet name="header">
            <h:outputText value="Personalize Data List" 
                style="padding-right:15px;"/>
        </f:facet>
        <f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/images/close_window.gif"
                    styleClass="hidelink" id="pers_queue_close_link" />
                <rich:componentControl for="personalizeQueuePanel" 
                    attachTo="pers_queue_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
        <h:form id="personalizeQueueForm">         

        <h:panelGrid columns="1" width="100%" border="0">
			<a4j:outputPanel ajaxRendered="true" id="enum_literal_errors">
					<h:messages id="dataEntryError" showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
			</a4j:outputPanel>
            <f:verbatim>&nbsp</f:verbatim>    
            <f:verbatim>&nbsp</f:verbatim>    

                <rich:listShuttle id="propListShuttle" 
                    sourceValue="#{InstanceQueueBean.availableProperties}" 
                    targetValue="#{InstanceQueueBean.selectedProperties}"  
                    sourceRequired="false" targetRequired="false"
                    sourceListWidth="170" targetListWidth="170" 
                    var="item" converter="PropertyConverter"
                    sourceCaptionLabel="Available Properties"
                    targetCaptionLabel="Selected Properties">
	                <rich:column>
	                    <f:facet name="header">
	                        <h:outputText value="Name" />
	                    </f:facet>
	      		        <h:graphicImage value="/images/properties.png"/>
	                    <h:outputText value="#{item.label}" />
	                </rich:column>
                </rich:listShuttle>
                	        
            <f:verbatim>&nbsp</f:verbatim>    
            <f:verbatim>&nbsp</f:verbatim>    
            <h:panelGrid columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton id="personalizeQueuePanel_create_button" 
                    value="    Ok    " 
                    action="#{InstanceQueueBean.save}"
                    ajaxSingle="false"
                    reRender="personalizeQueueForm,#{InstanceQueueBean.saveActionReRender}"
                    oncomplete="javascript:closeLiteralPanel()">
                </a4j:commandButton> 
                <a4j:commandButton id="personalizeQueuePanel_cancel_button" value="Cancel"
                    immediate="true"
                    action="#{InstanceQueueBean.cancel}"
                    onclick="Richfaces.hideModalPanel('personalizeQueuePanel');">
                </a4j:commandButton> 
            </h:panelGrid>
        </h:panelGrid>
        </h:form>
  </rich:modalPanel>
  <script type="text/javascript">
    //<![CDATA[
       function closeLiteralPanel(){
            if (document.getElementById('personalizeQueueForm:dataEntryError')==null){
                 Richfaces.hideModalPanel('personalizeQueuePanel');
            };
       };
    //\]\]\>
 </script>	        
  	