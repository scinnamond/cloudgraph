
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
</head>
<body>
  <%/* begin boilerplate header */%>  
  <div class="TopToolbarDiv">
      <jsp:include page="/TopToolbar.jsp" flush="false"/>
  </div>    
  <div class="LeftNavDiv">
  <jsp:include page="/LeftTreeNav.jsp" flush="false"/>
  </div>
  <div class="TopNavDiv">
  <a4j:outputPanel id="top_nav_Panel">
      <jsp:include page="/TopNav.jsp" flush="false"/>
  </a4j:outputPanel>
  </div>
  <%/* end boilerplate header */%> 

  <%@ include file="/ajaxloading.jsp" %>
  <div class="ContentDiv">
    <rich:tabPanel id="admin_content_panel" switchType="client"
        width="98%">
        <rich:tab label="Taxonomy" title=""
            rendered="#{not empty TaxonomyEditBean.selectedTaxonomyTree}">


     <a4j:form id="taxonomy_edit_content_form">   
     
     <f:subview id="top_button_subview">
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_save_label}"
            title="#{bundle.aplsTaxonomyEdit_save_tooltip}" 
            action="#{TaxonomyEditBean.saveFromAjax}"
            reRender="taxonomy_content_panel"/>
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_export_label}"
            title="#{bundle.aplsTaxonomyEdit_export_tooltip}" 
            action="#{TaxonomyEditBean.export}"
            reRender="export_panel_form"
            oncomplete="#{rich:component('exportModalPanel')}.show()"/>
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_exit_label}"
            title="#{bundle.aplsTaxonomyEdit_exit_tooltip}" 
            action="#{TaxonomyEditBean.exit}"
            reRender="taxonomy_content_panel"/>
     </f:subview>    
     
     <h:panelGrid id="taxonomy_content_panel" width="100%" columns="1" border="0"
          rowClasses="AlignTop" columnClasses="AlignTop"> 

        <h:panelGrid id="taxonomy_body_panel" columns="3" width="100%" border="0">
            
            <h:panelGrid columns="1" width="100%" border="0">
                <f:subview id="taxonomy_tree"
                    rendered="#{not empty TaxonomyEditBean.selectedTaxonomyTree}">
                    <f:verbatim><div class="EditableTaxonomyTreePanel"></f:verbatim>
						<rich:tree id="taxonomyTree" 
						    componentState="#{TaxonomyEditBean.selectedTaxonomyTree.treeState}"
						    switchType="ajax"
							value="#{TaxonomyEditBean.selectedTaxonomyTree.model}" 
							var="item" nodeFace="#{item.type}"
							nodeSelectListener="#{TaxonomyEditBean.categorySelectListener}">
							
							<rich:treeNode id="taxonomyNode"
							    type="taxonomy"
								iconLeaf="/images/orangedotleaf.gif" 
								icon="/images/yellow-folder-open.png"
								changeExpandListener="#{TaxonomyEditBean.selectedTaxonomyTree.processExpansion}">
							    <a4j:commandLink value="#{item.label}"
						            styleClass="LeftNavActive" 
						            reRender="taxonomy_edit_panel"
						            action="#{item.onAction}" 
						            title="#{item.tooltip}"/>
							</rich:treeNode>
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
                                        oncomplete="#{rich:component('deleteConfirmModalPanel')}.show()">
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

     <f:subview id="bottom_button_subview"
         rendered="#{not empty TaxonomyEditBean.selectedTaxonomyTree}">
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_save_label}"
            title="#{bundle.aplsTaxonomyEdit_save_tooltip}" 
            action="#{TaxonomyEditBean.saveFromAjax}"
            reRender="taxonomy_content_panel"/>
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_export_label}"
            title="#{bundle.aplsTaxonomyEdit_export_tooltip}" 
            action="#{TaxonomyEditBean.export}"
            reRender="export_panel_form"
            oncomplete="#{rich:component('exportModalPanel')}.show()"/>
        <a4j:commandButton 
            value="#{bundle.aplsTaxonomyEdit_exit_label}"
            title="#{bundle.aplsTaxonomyEdit_exit_tooltip}" 
            action="#{TaxonomyEditBean.exit}"
            reRender="taxonomy_content_panel"/>
     </f:subview>    


     </h:panelGrid>
     </a4j:form>




        </rich:tab>
    </rich:tabPanel>
  </div>  
  
    <rich:modalPanel id="deleteConfirmModalPanel"
        width="440" height="310"
        autosized="false" resizeable="false">
        <f:facet name="header">
            <h:panelGroup>
                <h:outputText value="Delete Category Confirm?"></h:outputText>
            </h:panelGroup>
        </f:facet>
        <f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/images/modal_close.gif"
                    styleClass="hidelink" id="del_conf_close_link" />
                <rich:componentControl for="deleteConfirmModalPanel" attachTo="del_conf_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
        <h:form  id="delete_confirm_panel_form">
            <h:panelGrid columns="1" width="420px" border="0"
                cellpadding="2" cellspacing="2">  
                <h:outputText style="width: 420px; word-wrap: yes; FONT-WEIGHT: bold;" 
	                    value="The following category will be deleted and all references to the category will be either deleted or removed. In addition all sub-categories and all referenced objects as indicated in the below table will be similarly deleted. Note: the 'Save' button on the main panel must be pressed before the delete is executed. Do you with to continue?" />
		        <h:panelGrid rowClasses="FormPanelRow" 
	                columnClasses="FormLabelColumn,FormControlColumn" 
	                columns="2" width="95%" border="0">  
	                <h:outputText styleClass="labelBold" 
	                    value="#{bundle.aplsTaxonomyEdit_name_label}:" />
	                <h:outputText
	                    value="#{TaxonomyEditBean.selectedCategory.name}"/>
	                <h:outputText  
	                    styleClass="labelBold"
	                    value="#{bundle.aplsTaxonomyEdit_definition_label}:" />
	                <h:outputText
	                    value="#{TaxonomyEditBean.selectedCategory.truncatedDefinition}"/>
                </h:panelGrid>
                
                <f:subview id="link_objs_table_subview">
                    <f:verbatim><div style="HEIGHT: 100px; overflow-y: auto;"></f:verbatim>
		         	    <rich:dataTable  
		         	        style="HEIGHT: 120px; overflow-x: auto;" 			                 
			                width="100%" cellpadding="0" cellspacing="0" 
			                rowKeyVar="row"
			                var="obj" value="#{TaxonomyEditBean.deletedCatagoryGraphAsList}">	     	      	     	        
		     	            <f:facet name="header">
		     	                <h:outputText value="Linked Objects To Be Deleted" />	
		     	            </f:facet> 
			     	        <h:column>
				                <h:outputText value="#{row}" />	              
					        </h:column> 
			     	        <h:column>
			     	            <f:facet name="header">
			     	                <h:outputText value="Type" />	
			     	            </f:facet> 
				                <h:outputText value="#{obj.type.name}" />	              
					        </h:column> 
			     	        <h:column>
			     	            <f:facet name="header">
			     	                <h:outputText value="Caption" />	
			     	            </f:facet> 
				                <h:outputText value="#{obj.caption}" />	              
					        </h:column> 
			     	        <h:column>
			     	            <f:facet name="header">
			     	                <h:outputText value="Description" />	
			     	            </f:facet> 
				                <h:outputText value="#{obj.description}" />	              
					        </h:column> 
					    </rich:dataTable> 
			        <f:verbatim></div></f:verbatim>
			    </f:subview>
          
                <h:panelGrid columns="2" width="50%" border="0"
                    cellpadding="2" cellspacing="2"> 
	                <a4j:commandButton 
	                    action="#{TaxonomyEditBean.delete}"
	                    reRender="taxonomy_body_panel"
	                    onclick="Richfaces.hideModalPanel('deleteConfirmModalPanel');" value="  Delete  ">
	                </a4j:commandButton>
	                <a4j:commandButton 
	                    action="#{TaxonomyEditBean.cancelDelete}"
	                    onclick="Richfaces.hideModalPanel('deleteConfirmModalPanel');" value="  Cancel  ">
	                </a4j:commandButton>
                </h:panelGrid>
            </h:panelGrid>
        </h:form>
    </rich:modalPanel>
 
 
    <rich:modalPanel id="exportModalPanel"
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
                <rich:componentControl for="exportModalPanel" attachTo="exp_close_link"
                    operation="hide" event="onclick" />
            </h:panelGroup>
        </f:facet>
        <h:form  id="export_panel_form">
            <h:panelGrid columns="1" width="400px" border="0"
                cellpadding="2" cellspacing="2">  
                <f:subview id="export_text_subview">
                    <f:verbatim><div style="border-width: 1px; border-style: inset; width: 400px; height: 240px; overflow: auto; text-align: left;"></f:verbatim>
                    <h:outputText 
	                    value="#{TaxonomyEditBean.exportXML}" />
                    <f:verbatim></div></f:verbatim>
                </f:subview>

                <h:panelGrid columns="2" width="50%" border="0"
                    cellpadding="2" cellspacing="2"> 
	                <a4j:commandButton 
	                    onclick="Richfaces.hideModalPanel('exportModalPanel');" value="  Ok  ">
	                </a4j:commandButton>
                </h:panelGrid>


            </h:panelGrid>
        </h:form>
    </rich:modalPanel>
   
</body>
</html>
</f:view>
