<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:panelGrid id="catz_panel" columns="1" width="100%" border="0">
    <h:dataTable id="class_tax_table" width="95%" border="0"
        var="tax" value="#{ClassEditBean.taxonomies}">
 	    <h:column>
 	        <rich:dataTable 
 	            id="tax_sub_table"
                onRowMouseOver="this.style.backgroundColor='#F1F1F1'"
                onRowMouseOut="this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'"
	            cellpadding="0" cellspacing="0" 
	            width="95%" border="0" 
	            var="categorization" value="#{tax.categorizations}">
	          <f:facet name="header">
	              <h:outputText value="#{tax.name} (#{tax.version})" />
	          </f:facet>  
              <rich:column>
                  <f:facet name="header">
                      <h:outputText value="Code" />
                  </f:facet>  
                  <h:outputText value="#{categorization.parentCategoryId}:#{categorization.categoryId}" />                 
              </rich:column>
              <rich:column>
                  <f:facet name="header">
                      <h:outputText value="Parent Category Name" />
                  </f:facet>  
                  <h:outputText value="#{categorization.parentCategoryName}" />                 
              </rich:column>
	 	      <rich:column>
	              <f:facet name="header">
	                  <h:outputText value="Category Name" />
	              </f:facet>  
	              <h:outputText value="#{categorization.categoryName}" />	              
		      </rich:column>
	          <rich:column
	              rendered="true">
                  <f:facet name="header">
                      <h:outputText value="Actions" />
                  </f:facet>  
                  <a4j:commandLink id="edit_catz_link" 
                      value="edit"
                      action="#{CategorizationEditBean.edit}"        
                      oncomplete="#{rich:component('createEditCategorizationPanel')}.show()"
                      reRender="createEditCategorizationForm">
                      <f:setPropertyActionListener value="#{ClassEditBean.clazzDataObject}"   
                            target="#{CategorizationEditBean.target}" />                                           
                      <f:setPropertyActionListener value="#{categorization}"   
                            target="#{CategorizationEditBean.categorization}" />                                              
                      <f:setPropertyActionListener value="#{tax}"   
                            target="#{CategorizationEditBean.taxonomy}" />                                           
                      <f:setPropertyActionListener value="class_tax_table"   
                            target="#{CategorizationEditBean.saveActionReRender}" />                                            
                  </a4j:commandLink>    
                  <f:verbatim>&nbsp</f:verbatim>
                  <a4j:commandLink id="delete_catz_link" 
                      value="delete"
                      action="#{CategorizationEditBean.delete}"      
                      reRender="class_tax_table">
                      <f:setPropertyActionListener value="#{ClassEditBean.clazzDataObject}"   
                            target="#{CategorizationEditBean.target}" />                                           
                      <f:setPropertyActionListener value="#{categorization}"   
                            target="#{CategorizationEditBean.categorization}" />                                              
                      <f:setPropertyActionListener value="#{tax}"   
                            target="#{CategorizationEditBean.taxonomy}" />                                           
                  </a4j:commandLink>    
              </rich:column>
	 	      <rich:column>
	              <f:facet name="header">
	                  <h:outputText value="Category Definition" />
	              </f:facet>  
	              <h:outputText value="#{categorization.categoryDefinition}" />	              
		      </rich:column>
	       </rich:dataTable>
	       
		   <h:panelGrid columns="2" width="10%" border="0">
               <a4j:commandButton 
                    rendered="true"
				    value="#{bundle.aplsPropertyEdit_addCategorization_label}"
				    title="#{bundle.aplsPropertyEdit_addCategorization_tooltip}" 
				    action="#{CategorizationEditBean.create}"	    
			        oncomplete="#{rich:component('createEditCategorizationPanel')}.show()"
			        reRender="createEditCategorizationForm">
			        <f:setPropertyActionListener value="#{ClassEditBean.clazzDataObject}"   
				            target="#{CategorizationEditBean.target}" />                      	                    
                    <f:setPropertyActionListener value="#{tax}"   
                            target="#{CategorizationEditBean.taxonomy}" />                                           
			        <f:setPropertyActionListener value="class_tax_table"   
				            target="#{CategorizationEditBean.saveActionReRender}" />                      	                    
				</a4j:commandButton>    
		   </h:panelGrid>
           <f:verbatim><p></p></f:verbatim>   
	    </h:column>

    </h:dataTable>   
    
</h:panelGrid> 	

