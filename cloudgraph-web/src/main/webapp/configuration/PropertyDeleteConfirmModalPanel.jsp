<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

<rich:modalPanel id="deletePropertyConfirmModalPanel"
    width="440" height="310"
    autosized="false" resizeable="false">
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="Delete Property Confirm?"></h:outputText>
        </h:panelGroup>
    </f:facet>
    <f:facet name="controls">
        <h:panelGroup>
            <h:graphicImage value="/images/modal_close.gif"
                styleClass="hidelink" id="prop_del_conf_close_link" />
            <rich:componentControl for="deletePropertyConfirmModalPanel" attachTo="prop_del_conf_close_link"
                operation="hide" event="onclick" />
        </h:panelGroup>
    </f:facet>
    <h:form  id="prop_delete_confirm_panel_form">
        <h:panelGrid columns="1" width="320px" border="0"
            cellpadding="2" cellspacing="2">  
            <h:outputText style="width: 420px; word-wrap: yes; FONT-WEIGHT: bold;" 
                    value="The following property will be deleted and all instance 'slots' as well as values. Do you with to continue?" />
	        <h:panelGrid rowClasses="FormPanelRow" 
                columnClasses="FormLabelColumn,FormControlColumn" 
                columns="2" width="95%" border="0">  
                <h:outputText styleClass="labelBold" 
                    value="#{bundle.aplsPropertyEdit_name_label}:" />
                <h:outputText
                    value="#{PropertyEditBean.property.name}"/>
            </h:panelGrid>
      
            <h:panelGrid columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton 
                    action="#{PropertyEditBean.delete}"
                    reRender="admin_content_panel"
                    onclick="Richfaces.hideModalPanel('deletePropertyConfirmModalPanel');" value="  Delete  ">
                </a4j:commandButton>
                <a4j:commandButton 
                    action="#{PropertyEditBean.cancelDelete}"
                    onclick="Richfaces.hideModalPanel('deletePropertyConfirmModalPanel');" value="  Cancel  ">
                </a4j:commandButton>
            </h:panelGrid>
        </h:panelGrid>
    </h:form>
</rich:modalPanel>
