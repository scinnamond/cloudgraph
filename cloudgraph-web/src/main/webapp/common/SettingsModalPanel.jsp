<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<rich:modalPanel id="settingsModalPanel"
    autosized="true" resizeable="false">
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="Edit Settings"></h:outputText>
        </h:panelGroup>
    </f:facet>
    <f:facet name="controls">
        <h:panelGroup>
            <h:graphicImage value="/images/close_window.gif"
                styleClass="hidelink" id="settings_queue_close_link" />
            <rich:componentControl for="settingsModalPanel" 
                attachTo="settings_queue_close_link"
                operation="hide" event="onclick" />
        </h:panelGroup>
    </f:facet>
    <a4j:form  id="settings_modalPanel1_form" 
        ajaxSubmit="true" 
        reRender="dashboard_content_panel">
        <h:panelGrid columns="1" width="95%" 
            cellpadding="2" cellspacing="2">  
            <a4j:outputPanel ajaxRendered="true" id="catz_errors">
                <h:messages id="dataEntryError" showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
            </a4j:outputPanel>
                <h:panelGrid columns="2" width="80%" 
                    columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn">
                    <h:outputText value="Username:" />
                    <h:inputText value="#{UserBean.username}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="Password:" />
                    <h:inputText value="#{UserBean.password}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="First Name:" />
                    <h:inputText value="#{UserBean.firstName}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="Last Name:" />
                    <h:inputText value="#{UserBean.lastName}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="Email Address:" />
                    <h:inputText value="#{UserBean.emailAddress}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                </h:panelGrid>          
            <f:verbatim>&nbsp</f:verbatim>
            <f:verbatim>&nbsp</f:verbatim>
            <h:panelGrid columns="2" width="80%">
            <a4j:commandButton 
                action="#{UserBean.commitProfile}"
                reRender="dashboard_content_panel"
                onclick="Richfaces.hideModalPanel('settingsModalPanel');" value="  OK  ">
            </a4j:commandButton>
            <a4j:commandButton id="settingsPanel_cancel_button" value="Cancel"
                immediate="true"
                action="#{UserBean.cancelCommitProfile}"
                onclick="Richfaces.hideModalPanel('settingsModalPanel');">
            </a4j:commandButton> 
            </h:panelGrid>
    </h:panelGrid>
    </a4j:form>
</rich:modalPanel>
<script type="text/javascript">
  //<![CDATA[
     function closeSettingsPanel(){
          if (document.getElementById('settings_modalPanel1_form:dataEntryError')==null){
               Richfaces.hideModalPanel('settingsModalPanel');
          };
     };
  //\]\]\>
</script>	        
  	