<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

<rich:modalPanel id="createEditCategoryLinkModalPanel"
    width="420" height="320"
    autosized="false" resizeable="true">
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="Select Categories"></h:outputText>
        </h:panelGroup>
    </f:facet>
    <f:facet name="controls">
        <h:panelGroup>
            <h:graphicImage value="/images/modal_close.gif"
                styleClass="hidelink" id="close_link" />
            <rich:componentControl for="createEditCategoryLinkModalPanel" 
                attachTo="close_link"
                operation="hide" event="onclick" />
        </h:panelGroup>
    </f:facet>
    <h:form  id="create_edit_cat_link_panel_form">
        <f:verbatim><div style="WIDTH: 400px;"></f:verbatim>
        <h:panelGrid columns="1" border="1"
            cellpadding="2" cellspacing="2"> 
            
            <rich:tabPanel switchType="client">
                <rich:tab label="Left Category" title="">
			        <h:panelGrid id="left_tree_body_panel" 
			            columns="3" width="100%" border="0">
			            <h:panelGrid columns="1" width="100%" border="0">
			                <h:outputText styleClass="labelBold" 
			                    value="Available Values:"/>
			                <f:subview id="left_tree_sv">
			                    <f:verbatim><div class="TaxonomyTreePanel"></f:verbatim>
								<rich:tree id="left_tree"
								    switchType="ajax"
								    value="#{TaxonomyMapEditBean.leftTaxonomyTree.model}"
								    var="item"
								    nodeFace="#{item.type}"
								    componentState="#{TaxonomyMapEditBean.leftTaxonomyTree.treeState}"
								    nodeSelectListener="#{TaxonomyMapEditBean.leftCategorySelectListener}">
									
									<rich:treeNode id="left_tree_node"
								        type="level_any"
								        icon="/images/yellow-folder-open.png"
									    iconLeaf="/images/orangedotleaf.gif"
								        changeExpandListener="#{TaxonomyMapEditBean.leftTaxonomyTree.processExpansion}">       
									    <a4j:commandLink value="#{item.label}"
									        reRender="left_caption_text_panel"
								            styleClass="LeftNavActive" 
								            action="#{item.onAction}" 
								            title="#{item.tooltip}"/>
									</rich:treeNode>						
								</rich:tree>
			                    <f:verbatim></div></f:verbatim> 
			                </f:subview>
			            </h:panelGrid>
			            <f:verbatim>&nbsp</f:verbatim>
			            <h:panelGrid id="left_caption_text_panel" 
			                columns="1" width="100%" border="0">
			                <h:outputText styleClass="labelBold" 
			                    value="Selected Value:"/>
			                <f:subview id="left_tree_selection">
			                    <f:verbatim><div class="TaxonomyTreeSelectonDiv"></f:verbatim>
			                    <h:outputText 
			                        value="#{TaxonomyMapEditBean.selectedCategoryLink.leftCaption}"/>
			                    <f:verbatim></div></f:verbatim> 
			                </f:subview>
			            </h:panelGrid>
			        </h:panelGrid>
	            </rich:tab>
	            <rich:tab label="Right Category" title="">
			        <h:panelGrid id="right_tree_body_panel" 
			            columns="3" width="100%" border="0">
			            <h:panelGrid columns="1" width="100%" border="0">
			                <h:outputText styleClass="labelBold" 
			                    value="Available Values:"/>
			                <f:subview id="right_tree_sv">
			                    <f:verbatim><div class="TaxonomyTreePanel"></f:verbatim>
								<rich:tree id="right_tree"
								    switchType="ajax"
								    value="#{TaxonomyMapEditBean.rightTaxonomyTree.model}"
								    var="item"
								    nodeFace="#{item.type}"
								    componentState="#{TaxonomyMapEditBean.rightTaxonomyTree.treeState}"
								    nodeSelectListener="#{TaxonomyMapEditBean.rightCategorySelectListener}">
									
									<rich:treeNode id="right_tree_node"
								        type="level_any"
								        icon="/images/yellow-folder-open.png"
									    iconLeaf="/images/orangedotleaf.gif"
								        changeExpandListener="#{TaxonomyMapEditBean.rightTaxonomyTree.processExpansion}">       
									    <a4j:commandLink value="#{item.label}"
									        reRender="right_caption_text_panel"
								            styleClass="LeftNavActive" 
								            action="#{item.onAction}" 
								            title="#{item.tooltip}"/>
									</rich:treeNode>						
								</rich:tree>
			                    <f:verbatim></div></f:verbatim> 
			                </f:subview>
			            </h:panelGrid>
			            <f:verbatim>&nbsp</f:verbatim>
			            <h:panelGrid id="right_caption_text_panel" 
			                columns="1" width="100%" border="0">
			                <h:outputText styleClass="labelBold" 
			                    value="Selected Value:"/>
			                <f:subview id="right_tree_selection">
			                    <f:verbatim><div class="TaxonomyTreeSelectonDiv"></f:verbatim>
			                    <h:outputText 
			                        value="#{TaxonomyMapEditBean.selectedCategoryLink.rightCaption}"/>
			                    <f:verbatim></div></f:verbatim> 
			                </f:subview>
			            </h:panelGrid>
			        </h:panelGrid>
	            </rich:tab>
	        </rich:tabPanel>   
	        
	        
            <h:panelGrid columns="2" width="50%" border="0"
                cellpadding="2" cellspacing="2"> 
                <a4j:commandButton 
                    reRender="taxonomymap_content_panel"
                    onclick="Richfaces.hideModalPanel('createEditCategoryLinkModalPanel');" 
                    value="  Save  ">
                </a4j:commandButton>
                <a4j:commandButton 
                    reRender="taxonomymap_content_panel"
                    onclick="Richfaces.hideModalPanel('createEditCategoryLinkModalPanel');" 
                    value="  Cancel  ">
                </a4j:commandButton>
            </h:panelGrid>
            
        </h:panelGrid>
        <f:verbatim></div></f:verbatim>
    </h:form>
</rich:modalPanel>
   
