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
    <%/* 
    <f:facet name="controls">
        <h:graphicImage value="/apls/images/close.png" style="cursor:pointer" onclick="Richfaces.hideModalPanel('settingsModalPanel')" />
    </f:facet> 
    */%>         
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
                    <h:outputText value="#{UserBean.name}" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    <h:outputText value="First Name:" />
                    <h:outputText value="Last Name:" />
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                </h:panelGrid>          
            <f:verbatim>&nbsp</f:verbatim>
            <f:verbatim>&nbsp</f:verbatim>
            <a4j:commandButton 
                action="#{UserBean.commitProfile}"
                reRender="dashboard_content_panel"
                onclick="Richfaces.hideModalPanel('settingsModalPanel');" value="  OK  ">
            </a4j:commandButton>
    </h:panelGrid>
    </a4j:form>
</rich:modalPanel>
<script type="text/javascript">
  //<![CDATA[
     function closeCategorizationPanel(){
          if (document.getElementById('settings_modalPanel1_form:dataEntryError')==null){
               Richfaces.hideModalPanel('settingsModalPanel');
          };
     };
  //\]\]\>
</script>	        
  	