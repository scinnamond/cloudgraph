<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

 <a4j:form id="taxonomymap_edit_content_form">   
 
 
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
    <a4j:commandButton 
        value="#{bundle.aplsTaxonomyMapEdit_save_label}"
        title="#{bundle.aplsTaxonomyMapEdit_save_tooltip}" 
        action="#{TaxonomyMapEditBean.saveFromAjax}"
        reRender="taxonomymap_content_panel"/>
    <a4j:commandButton 
        value="#{bundle.aplsTaxonomyMapEdit_add_label}"
        title="#{bundle.aplsTaxonomyMapEdit_add_tooltip}" 
        action="#{TaxonomyMapEditBean.add}"
        reRender="create_edit_cat_link_panel_form"
        oncomplete="#{rich:component('createEditCategoryLinkModalPanel')}.show()"/>
</h:panelGrid>   
 
 
 <h:panelGrid id="taxonomymap_content_panel" width="100%" columns="1" border="0"
      rowClasses="AlignTop" columnClasses="AlignTop"> 
            
    <f:subview id="cat_links_table_subview">
        <f:verbatim><div style="HEIGHT: 200px; overflow-y: auto;"></f:verbatim>
     	    <rich:dataTable 
                width="100%" cellpadding="0" cellspacing="0" 
                rowKeyVar="row"
                var="link" value="#{TaxonomyMapEditBean.mapElements}">	     	      	     	        
 	            <f:facet name="header">
 	                <h:outputText value="#{TaxonomyMapEditBean.selectedTaxonomyMap.name}" />	
 	            </f:facet> 
     	        <h:column>
	                <h:outputText value="#{row + 1}" />	              
		        </h:column> 
     	        <h:column>
     	            <f:facet name="header">
     	                <h:outputText value="Left Category" title="#{link.leftTooltip}"/>	
     	            </f:facet> 
	                <h:outputText value="#{link.leftCaption}" />	              
		        </h:column> 
     	        <h:column>
     	            <f:facet name="header">
     	                <h:outputText value="Right Category" title="#{link.rightTooltip}"/>	
     	            </f:facet> 
	                <h:outputText value="#{link.rightCaption}" />	              
		        </h:column>  
     	        <h:column>
     	            <f:facet name="header">
     	                <h:outputText value="Actions"/>	
     	            </f:facet> 
	                <a4j:commandLink ajaxSingle="true"
	                    oncomplete="#{rich:component('createEditCategoryLinkModalPanel')}.show()"
	                    reRender="create_edit_cat_link_panel_form">
		                <h:outputText value="edit" />
	                    <f:setPropertyActionListener value="#{link}"
	                          target="#{TaxonomyMapEditBean.selectedCategoryLink}" />
	                </a4j:commandLink>
		        </h:column>  
		    </rich:dataTable> 
        <f:verbatim></div></f:verbatim>
    </f:subview>

 </h:panelGrid>


 </a4j:form>



