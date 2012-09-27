<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="campaign_edit_content_form">   
 
 <h:panelGrid id="campaign_content_panel" width="100%" columns="1" border="0"
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
        value="#{bundle.aplsCampaignEdit_save_label}"
        title="#{bundle.aplsCampaignEdit_save_tooltip}" 
        action="#{CampaignEditBean.saveFromAjax}"
        reRender="campaign_content_panel"/>
      <a4j:commandLink 
        value="#{bundle.aplsCampaignEdit_exit_label}"
        title="#{bundle.aplsCampaignEdit_exit_tooltip}" 
        action="#{CampaignEditBean.exit}"
        reRender="dashboard_content_panel"/>
   </h:panelGrid>   
	
	<a4j:outputPanel ajaxRendered="true" id="campaign_errors">
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
	            value="#{bundle.aplsCampaignEdit_name_label}:" 
	            title="#{bundle.aplsCampaignEdit_name_tooltip}"/>
	        <h:inputText id="aplsCampaignEdit_name"
	            required="true"
	            maxlength="#{CampaignEditBean.nameMaxLength}"
	            value="#{CampaignEditBean.campaign.name}"
	            title="#{bundle.aplsCampaignEdit_name_tooltip}">
	        </h:inputText>                
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
            <h:outputText  
                value="#{bundle.aplsCampaignEdit_type_label}:" 
                title="#{bundle.aplsCampaignEdit_type_tooltip}"/>
	        <h:selectOneMenu id="aplsCampaignEdit_type"
	            required="true"
	            value="#{CampaignEditBean.campaign.campaignType}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems value="#{CampaignEditBean.typeItems}" />
                <rich:toolTip value="#{bundle.aplsCampaignEdit_type_tooltip}"/>
	        </h:selectOneMenu>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
            <h:outputText  
                value="#{bundle.aplsCampaignEdit_dispersal_label}:" 
                title="#{bundle.aplsCampaignEdit_dispersal_tooltip}"/>
	        <h:selectOneMenu id="aplsCampaignEdit_dispersal"
	            required="true"
	            value="#{CampaignEditBean.campaign.dispersalType}"
	            disabled="false"
	            disabledClass="color:gray">
	            <f:selectItems value="#{CampaignEditBean.dispersalMethodItems}" />
                <rich:toolTip value="#{bundle.aplsCampaignEdit_dispersal_tooltip}"/>
	        </h:selectOneMenu>
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsCampaignEdit_description_label}:" 
	            title="#{bundle.aplsCampaignEdit_description_tooltip}"/>                             
	        <rich:editor 
	            width="280" height="100"
	            viewMode="visual" 
	            readonly="false"
	            value="#{CampaignEditBean.campaign.description}" 
	            validator="#{CampaignEditBean.validateDescriptionLength}"
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
	            value="#{bundle.aplsCampaignEdit_startDate_label}:" 
	            title="#{bundle.aplsCampaignEdit_startDate_tooltip}"/>                             
            <a4j:outputPanel layout="block"
                title="">
                <rich:calendar value="#{CampaignEditBean.campaign.startDate}"
                    datePattern="d/MMM/yy"
                    cellWidth="24px" cellHeight="22px" 
                    style="width:200px"/>
                <rich:toolTip value="#{bundle.aplsCampaignEdit_startDate_tooltip}"/>
            </a4j:outputPanel>	            
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
 	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsCampaignEdit_endDate_label}:" 
	            title="#{bundle.aplsCampaignEdit_endDate_tooltip}"/>                             
            <a4j:outputPanel layout="block"
                title="">
                <rich:calendar value="#{CampaignEditBean.campaign.endDate}"
                    datePattern="d/MMM/yy"
                    cellWidth="24px" cellHeight="22px" 
                    style="width:200px"/>
                <rich:toolTip value="#{bundle.aplsCampaignEdit_endDate_tooltip}"/>
            </a4j:outputPanel>	            
	        <f:verbatim>&nbsp</f:verbatim>    
	        <f:verbatim>&nbsp</f:verbatim>
	        <h:outputText styleClass="labelBold" 
	            value="#{bundle.aplsCampaignEdit_notes_label}:" 
	            title="#{bundle.aplsCampaignEdit_notes_tooltip}"/>                             
	        <rich:editor 
	            width="280" height="100"
	            viewMode="visual" 
	            readonly="false"
	            value="#{CampaignEditBean.campaign.notes}" 
	            validator="#{CampaignEditBean.validateNotesLength}"
	            validatorMessage="Notes text is longer than allowed maximum characters"
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



	    </h:panelGrid>
    </rich:tab>
	<rich:tab title=""
	    rendered="true">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Attributes" />
	        </h:panelGroup>
	    </f:facet>
        <h:panelGrid columns="1" width="100%" border="0">

            <rich:listShuttle id="propListShuttle" 
                sourceValue="#{CampaignEditBean.availableProperties}" 
                targetValue="#{CampaignEditBean.selectedProperties}"  
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
        </h:panelGrid>
    </rich:tab>

    </rich:tabPanel>
 </h:panelGrid>
 </a4j:form>



