<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <h:form id="property_edit_content_form">   
 
 <h:panelGrid id="property_content_panel" width="100%" columns="1" border="0"
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
          title="" 
          action="#{PropertyEditBean.createFromAjax}"
          reRender="property_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
      <a4j:commandLink 
        value="#{bundle.aplsPropertyEdit_save_label}"
        title="#{bundle.aplsPropertyEdit_save_tooltip}" 
        action="#{PropertyEditBean.saveFromAjax}"
        reRender="property_content_panel,admin_content_panel"/>
      <a4j:commandLink 
        value="#{bundle.aplsPropertyEdit_exit_label}"
        title="#{bundle.aplsPropertyEdit_exit_tooltip}" 
        action="#{PropertyEditBean.exit}"
        reRender="admin_content_panel"
        immediate="true">
      </a4j:commandLink>                                             
   </h:panelGrid>   
	
	<a4j:outputPanel ajaxRendered="true" id="property_errors">
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
	            value="#{bundle.aplsPropertyEdit_name_label}:" 
	            title="#{bundle.aplsPropertyEdit_name_tooltip}">
	            <rich:toolTip value="#{bundle.aplsPropertyEdit_name_tooltip}"/>
	        </h:outputText>
	        <h:inputText id="aplsPropertyEdit_name"
	            required="true"
	            maxlength="#{PropertyEditBean.nameMaxLength}"
	            value="#{PropertyEditBean.property.name}">
	            <rich:toolTip value="#{bundle.aplsPropertyEdit_name_tooltip}"/>
	        </h:inputText>                
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsPropertyEdit_definition_label}:" 
	            title="#{bundle.aplsPropertyEdit_definition_tooltip}"/>                             
	        <rich:editor 
	            id="prop_defn_descr_editor"
	            width="300" height="100"
	            viewMode="visual" 
	            readonly="false"
	            value="#{PropertyEditBean.property.definition}" 
	            validator="#{PropertyEditBean.validateDefinitionLength}"
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
            <h:outputText  
                value="#{bundle.aplsPropertyEdit_ownerClass_label}:" 
                title="#{bundle.aplsPropertyEdit_ownerClass_tooltip}"/>
	        <h:selectOneMenu id="aplsPropertyEdit_ownerClass"
	            required="true"
	            value="#{PropertyEditBean.ownerClassId}"
	            validator="#{PropertyEditBean.validateOwnerClassId}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems value="#{PropertyEditBean.ownerClassItems}" />
                <rich:toolTip value="#{bundle.aplsPropertyEdit_ownerClass_tooltip}"/>
	        </h:selectOneMenu>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
            <h:outputText  
                value="#{bundle.aplsPropertyEdit_lowerValue_label}:" 
                title="#{bundle.aplsPropertyEdit_lowerValue_tooltip}"/>
	        <h:selectOneRadio id="aplsPropertyEdit_lowerValue"
	            required="false"
	            value="#{PropertyEditBean.isRequired}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems  value="#{PropertyEditBean.requiredItems}" />
                <rich:toolTip value="#{bundle.aplsPropertyEdit_lowerValue_tooltip}"/>
	        </h:selectOneRadio>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim> 
            <h:outputText  
                value="#{bundle.aplsPropertyEdit_upperValue_label}:" 
                title="#{bundle.aplsPropertyEdit_upperValue_tooltip}"/>
	        <h:selectOneRadio id="aplsPropertyEdit_upperValue"
	            required="false"
	            value="#{PropertyEditBean.isMany}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems  value="#{PropertyEditBean.manyItems}" />
                <rich:toolTip value="#{bundle.aplsPropertyEdit_upperValue_tooltip}"/>
	        </h:selectOneRadio>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim> 
	        
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsPropertyEdit_dataType_label}:" 
	            title="#{bundle.aplsPropertyEdit_dataType_tooltip}"/>
	            
	        <rich:panel bodyClass="rich-laguna-panel-no-header">
	        <h:panelGrid columns="2" width="100%" border="0">
				<rich:tabPanel switchType="client">
				<rich:tab title="">
				    <f:facet name="label">
				        <h:panelGroup>
				            <h:outputText value="Simple" />
				        </h:panelGroup>
				    </f:facet>
				    <f:verbatim><div class="TablePopupDiv"></f:verbatim>				    
				    <rich:dataTable 
				        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
				        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
				        cellpadding="0" cellspacing="0" 
				        width="100%" border="0" 
				        var="item" value="#{PropertyEditBean.primitiveTypeItems}">
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Name" />
				          </f:facet>  
			              <a4j:commandLink 
			                value="#{item.label}" reRender="selected_datatype">                                
			                <f:setPropertyActionListener value="0"               
			                            target="#{PropertyEditBean.type}" />                                        
			                <f:setPropertyActionListener value="#{item.value}"                         
			                            target="#{PropertyEditBean.dataTypeId}" />                                       
			              </a4j:commandLink>                                                                
				      </rich:column>
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Definition" />
				          </f:facet>  
	                      <h:outputText value="#{item.description}"/>
				      </rich:column>
				    </rich:dataTable>
				    <f:verbatim></div></f:verbatim>				    
				</rich:tab>
				<rich:tab title="">
				    <f:facet name="label">
				        <h:panelGroup>
				            <h:outputText value="Value List" />
				        </h:panelGroup>
				    </f:facet>
				    <f:verbatim><div class="TablePopupDiv"></f:verbatim>				    
				    <rich:dataTable 
				        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
				        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
				        cellpadding="0" cellspacing="0" 
				        width="100%" border="0" 
				        var="item" value="#{PropertyEditBean.enumerationItems}">
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Name" />
				          </f:facet>  
			              <a4j:commandLink 
			                value="#{item.label}" reRender="selected_datatype">                                
			                <f:setPropertyActionListener value="1"               
			                            target="#{PropertyEditBean.type}" />                                        
			                <f:setPropertyActionListener value="#{item.value}"                         
			                            target="#{PropertyEditBean.dataTypeId}" />                                       
			              </a4j:commandLink>                                                  
				      </rich:column>
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Definition" />
				          </f:facet>  
	                      <h:outputText value="#{item.description}"/>
				      </rich:column>				    </rich:dataTable>
				    <f:verbatim></div></f:verbatim>	
				</rich:tab>
				<rich:tab title="">
				    <f:facet name="label">
				        <h:panelGroup>
				            <h:outputText value="Class" />
				        </h:panelGroup>
				    </f:facet>
				    <f:verbatim><div class="TablePopupDiv"></f:verbatim>				    
				    <rich:dataTable 
				        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
				        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
				        cellpadding="0" cellspacing="0" 
				        width="100%" border="0" 
				        var="item" value="#{PropertyEditBean.classItems}">
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Name" />
				          </f:facet>  
			              <a4j:commandLink 
			                value="#{item.label}" reRender="selected_datatype">                                
			                <f:setPropertyActionListener value="2"               
			                            target="#{PropertyEditBean.type}" />                                        
			                <f:setPropertyActionListener value="#{item.value}"                         
			                            target="#{PropertyEditBean.dataTypeId}" />                                       
			              </a4j:commandLink>                                                  
				      </rich:column>				    
				      <rich:column>
				          <f:facet name="header">
				              <h:outputText value="Definition" />
				          </f:facet>  
	                      <h:outputText value="#{item.description}"/>
				      </rich:column>				    </rich:dataTable>
				    <f:verbatim></div></f:verbatim>	
				</rich:tab>
				</rich:tabPanel>
				<rich:panel id="selected_datatype" bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Data Type" /> 
			        <h:inputText 
			            required="false"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{PropertyEditBean.selectedDataTypeName}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>	        
	        </h:panelGrid>

	        </rich:panel>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim> 
	    </h:panelGrid>
    </rich:tab>
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Perspective" />
	        </h:panelGroup>
	    </f:facet>
        <jsp:include page="/configuration/PropertyPerspectivePanel.jsp" flush="false"/>
    </rich:tab>
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Categorization" />
	        </h:panelGroup>
	    </f:facet>
        <jsp:include page="/configuration/PropertyCategorizationPanel.jsp" flush="false"/>
    </rich:tab>
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Campaigns" />
	        </h:panelGroup>
	    </f:facet>
    </rich:tab>
    </rich:tabPanel>
 </h:panelGrid>
 </h:form>



