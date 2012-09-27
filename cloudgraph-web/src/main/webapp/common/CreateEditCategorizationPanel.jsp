<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

  <rich:modalPanel id="createEditCategorizationPanel" 
      width="340" height="290" resizeable="true">
        <f:facet name="header">
            <h:outputText value="Create/Edit Categorization" 
                style="padding-right:15px;"/>
        </f:facet>
        <f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/images/close_window.gif"
                    styleClass="hidelink" id="catz_panel_close_link" />
                <rich:componentControl for="createEditCategorizationPanel" 
                    attachTo="catz_panel_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
        <h:form id="createEditCategorizationForm">         

        <h:panelGrid columns="1" width="100%" border="0">
			<a4j:outputPanel ajaxRendered="true" id="catz_errors">
				<h:messages id="dataEntryError" showDetail="true" showSummary="false" layout="table" errorClass="ErrorMessage" infoClass="ErrorMessage"/>
			</a4j:outputPanel>
            <h:panelGrid columns="1" width="100%" border="0">
                <f:subview id="catz_status_sv">
                    <h:outputText styleClass="labelBold" 
                        value="Available Values: "/>
                    <a4j:status id="catz_panel_status" forceId="true"
                        startText=" loading..." stopText="">
                        <%/*
                        <f:facet name="start">
                            <h:graphicImage value="/images/ajax_loader.gif"/>
                        </f:facet>
                        */%>
                    </a4j:status>
                </f:subview>
                <f:subview id="tree_sv">
                    <f:verbatim><div class="TaxonomyTreePanel"></f:verbatim>
                    <rich:tree id="taxTree" 
                        switchType="ajax"
                        status="catz_panel_status"
                        value="#{CategorizationEditBean.model}" 
                        ajaxKeys="#{CategorizationEditBean.ajaxKeys}"
                        var="item" nodeFace="#{item.type}"
                        componentState="#{CategorizationEditBean.treeState}"
                        nodeSelectListener="#{CategorizationEditBean.categorySelectedListener}">
                        
                        <rich:treeNode id="treeNodeAny"
                            type="level_any"
                            iconLeaf="/images/orangedotleaf.gif" icon="/images/yellow-folder-open.png"
                            changeExpandListener="#{CategorizationEditBean.processExpansion}">
                            <a4j:commandLink value="#{item.label}"
                                styleClass="LeftNavActive" 
                                ajaxSingle="false" 
                                reRender="catz_btn_pannel"
                                title="#{item.tooltip}">
                            </a4j:commandLink>
                        </rich:treeNode>
                    </rich:tree>
                    <f:verbatim></div></f:verbatim> 
                </f:subview>
            </h:panelGrid>
            <h:panelGrid id="catz_btn_pannel" columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton id="createEditCategorizationPanel_select_button" 
                    value="    Select    "
                    disabled="#{!CategorizationEditBean.categorySelected}" 
                    ajaxSingle="false"
                    action="#{CategorizationEditBean.save}"
                    reRender="createEditCategorizationForm,#{CategorizationEditBean.saveActionReRender}"
                    oncomplete="javascript:closeCategorizationPanel()">
                </a4j:commandButton> 
                <a4j:commandButton id="createEditCategorizationPanel_cancel_button" value="Cancel"
                    immediate="true"
                    action="#{CategorizationEditBean.cancel}"
                    onclick="Richfaces.hideModalPanel('createEditCategorizationPanel');">
                </a4j:commandButton> 
            </h:panelGrid>
        </h:panelGrid>
        </h:form>
  </rich:modalPanel>
  <script type="text/javascript">
    //<![CDATA[
       function closeCategorizationPanel(){
            if (document.getElementById('createEditCategorizationForm:dataEntryError')==null){
                 Richfaces.hideModalPanel('createEditCategorizationPanel');
            };
       };
    //\]\]\>
 </script>	        
  	