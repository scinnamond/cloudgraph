<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<rich:modalPanel id="contactModalPanel"
    autosized="true" resizeable="false">
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="Contact"></h:outputText>
        </h:panelGroup>
    </f:facet>
    <f:facet name="controls">
        <h:panelGroup>
            <h:graphicImage value="/images/close_window.gif"
                styleClass="hidelink" id="contact_queue_close_link" />
            <rich:componentControl for="contactModalPanel" 
                attachTo="contact_queue_close_link"
                operation="hide" event="onclick" />
        </h:panelGroup>
    </f:facet>
    <a4j:form  id="contact_modalPanel1_form" 
        ajaxSubmit="true" 
        reRender="dashboard_content_panel">
        <h:panelGrid columns="1" width="95%" 
            cellpadding="2" cellspacing="2">  
            <a4j:outputPanel ajaxRendered="true" id="catz_errors">
                <h:messages id="dataEntryError" showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
            </a4j:outputPanel>
                <h:panelGrid columns="2" width="80%" 
                    columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn">
                    <h:outputText value="Subject:" />
                    <h:inputText value="#{EmailBean.subject}" 
                        required="true"
                        validator="#{EmailBean.validateSubject}"/>
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="Message:" />
                    <h:inputTextarea value="#{EmailBean.message}" 
                        validator="#{EmailBean.validateMessage}"
                        cols="30" rows="8"/>
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                    
                    <h:outputText value="From Email Address:" />
                    <h:inputText value="#{EmailBean.emailAddress}" 
                        validator="#{EmailBean.validateEmailAddress}"/>
                    <f:verbatim>&nbsp</f:verbatim>
                    <f:verbatim>&nbsp</f:verbatim>
                </h:panelGrid>          
            <f:verbatim>&nbsp</f:verbatim>
            <f:verbatim>&nbsp</f:verbatim>
            <h:panelGrid columns="2" width="80%">
            <a4j:commandButton 
                action="#{EmailBean.send}"
                reRender="dashboard_content_panel"
                oncomplete="javascript:closeContactPanel()"
                value="  OK  ">
            </a4j:commandButton>
            <a4j:commandButton id="contactPanel_cancel_button" value="Cancel"
                immediate="true"
                action="#{EmailBean.cancel}"
                onclick="Richfaces.hideModalPanel('contactModalPanel');">
            </a4j:commandButton> 
            </h:panelGrid>
    </h:panelGrid>
    </a4j:form>
</rich:modalPanel>
<script type="text/javascript">
  //<![CDATA[
     function closeContactPanel(){
          if (document.getElementById('contact_modalPanel1_form:dataEntryError')==null){
               Richfaces.hideModalPanel('contactModalPanel');
          };
     };
  //\]\]\>
</script>           
  	