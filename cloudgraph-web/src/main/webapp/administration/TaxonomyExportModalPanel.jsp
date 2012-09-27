<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

<rich:modalPanel id="exportTaxonomyModalPanel"
    width="420" height="320"
    autosized="false" resizeable="true">
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="Export Content"></h:outputText>
        </h:panelGroup>
    </f:facet>
    <f:facet name="controls">
        <h:panelGroup>
            <h:graphicImage value="/images/modal_close.gif"
                styleClass="hidelink" id="exp_close_link" />
            <rich:componentControl for="exportTaxonomyModalPanel" attachTo="exp_close_link"
                operation="hide" event="onclick" />
        </h:panelGroup>
    </f:facet>
    <h:form  id="export_panel_form">
        <h:panelGrid columns="1" width="400px" border="0"
            cellpadding="2" cellspacing="2">  
            <f:subview id="export_text_subview">
                <f:verbatim><div style="border-width: 1px; border-style: inset; width: 400px; height: 240px; overflow: auto; text-align: left;"></f:verbatim>
                <f:verbatim><pre></f:verbatim>
                <h:outputText 
                    value="#{TaxonomyEditBean.exportXML}" />
                <f:verbatim></pre></f:verbatim>
                <f:verbatim></div></f:verbatim>
            </f:subview>
            <h:panelGrid columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton 
                    onclick="Richfaces.hideModalPanel('exportTaxonomyModalPanel');" value="  Ok  ">
                </a4j:commandButton>
            </h:panelGrid>
        </h:panelGrid>
    </h:form>
</rich:modalPanel>
   
