<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="inst_edit_content_form">   
 
 <h:panelGrid id="inst_content_panel" width="100%" columns="1" border="0"
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
          action="#{InstanceEditBean.createFromAjax}"
          reRender="inst_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
      <a4j:commandLink 
        value="#{bundle.aplsClassEdit_save_label}"
        title="#{bundle.aplsClassEdit_save_tooltip}" 
        action="#{InstanceEditBean.saveFromAjax}"
        reRender="inst_content_panel,admin_content_panel,configuration_tab"/>
      <a4j:commandLink 
        value="#{bundle.aplsClassEdit_exit_label}"
        title="#{bundle.aplsClassEdit_exit_tooltip}" 
        action="#{InstanceEditBean.exit}"
        reRender="dashboard_content_panel"/>
   </h:panelGrid>   
	
	<a4j:outputPanel ajaxRendered="true" id="inst_errors">
			<h:messages showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
	</a4j:outputPanel>
	
	<rich:tabPanel switchType="ajax">
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Basic" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table1" value="#{InstanceEditBean.identificationSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table1">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="#{InstanceEditBean.costSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Cost" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table2" value="#{InstanceEditBean.costSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
 	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table2">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="#{InstanceEditBean.investmentSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Investment" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table3" value="#{InstanceEditBean.investmentSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table3">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="#{InstanceEditBean.hostingSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Hosting" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table4" value="#{InstanceEditBean.hostingSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table4">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="#{InstanceEditBean.processSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Process" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table5" value="#{InstanceEditBean.processSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table5">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="#{InstanceEditBean.complianceSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Compliance" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table6" value="#{InstanceEditBean.complianceSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table6">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>
    
	<rich:tab title=""
	    rendered="#{InstanceEditBean.otherSlotsCount > 0}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Other" />
	        </h:panelGroup>
	    </f:facet>
        <h:dataTable id="slots_table7" value="#{InstanceEditBean.otherSlots}" 
            var="slot">
            <h:column>
                <h:outputText styleClass="labelBold" 
                    value="#{slot.propertyName}:"/>
            </h:column>
            <h:column>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isString}"
                    required="#{slot.isRequired}">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
		        <rich:editor 
		            rendered="#{slot.isLongString}"
		            width="280" height="100"
		            viewMode="visual" 
		            readonly="false"
		            value="#{slot.value}" 
		            useSeamText="false"
		            theme="advanced" 
		            plugins="paste">
		            <f:param name="theme_advanced_buttons1" value="bold,italic,underline,separator,cut,copy,paste"/>
		            <f:param name="theme_advanced_buttons2" value=""/>
		            <f:param name="theme_advanced_buttons3" value=""/>
		            <f:param name="theme_advanced_toolbar_location" value="top"/>                               
		            <f:param name="theme_advanced_toolbar_align" value="left"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
		        </rich:editor>
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isIntegral}"
                    required="#{slot.isRequired}">
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <h:inputText value="#{slot.value}" 
                    rendered="#{slot.isFloatingPoint}"
                    required="#{slot.isRequired}">
                    <f:convertNumber maxFractionDigits="2"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:inputText>    
                <a4j:outputPanel layout="block"
                    rendered="#{slot.isDate}"
                    title="">
                    <rich:calendar value="#{slot.value}"
                        datePattern="d/MMM/yy"
                        cellWidth="24px" cellHeight="22px" 
                        style="width:200px"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </a4j:outputPanel>	            
                <h:selectOneRadio
                    rendered="#{slot.isBoolean}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.booleanItems}" />
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneRadio>   
                <h:selectOneMenu
                    rendered="#{slot.isClassType}"
                    required="#{slot.isRequired}"
                    value="#{slot.value}"
                    disabled="false"
                    disabledClass="color:gray">
                    <f:selectItems value="#{slot.classTypeItems}" />
                    <f:convertNumber integerOnly="true"/>
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
                </h:selectOneMenu>   
				<rich:panel
				    rendered="#{slot.isEnumerationType}" 
				    bodyClass="rich-laguna-panel-no-header">
	                <rich:toolTip value="#{slot.propertyDefinition}"/>
				<h:panelGrid columns="2" width="100%" 
                    cellpadding="3" cellspacing="3" border="0"> 
			    <rich:dataTable 
			        onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
			        onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
			        cellpadding="0" cellspacing="0" 
			        width="100%" border="0" 
			        var="item" value="#{slot.enumerationLiteralItems}">
		          <f:facet name="header">
		              <h:outputText value="Available Values" />
		          </f:facet>  
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Name" />
			          </f:facet>  
		              <a4j:commandLink 
		                value="#{item.label}" 
		                reRender="slots_table7">                                
		                <f:setPropertyActionListener value="#{item.value}"                         
		                            target="#{slot.value}" />                                       
		              </a4j:commandLink>                                                  
			      </rich:column>
			      <rich:column>
			          <f:facet name="header">
			              <h:outputText value="Definition" />
			          </f:facet>  
                      <h:outputText value="#{item.description}"/>
			      </rich:column>				    
			    </rich:dataTable>                
				<rich:panel bodyClass="rich-laguna-panel-no-header">
			        <h:panelGrid columns="1" width="100%" border="0">
			        <h:outputText styleClass="labelBold"
			            value="Selected Value:" /> 
			        <h:inputText 
			            id="selected_literal" 
			            required="#{slot.isRequired}"
                        disabled="true"
                        style="background-color:faebd7;color:000000"
                        value="#{slot.value}">
			        </h:inputText>
			        </h:panelGrid>
		        </rich:panel>
		        </h:panelGrid>	        
		        </rich:panel>	        
                           
                   
            </h:column>
            
        </h:dataTable>
    </rich:tab>

	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Categorization" />
	        </h:panelGroup>
	    </f:facet>
        <jsp:include page="/data/InstanceCategorizationPanel.jsp" flush="false"/>
    </rich:tab>
    </rich:tabPanel>
 </h:panelGrid>
 </a4j:form>



