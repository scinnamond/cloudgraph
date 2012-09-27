<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="enumeration_edit_content_form">   
 
 <h:panelGrid id="enumeration_content_panel" width="100%" columns="1" border="0"
      rowClasses="AlignTop" columnClasses="AlignTop"> 

  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <a4j:commandLink 
          reRender="admin_content_panel"
          title="Refresh this list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
      <a4j:commandLink 
        value="#{bundle.aplsEnumerationEdit_save_label}"
        title="#{bundle.aplsEnumerationEdit_save_tooltip}" 
        action="#{EnumerationEditBean.saveFromAjax}"
        reRender="enumeration_content_panel"/>
      <a4j:commandLink 
        value="#{bundle.aplsEnumerationEdit_exit_label}"
        title="#{bundle.aplsEnumerationEdit_exit_tooltip}" 
        action="#{EnumerationEditBean.exit}"
        reRender="dashboard_content_panel"/>
   </h:panelGrid>   
	
	<a4j:outputPanel ajaxRendered="true" id="enumeration_errors">
			<h:messages showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
	</a4j:outputPanel>
	
	<rich:tabPanel switchType="ajax">
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="General" />
	        </h:panelGroup>
	    </f:facet>

	    <h:panelGrid id="basic_panel" rowClasses="FormPanelRow" 
	        columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn" 
	        columns="4" width="95%" border="0">  
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsEnumerationEdit_name_label}:" 
	            title="#{bundle.aplsEnumerationEdit_name_tooltip}"/>
	        <h:inputText id="aplsEnumerationEdit_name"
	            required="true"
	            maxlength="#{EnumerationEditBean.nameMaxLength}"
	            value="#{EnumerationEditBean.name}"
	            title="#{bundle.aplsEnumerationEdit_name_tooltip}">
	        </h:inputText>                
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsEnumerationEdit_definition_label}:" 
	            title="#{bundle.aplsEnumerationEdit_definition_tooltip}"/>                             
	        <rich:editor 
	            id="prop_defn_descr_editor"
	            width="280" height="100"
	            viewMode="visual" 
	            readonly="false"
	            value="#{EnumerationEditBean.definition}" 
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
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>

	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsEnumerationEdit_literals_label}:" 
	            title="#{bundle.aplsEnumerationEdit_literals_tooltip}"/>                             

	        <h:panelGrid columns="1" width="100%" border="0">  
			    <rich:dataTable 
			        rendered="true"
			        id="literals_table"
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="literal" value="#{EnumerationEditBean.enumeration.ownedLiteral}">
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Value" />
			          </f:facet>  
			          <h:outputText value="#{literal.name}" />	              
			      </rich:column>
				  <rich:column id="actionsColumn">                                                                                                                              
			          <f:facet name="header">
			              <h:outputText value="Actions" />
			          </f:facet>  
		              <a4j:commandLink id="edit_literal_link" 
		                value="edit"
		                oncomplete="#{rich:component('createEditLiteralPanel')}.show()"  
		                reRender="createEditLiteralForm">                                
		                <f:setPropertyActionListener value="#{literal}"                         
		                            target="#{EnumerationEditBean.literal}" />                                       
		                <f:setPropertyActionListener value="literals_table"               
		                            target="#{EnumerationEditBean.saveActionReRender}" />                                        
		              </a4j:commandLink>                                                  
		              <f:verbatim>&nbsp</f:verbatim>                                      
		              <a4j:commandLink id="delete_literal_link"                             
		                    value="delete"                                                  
		                    action="#{EnumerationEditBean.deleteLiteral}"                           
		                reRender="literals_table">                                        
		                <f:setPropertyActionListener value="#{literal}"                         
		                            target="#{EnumerationEditBean.literal}" />                                       
		              </a4j:commandLink> 
				    </rich:column>  
			        <rich:column>
			            <f:facet name="header">
			              <h:outputText value="Definition" />
			            </f:facet>  
			            <h:outputText value="#{literal.definition}" />	              
			        </rich:column>
			    </rich:dataTable>
		        <a4j:commandButton id="add_literal_btn" 
		            value="#{bundle.aplsEnumerationEdit_addLiteral_label}"
		            title="#{bundle.aplsEnumerationEdit_addLiteral_tooltip}" 
		            action="#{EnumerationEditBean.createLiteral}"           
		            oncomplete="#{rich:component('createEditLiteralPanel')}.show()"
		            reRender="createEditLiteralForm">
		            <f:setPropertyActionListener value="literals_table"   
		                    target="#{EnumerationEditBean.saveActionReRender}" />                                            
		        </a4j:commandButton>    
		    </h:panelGrid>             
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>

	 
	    </h:panelGrid>
    </rich:tab>

    </rich:tabPanel>
 </h:panelGrid>
 </a4j:form>



