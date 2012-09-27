<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

<rich:modalPanel id="deleteCategoryConfirmModalPanel"
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
            <rich:componentControl for="deleteCategoryConfirmModalPanel" attachTo="del_conf_close_link"
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
                    onclick="Richfaces.hideModalPanel('deleteCategoryConfirmModalPanel');" value="  Delete  ">
                </a4j:commandButton>
                <a4j:commandButton 
                    action="#{TaxonomyEditBean.cancelDelete}"
                    onclick="Richfaces.hideModalPanel('deleteCategoryConfirmModalPanel');" value="  Cancel  ">
                </a4j:commandButton>
            </h:panelGrid>
        </h:panelGrid>
    </h:form>
</rich:modalPanel>
