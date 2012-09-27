<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

  <rich:modalPanel id="createEditLiteralPanel" autosized="true" width="340">
        <f:facet name="header">
            <h:outputText value="Create/Edit List Value" 
                style="padding-right:15px;"/>
        </f:facet>
        <f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/images/close_window.gif"
                    styleClass="hidelink" id="literal_panel_close_link" />
                <rich:componentControl for="createEditLiteralPanel" 
                    attachTo="literal_panel_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
        <h:form id="createEditLiteralForm">         

        <h:panelGrid columns="1" width="100%" border="0">
			<a4j:outputPanel ajaxRendered="true" id="enum_literal_errors">
					<h:messages id="dataEntryError" showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
			</a4j:outputPanel>
            <f:verbatim>&nbsp</f:verbatim>    
            <f:verbatim>&nbsp</f:verbatim>    

	        <h:panelGrid rowClasses="FormPanelRow" 
	                columnClasses="FormLabelColumn,FormControlColumn" 
	                columns="2" width="95%" border="0">  
	            <h:outputText styleClass="labelBold" 
	                value="#{bundle.aplsLiteralEdit_name_label}:" 
	                title="#{bundle.aplsLiteralEdit_name_tooltip}"/>
	            <h:inputText
	                title="#{bundle.aplsLiteralEdit_name_tooltip}"
	                size="30" 
	                maxlength="#{EnumerationEditBean.literalNameMaxLength}"
	                required="true"
	                requiredMessage="Name is a required field"
	                validator="#{EnumerationEditBean.validateLiteralNameLength}"
	                value="#{EnumerationEditBean.literal.name}"/>        	                
		        <h:outputText styleClass="labelBold" 
		            value="#{bundle.aplsLiteralEdit_definition_label}:" 
		            title="#{bundle.aplsLiteralEdit_definition_tooltip}"/>                             
		        <rich:editor 
		    		width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{EnumerationEditBean.literal.definition}" 
		            validator="#{EnumerationEditBean.validateDefinitionLength}"
		            validatorMessage="Definition text is longer than allowed maximum characters"
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
		        </rich:editor>
	        </h:panelGrid> 
	        
            <f:verbatim>&nbsp</f:verbatim>    
            <f:verbatim>&nbsp</f:verbatim>    
            <h:panelGrid columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton id="createEditLiteralPanel_create_button" 
                    value="    Ok    " 
                    action="#{EnumerationEditBean.save}"
                    ajaxSingle="false"
                    reRender="createEditLiteralForm,#{EnumerationEditBean.saveActionReRender}"
                    oncomplete="javascript:closeLiteralPanel()">
                </a4j:commandButton> 
                <a4j:commandButton id="createEditLiteralPanel_cancel_button" value="Cancel"
                    immediate="true"
                    action="#{EnumerationEditBean.cancelCreateEditLiteral}"
                    onclick="Richfaces.hideModalPanel('createEditLiteralPanel');">
                </a4j:commandButton> 
            </h:panelGrid>
        </h:panelGrid>
        </h:form>
  </rich:modalPanel>
  <script type="text/javascript">
    //<![CDATA[
       function closeLiteralPanel(){
            if (document.getElementById('createEditLiteralForm:dataEntryError')==null){
                 Richfaces.hideModalPanel('createEditLiteralPanel');
            };
       };
    //\]\]\>
 </script>	        
  	