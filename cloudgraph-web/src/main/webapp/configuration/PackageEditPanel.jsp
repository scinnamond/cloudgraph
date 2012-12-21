<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="package_edit_content_form">   
 
 <h:panelGrid id="package_content_panel" width="100%" columns="1" border="0"
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
          action="#{PackageEditBean.createFromAjax}"
          reRender="package_content_panel">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
      </a4j:commandLink>
      <a4j:commandLink 
        value="#{bundle.aplsPackageEdit_save_label}"
        title="#{bundle.aplsPackageEdit_save_tooltip}" 
        action="#{PackageEditBean.saveFromAjax}"
        reRender="package_content_panel,admin_content_panel,configuration_tab"/>
      <a4j:commandLink 
        value="#{bundle.aplsPackageEdit_exit_label}"
        title="#{bundle.aplsPackageEdit_exit_tooltip}" 
        action="#{PackageEditBean.exit}"
        reRender="dashboard_content_panel"/>
   </h:panelGrid>   
	
	<a4j:outputPanel ajaxRendered="true" id="class_errors">
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

	    <h:panelGrid id="basic_panel" rowClasses="FormPanelRow" 
	        columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn" 
	        columns="4" width="95%" border="0">  
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsPackageEdit_name_label}:" 
	            title="#{bundle.aplsPackageEdit_name_tooltip}"/>
	        <h:inputText id="aplsPackageEdit_name"
	            required="true"
	            maxlength="#{PackageEditBean.nameMaxLength}"
	            value="#{PackageEditBean.package.name}"
	            title="#{bundle.aplsPackageEdit_name_tooltip}">
	        </h:inputText>                
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsPackageEdit_definition_label}:" 
	            title="#{bundle.aplsPackageEdit_definition_tooltip}"/>                             
	        <rich:editor 
	            id="prop_defn_descr_editor"
	            width="280" height="100"
	            viewMode="visual" 
	            readonly="false"
	            value="#{PackageEditBean.package.definition}" 
	            validator="#{PackageEditBean.validateDefinitionLength}"
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
                value="#{bundle.aplsPackageEdit_parentPackage_label}:" 
                title="#{bundle.aplsPackageEdit_parentPackage_tooltip}"/>
	        <h:selectOneMenu id="aplsPackageEdit_parentPackage"
	            required="false"
	            value="#{PackageEditBean.parentPackageId}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems value="#{PackageEditBean.parentPackageItems}" />
                <rich:toolTip value="#{bundle.aplsPackageEdit_parentPackage_tooltip}"/>
	        </h:selectOneMenu>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	 
	    </h:panelGrid>
    </rich:tab>
    </rich:tabPanel>
 </h:panelGrid>
 </a4j:form>



