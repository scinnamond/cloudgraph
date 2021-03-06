<!DOCTYPE html [
    <!ENTITY nbsp "&#160;"> 
]>
<html lang="en"
     xmlns="http://www.w3.org/1999/xhtml"
     xmlns:f="http://java.sun.com/jsf/core"
     xmlns:ui="http://java.sun.com/jsf/facelets"
     xmlns:h="http://java.sun.com/jsf/html"
     xmlns:p="http://primefaces.org/ui"
     xmlns:c="http://java.sun.com/jsp/jstl/core">
<body>
<ui:composition>
 <h:form id="taxonomy_edit_content_form">   
 
    
  <h:panelGrid columns="6" width="100%" 
      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
      cellpadding="3" cellspacing="3" border="0" > 
  	  <p:commandLink 
          reRender="admin_content_panel"
          title="Refresh this list">
          <h:graphicImage value="/images/refresh2_16_16.gif"/>
          <p:spacer width="18" height="1"/>
      </p:commandLink>
  	  <p:commandLink 
          title="">
          <h:graphicImage value="/images/new_item.gif"/>
          <p:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.administrationSelected}" />
      </p:commandLink>
      <p:commandLink 
        value="#{bundle.aplsTaxonomyEdit_save_label}"
        title="#{bundle.aplsTaxonomyEdit_save_tooltip}" 
        action="#{TaxonomyEditBean.saveFromAjax}"
        reRender="taxonomy_content_panel"/>
      <p:commandLink 
        value="#{bundle.aplsTaxonomyEdit_export_label}"
        title="#{bundle.aplsTaxonomyEdit_export_tooltip}" 
        action="#{TaxonomyEditBean.export}"
        reRender="export_panel_form"
        oncomplete="#{p:component('exportTaxonomyModalPanel')}.show()"/>
      <p:commandLink 
        value="#{bundle.aplsTaxonomyEdit_exit_label}"
        title="#{bundle.aplsTaxonomyEdit_exit_tooltip}" 
        action="#{TaxonomyEditBean.exit}"
        reRender="dashboard_content_panel"/>
   </h:panelGrid>   
 
 <h:panelGrid id="taxonomy_content_panel" width="100%" columns="1" border="0"
      rowClasses="AlignTop" columnClasses="AlignTop"> 

    <h:panelGrid id="taxonomy_body_panel" columns="3" width="100%" border="0">
        
        <h:panelGrid columns="1" width="100%" border="0">
            <h:outputText styleClass="labelBold" 
                value="Taxonomy: #{TaxonomyEditBean.selectedTaxonomy.category.name} Version: #{TaxonomyEditBean.selectedTaxonomy.version}"/>
            <f:subview id="taxonomy_tree"
                rendered="#{not empty TaxonomyEditBean.selectedTaxonomyTree}">
                <f:verbatim><div class="EditableTaxonomyTreePanel"></f:verbatim>
					<p:tree id="taxonomyTree" 
					    componentState="#{TaxonomyEditBean.selectedTaxonomyTree.treeState}"
					    switchType="ajax"
						value="#{TaxonomyEditBean.selectedTaxonomyTree.model}" 
						var="item" nodeFace="#{item.type}"
						nodeSelectListener="#{TaxonomyEditBean.categorySelectListener}">

						<p:treeNode id="categoryNode"
						    type="category"
							iconLeaf="/images/orangedotleaf.gif" icon="/images/yellow-folder-open.png"
							changeExpandListener="#{TaxonomyEditBean.selectedTaxonomyTree.processExpansion}">
						    <p:commandLink id="tree_node_link" value="#{item.label}"
					            styleClass="LeftNavActive" 
					            reRender="taxonomy_edit_panel"
					            action="#{item.onAction}" 
					            title="#{item.tooltip}"/>
			                <p:contextMenu event="oncontextmenu" attachTo="tree_node_link" submitMode="ajax">
                                <p:menuItem value="Add Child" id="add_child"
                                    action="#{TaxonomyEditBean.addChild}"
                                    reRender="taxonomy_body_panel">
                                    <p:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </p:menuItem>
                                <p:menuItem value="Add Sibling" id="add_sib"
                                    action="#{TaxonomyEditBean.addSibling}"
                                    reRender="taxonomy_body_panel">
                                    <p:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </p:menuItem>
                                <p:menuItem value="Delete" id="delete"
                                    action="#{TaxonomyEditBean.confirmDelete}"
                                    reRender="delete_confirm_panel_form"
                                    oncomplete="#{p:component('deleteCategoryConfirmModalPanel')}.show()">
                                    <p:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </p:menuItem>
                            </p:contextMenu>		            
					            
						</p:treeNode>
					
					</p:tree>
						
						
                <f:verbatim></div></f:verbatim> 
            </f:subview>
        </h:panelGrid>
        <f:verbatim>&nbsp</f:verbatim>
        <h:panelGrid id="taxonomy_edit_panel" 
            columns="1" width="100%" border="0">                            
            <h:outputText styleClass="labelBold" 
                value="Selected Category Detail:"
                rendered="#{not empty TaxonomyEditBean.selectedCategory}"/>
            <f:subview id="tax_edit_subview"
                rendered="#{not empty TaxonomyEditBean.selectedCategory}">
                <f:verbatim><div class="TaxonomyTreeSelectonDiv"></f:verbatim>

	            <h:panelGrid rowClasses="FormPanelRow" 
	                columnClasses="FormLabelColumn,FormControlColumn,FormMessageColumn" 
	                columns="3" width="95%" border="0">  
	                <h:outputText styleClass="labelBold" 
	                    value="#{bundle.aplsTaxonomyEdit_name_label}:" 
	                    title="#{TaxonomyEditBean.selectedCategory.nameTooltip}"/>
	                <h:inputText id="aplsTaxonomyEdit_name"
	                    required="true"
	                    value="#{TaxonomyEditBean.selectedCategory.name}"
	                    disabled="false"
	                    title="#{TaxonomyEditBean.selectedCategory.nameTooltip}">
	                </h:inputText> 
	                <h:message for="aplsTaxonomyEdit_name" showDetail="false" showSummary="true"/>             
	                <h:outputText styleClass="labelBold" 
	                    value="#{bundle.aplsTaxonomyEdit_id_label}:" 
	                    title="#{TaxonomyEditBean.selectedCategory.idTooltip}"/>
	                <h:inputText id="aplsTaxonomyEdit_id"
	                    size="6"
	                    required="true"
	                    value="#{TaxonomyEditBean.selectedCategory.id}"
	                    disabled="false"
	                    title="#{TaxonomyEditBean.selectedCategory.idTooltip}">
	                    <f:validateLongRange minimum="1" maximum="999999"/>
	                </h:inputText>
	                <h:message for="aplsTaxonomyEdit_id" showDetail="false" showSummary="true"/>              
	            </h:panelGrid>
	            <h:panelGrid  columns="2" width="95%" border="0"
	                columnClasses="AlignLeft,FormMessageColumn">
		            <h:panelGrid  
		                columns="1" width="100%" border="0">  
		                <h:outputText styleClass="labelBold" 
		                    value="#{bundle.aplsTaxonomyEdit_definition_label}:" 
		                    title="#{TaxonomyEditBean.selectedCategory.definitionTooltip}"/>
		                <h:inputTextarea id="aplsTaxonomyEdit_definition"
		                    cols="26" rows="6"
		                    required="true"
		                    value="#{TaxonomyEditBean.selectedCategory.definition}"
		                    disabled="false"
		                    title="#{TaxonomyEditBean.selectedCategory.definitionTooltip}">
		                </h:inputTextarea>              
		            </h:panelGrid>
		            <h:message for="aplsTaxonomyEdit_definition" showDetail="false" showSummary="true"/>
	            </h:panelGrid>
                <f:verbatim></div></f:verbatim> 
            </f:subview>
        </h:panelGrid>
    </h:panelGrid>


 </h:panelGrid>
 </h:form>
</ui:composition>
</body>
</html>


