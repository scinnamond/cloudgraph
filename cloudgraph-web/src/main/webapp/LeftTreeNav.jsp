<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:form id="leftnav_form">


<h:panelGrid columns="1" border="0">
    <rich:toolBar height="24" itemSeparator="line">
         <rich:toolBarGroup location="right">
	         <a4j:commandLink value="Hide"
	            action="#{ControlNavigationBean.datafiltersAction.deselect}" 
	            reRender="body_panel"
	            title="Hide the data filters" />			        
          </rich:toolBarGroup>
    </rich:toolBar>

    <rich:spacer width="230" height="1"/>
    <h:panelGrid columns="2" border="0">
    
	<rich:tabPanel switchType="ajax">
	<rich:tab id="data_filters_tab" title=""
	    rendered="#{!NavigationBean.administrationAction.selected}">
	    <f:facet name="label">
	        <h:panelGroup>
	            <h:outputText value="Data Filters" />
	            <h:graphicImage value="/images/find.png"/>
	        </h:panelGroup>
	    </f:facet>	       

		<rich:simpleTogglePanel opened="true" switchType="ajax" 
			label="Catalogs" bodyClass="ParentPannelBody">
		  <h:panelGrid columns="6" width="100%" 
		      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
		      cellpadding="3" cellspacing="3" border="0" > 
		  	  <a4j:commandLink 
		          reRender="configuration_tab"
		          title="Refresh this component">
		          <h:graphicImage value="/images/refresh2_16_16.gif"/>
		          <rich:spacer width="18" height="1"/>
		      </a4j:commandLink>
		  	  <a4j:commandLink 
		          title="" 
		          rendered="#{NavigationBean.configurationAction.selected}"
		          action="#{PackageEditBean.create}"
		          reRender="dashboard_content_panel">
		          <h:graphicImage value="/images/new_item.gif"/>
		          <rich:spacer width="18" height="1"/>
                  <f:setPropertyActionListener value="true"   
                      target="#{NavigationBean.workspaceSelected}" />                                             
		      </a4j:commandLink>
		   </h:panelGrid> 	        
		   <h:dataTable value="#{SearchBean.packages}" var="pkg">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/book.png"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{pkg.name}"
			            styleClass="LeftNavActive"
			            action="#{InstanceQueueBean.clear}"
				        reRender="lefnav_classes_panel,dashboard_content_panel,attributes_panel,admin_content_panel"
				        title="#{pkg.name}">			        
			            <f:setPropertyActionListener value="#{pkg.seqId}"
		                    target="#{SearchBean.packageId}" />
		    	    </a4j:commandLink>
			    </h:column>
	            <h:column rendered="#{NavigationBean.configurationAction.selected}">
				    <a4j:commandLink value="[edit]"
				        rendered="#{NavigationBean.configurationAction.selected}"
			            styleClass="LeftNavActive"
			            action="#{PackageEditBean.edit}" 
				        reRender="attributes_panel,admin_content_panel"
				        title="#{pkg.definition}">			        
			            <f:setPropertyActionListener value="#{pkg.seqId}"
		                    target="#{PackageEditBean.packageId}" />
	                    <f:setPropertyActionListener value="true"   
	                        target="#{NavigationBean.workspaceSelected}" />                                             
		    	    </a4j:commandLink>
			    </h:column>
            </h:dataTable>
        </rich:simpleTogglePanel>

		<rich:simpleTogglePanel id="lefnav_classes_panel" opened="true" switchType="ajax" 
			label="Business Entities" bodyClass="ParentPannelBody">
		  <h:panelGrid columns="6" width="100%" 
		      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
		      cellpadding="3" cellspacing="3" border="0" > 
		  	  <a4j:commandLink 
		          reRender="configuration_tab"
		          title="Refresh this component">
		          <h:graphicImage value="/images/refresh2_16_16.gif"/>
		          <rich:spacer width="18" height="1"/>
		      </a4j:commandLink>
		  	  <a4j:commandLink 
		  	      rendered="#{NavigationBean.configurationAction.selected}"
		          title="" 
		          action="#{ClassEditBean.create}"
		          reRender="attributes_panel,admin_content_panel">
		          <h:graphicImage value="/images/new_item.gif"/>
		          <rich:spacer width="18" height="1"/>
	              <f:setPropertyActionListener value="true"   
	                  target="#{NavigationBean.workspaceSelected}" />                                             
		      </a4j:commandLink>
		   </h:panelGrid> 	        
		   <h:dataTable value="#{SearchBean.classes}" var="clss">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/orangedotleaf.gif"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{clss.classifier.name}"
			            styleClass="LeftNavActive"
			            action="#{InstanceQueueBean.clear}"
				        reRender="dashboard_content_panel,attributes_panel,admin_content_panel"
				        title="#{clss.classifier.definition}">			        
			            <f:setPropertyActionListener value="#{clss.seqId}"
		                    target="#{SearchBean.clazzId}" />
		    	    </a4j:commandLink>
			    </h:column>
	            <h:column rendered="#{NavigationBean.configurationAction.selected}">
				    <a4j:commandLink value="[edit]"
			            styleClass="LeftNavActive"
			            action="#{ClassEditBean.edit}" 
				        reRender="attributes_panel,admin_content_panel">			        
			            <f:setPropertyActionListener value="#{clss.seqId}"
		                    target="#{ClassEditBean.clazzId}" />
	                    <f:setPropertyActionListener value="true"   
	                        target="#{NavigationBean.workspaceSelected}" />                                             
		    	    </a4j:commandLink>
			    </h:column>
            </h:dataTable>
        </rich:simpleTogglePanel>

	    <rich:simpleTogglePanel opened="true" switchType="client" 
			label="Organizational Filters"
			bodyClass="ParentPannelBody">  
	
			<rich:simpleTogglePanel opened="true" switchType="client" id="org_panel"
				label="FS Orgs" 
				headerClass="ChildPannelHeader">
				<f:verbatim><div class="NavTreeDiv"></f:verbatim>
				<rich:tree id="leftNavTree2" switchType="ajax"
					value="#{OrganizationTreeBean.model}" 
					var="item" 
					nodeFace="#{item.type}"
					componentState="#{OrganizationTreeBean.treeState}"
					nodeSelectListener="#{SearchBean.orgSelectListener}">				
					<rich:treeNode type="level_any"
						iconLeaf="/images/organization_16_16.png" icon="/images/organization_16_16.png"
						changeExpandListener="#{OrganizationTreeBean.processExpansion}">
						
		                <h:outputText value="#{item.label}"
		                    styleClass="LeftNav" 
		                    title="#{item.tooltip}" 
		                    rendered="#{!item.enabled}"/>
						<f:verbatim><div class="LeftNavTreeCellDiv"></f:verbatim>
					    <a4j:commandLink value="#{item.label}"
		                    styleClass="LeftNavActive" 		        
					        reRender="dashboard_content_panel"
					        title="#{item.tooltip}" 
					        rendered="#{item.enabled}"/>
	                    <f:verbatim></div></f:verbatim>				        
					</rich:treeNode>
		
				</rich:tree>
				<f:verbatim></div></f:verbatim>
			</rich:simpleTogglePanel>
		</rich:simpleTogglePanel>	
	</rich:tab>
	



  
    
	<rich:tab label="Reference Data" title=""
	    rendered="#{NavigationBean.administrationAction.selected}">
	
		<rich:simpleTogglePanel id="tax_panel" opened="true" switchType="client" 
			label="Taxonomies">
	
	        <h:dataTable id="taxon1_table" value="#{TaxonomyEditBean.taxonomies}" 
	            var="tax">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/orangedotleaf.gif"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{tax.category.name}"
			            styleClass="LeftNavActive" 
				        reRender="admin_content_panel"
				        title="#{tax.category.definition}">			        
			                <f:setPropertyActionListener value="#{tax}"
		                          target="#{TaxonomyEditBean.selectedTaxonomy}" />
		            </a4j:commandLink>
			    </h:column>
	        </h:dataTable>
		</rich:simpleTogglePanel>
		<rich:simpleTogglePanel id="maps_panel" opened="true" switchType="client" 
			label="Taxonomy Maps">
			
	        <h:dataTable id="taxmap1_table" value="#{TaxonomyMapEditBean.taxonomyMaps}" 
	            var="taxmap">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/orangedotleaf.gif"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{taxmap.name}"
			            styleClass="LeftNavActive" 
				        reRender="admin_content_panel">			        
			                <f:setPropertyActionListener value="#{taxmap}"
		                          target="#{TaxonomyMapEditBean.selectedTaxonomyMap}" />
		            </a4j:commandLink>
			    </h:column>
	        </h:dataTable>
	
		</rich:simpleTogglePanel>
	
	
	</rich:tab>
	
	</rich:tabPanel>
	</h:panelGrid>
</h:panelGrid>
</h:form>

