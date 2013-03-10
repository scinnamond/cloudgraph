<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="taxonomy_edit_content_form">   
 
    
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
          title="">
          <h:graphicImage value="/images/new_item.gif"/>
          <rich:spacer width="18" height="1"/>
	      <f:setPropertyActionListener value="true"   
		      target="#{NavigationBean.administrationSelected}" />
      </a4j:commandLink>
      <a4j:commandLink 
        value="#{bundle.aplsTaxonomyEdit_save_label}"
        title="#{bundle.aplsTaxonomyEdit_save_tooltip}" 
        action="#{TaxonomyEditBean.saveFromAjax}"
        reRender="taxonomy_content_panel"/>
      <a4j:commandLink 
        value="#{bundle.aplsTaxonomyEdit_export_label}"
        title="#{bundle.aplsTaxonomyEdit_export_tooltip}" 
        action="#{TaxonomyEditBean.export}"
        reRender="export_panel_form"
        oncomplete="#{rich:component('exportTaxonomyModalPanel')}.show()"/>
      <a4j:commandLink 
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
					<rich:tree id="taxonomyTree" 
					    componentState="#{TaxonomyEditBean.selectedTaxonomyTree.treeState}"
					    switchType="ajax"
						value="#{TaxonomyEditBean.selectedTaxonomyTree.model}" 
						var="item" nodeFace="#{item.type}"
						nodeSelectListener="#{TaxonomyEditBean.categorySelectListener}">

						<rich:treeNode id="categoryNode"
						    type="category"
							iconLeaf="/images/orangedotleaf.gif" icon="/images/yellow-folder-open.png"
							changeExpandListener="#{TaxonomyEditBean.selectedTaxonomyTree.processExpansion}">
						    <a4j:commandLink id="tree_node_link" value="#{item.label}"
					            styleClass="LeftNavActive" 
					            reRender="taxonomy_edit_panel"
					            action="#{item.onAction}" 
					            title="#{item.tooltip}"/>
			                <rich:contextMenu event="oncontextmenu" attachTo="tree_node_link" submitMode="ajax">
                                <rich:menuItem value="Add Child" id="add_child"
                                    action="#{TaxonomyEditBean.addChild}"
                                    reRender="taxonomy_body_panel">
                                    <a4j:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </rich:menuItem>
                                <rich:menuItem value="Add Sibling" id="add_sib"
                                    action="#{TaxonomyEditBean.addSibling}"
                                    reRender="taxonomy_body_panel">
                                    <a4j:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </rich:menuItem>
                                <rich:menuItem value="Delete" id="delete"
                                    action="#{TaxonomyEditBean.confirmDelete}"
                                    reRender="delete_confirm_panel_form"
                                    oncomplete="#{rich:component('deleteCategoryConfirmModalPanel')}.show()">
                                    <a4j:actionparam name="cat_seq_id" assignTo="#{TaxonomyEditBean.selectedCategorySeqId}" value="#{item.userData.seqId}"/>
                                </rich:menuItem>
                            </rich:contextMenu>		            
					            
						</rich:treeNode>
					
					</rich:tree>
						
						
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
 </a4j:form>



