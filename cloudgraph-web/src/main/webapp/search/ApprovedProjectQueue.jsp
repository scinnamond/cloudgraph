
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>

  <h:panelGrid rowClasses="AlignCenter" columns="1" border="0">                                                            
                                                                                                                                            
  <t:dataTable id="aprvd_prj_dtbl" var="project" value="#{ApprovedProjectQueueBean.results}"                                                                       
    preserveDataModel="true" width="100%" align="center" rows="15" preserveSort="true"                                                       
    sortColumn="#{ApprovedProjectQueueBean.sortBy}"                                                                                                     
    sortAscending="#{ApprovedProjectQueueBean.ascending}"                                                                                               
    cellpadding="2" cellspacing="2" styleClass="workQueueTable"                                                                             
    rowClasses="tableoncell,tableoffcell" headerClass="tableHeader">                                                                        

   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="year">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_year_label}" title="#{bundle.aplsProjects_year_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.budgetYear}"/>                                                                                         
   </t:column> 

   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="parentOrgName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_parentOrgName_label}" title="#{bundle.aplsProjects_parentOrgName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.parentOrgName}"/>                                                                                         
   </t:column> 

   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="orgName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_orgName_label}" title="#{bundle.aplsProjects_orgName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.orgName}"/>                                                                                         
   </t:column> 
    
   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="investmentName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_investmentName_label}" title="#{bundle.aplsProjects_investmentName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.investmentName}"/>                                                                                         
   </t:column> 
     
   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="applicationName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_applicationName_label}" title="#{bundle.aplsProjects_investmentName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.applicationName}"/>                                                                                         
   </t:column>   
   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="segmentName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_segmentName_label}" title="#{bundle.aplsProjects_segmentName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.segmentName}"/>                                                                                         
    </t:column> 
   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="projectName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_name_label}" title="#{bundle.aplsProjects_name_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.projectName}"/>                                                                                         
    </t:column> 
   <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="projectLifecycleStatus">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_lifecycleStatus_label}" title="#{bundle.aplsProjects_lifecycleStatus_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
       <h:outputText value="#{project.projectLifecycleStatusResource}"/>                                                                                         
    </t:column> 
      
   <h:column id="actionsColumn">                                                                                                                              
        <f:facet name="header">                                                                                                               
            <h:outputText value="#{bundle.aplsProjects_actions_label}"                                                                    
                title="#{bundle.aplsProjects_tooltip_label}"/>                                                                          
        </f:facet>                                                                                                                            
        <a4j:commandLink action="#{ProjectEditBean.edit}"
            title="View this project (read-only)">                                                                               
            <h:outputText value="view"                                                                   
                title="View this project (read-only)"/>                                                                         
            <f:param name="seqid" value="#{project.projectId}"/>
            <f:setPropertyActionListener value="true"   
                    target="#{NavigationBean.workspaceSelected}" />                                             
            <f:setPropertyActionListener value="true"   
                    target="#{ProjectEditBean.readOnly}" />                                             
        </a4j:commandLink>                                                                                                                    
        <f:verbatim>&nbsp</f:verbatim>                   
        <a4j:commandLink action="#{ProjectEditBean.edit}"
            title="Edit this project">                                                                               
            <h:outputText value="edit"                                                                   
                title="Edit this project"/>                                                                         
            <f:param name="seqid" value="#{project.projectId}"/>
            <f:setPropertyActionListener value="true"   
                    target="#{NavigationBean.workspaceSelected}" />                                             
            <f:setPropertyActionListener value="0"   
                    target="#{ProjectEditBean.readOnly}" />                                             
        </a4j:commandLink>                                                                                                                    
        <f:verbatim>&nbsp</f:verbatim>                   
        <a4j:commandLink action="#{ProjectEditBean.copy}"
            title="Copy then edit the copy of this project"
            rendered="#{UserBean.roleName == 'SUPERUSER'}">                                                                               
            <h:outputText value="copy"                                                                   
                title="Copy then edit the copy of this project"/>                                                                         
            <f:param name="seqid" value="#{project.projectId}"/>
            <f:setPropertyActionListener value="true"   
                    target="#{NavigationBean.workspaceSelected}" />                                             
            <f:setPropertyActionListener value="0"   
                    target="#{ProjectEditBean.readOnly}" />                                             
        </a4j:commandLink>             
        <f:verbatim>&nbsp</f:verbatim>                   
        <a4j:commandLink ajaxSingle="true" 
            action="#{ProjectEditBean.deleteConfirm}"
            actionListener="#{ApprovedProjectQueueBean.refresh}"
            oncomplete="#{rich:component('deleteProjectConfirmModalPanel')}.show()"
            title="Delete this project"
            rendered="#{UserBean.roleName == 'SUPERUSER'}"
            reRender="delete_confirm_panel_form">                                                                               
            <h:outputText value="delete"                                                                   
                title="Delete this project"/>                                                                         
            <f:param name="seqid" value="#{project.projectId}"/>
        </a4j:commandLink>                                                                                                                    
        <f:verbatim>&nbsp</f:verbatim>                   
    </h:column>  
                                                                                                                              
    <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="type">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_type_label}" title="#{bundle.aplsProjects_type_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
      <h:outputText value="#{project.investmentType}"/>                                                                                         
    </t:column>
                                                                                                                                              
    <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="phase">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_phase_label}" title="#{bundle.aplsProjects_phase_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
      <h:outputText value="#{project.cpicPhase}"/>                                                                                         
    </t:column> 
    <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="projectLastUpdatedByName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_projectLastUpdatedByName_label}" 
              title="#{bundle.aplsProjects_projectLastUpdatedByName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
      <h:graphicImage value="/images/tpl_icon_people.gif"
          rendered="#{project.projectLastUpdatedByName != null}"/>                                                                                                                                             
      <h:outputText value="#{project.projectLastUpdatedByName}"/>                                                                                         
    </t:column>
    <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="projectCreatedByName">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_projectCreatedByName_label}" title="#{bundle.aplsProjects_projectCreatedByName_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>
      <h:graphicImage value="/images/tpl_icon_people.gif"
          rendered="#{project.projectCreatedByName != null}"/>                                                                                                                                             
      <h:outputText value="#{project.projectCreatedByName}"/>                                                                                         
    </t:column>
    <t:column>                                                                                                                               
      <f:facet name="header">                                                                                                               
        <t:commandSortHeader columnName="projectCreatedDate">                                                                                           
          <f:facet name="ascending">                                                                                                        
          <h:graphicImage value="/images/sort_ascending.gif" rendered="true" alt="Sort Ascending" border="0"/>                              
          </f:facet>                                                                                                                        
          <f:facet name="descending">                                                                                                       
          <h:graphicImage value="/images/sort_descending.gif" rendered="true" alt="Sort Descending" border="0"/>                            
          </f:facet>                                                                                                                        
          <h:outputText value="#{bundle.aplsProjects_projectCreatedDate_label}" title="#{bundle.aplsProjects_projectCreatedDate_tooltip}"/>                 
        </t:commandSortHeader>                                                                                                              
      </f:facet>                                                                                                                            
      <h:outputText value="#{project.projectCreatedDate}"/>                                                                                         
    </t:column>
                                                                                                                                                             
    </t:dataTable>                                                                                                                          
                                                                                                                                            
    <h:panelGrid rowClasses="AlignCenter" columns="1" border="0">                                                            
      <t:dataScroller                                                                                                          
      for="aprvd_prj_dtbl"                                                                                                                                         
      fastStep="15"                                                                                                                         
      pageCountVar="pageCount"                                                                                                              
      pageIndexVar="pageIndex"                                                                                                              
      styleClass="scroller"                                                                                                                 
      paginator="true"                                                                                                                      
      paginatorMaxPages="9"                                                                                                                 
      paginatorTableClass="paginator"                                                                                                       
      paginatorActiveColumnStyle="font-weight:bold;"                                                                                        
      immediate="false">                                                                                                                    
      <f:facet name="first" >                                                                                                               
        <h:graphicImage url="/images/arrow-first.gif" alt="First" border="1" />                                                             
      </f:facet>                                                                                                                            
      <f:facet name="last">                                                                                                                 
        <h:graphicImage url="/images/arrow-last.gif" alt="Last" border="1" />                                                               
      </f:facet>                                                                                                                            
      <f:facet name="previous">                                                                                                             
        <h:graphicImage url="/images/arrow-previous.gif" alt="Previous" border="1" />                                                       
      </f:facet>                                                                                                                            
      <f:facet name="next">                                                                                                                 
        <h:graphicImage url="/images/arrow-next.gif" alt="Next" border="1" />                                                               
      </f:facet>                                                                                                                            
      <f:facet name="fastforward">                                                                                                          
        <h:graphicImage url="/images/arrow-ff.gif" alt="Fast Forward" border="1" />                                                         
      </f:facet>                                                                                                                            
      <f:facet name="fastrewind">                                                                                                           
        <h:graphicImage url="/images/arrow-fr.gif" alt="Fast Rewind" border="1" />                                                          
      </f:facet>                                                                                                                            
      </t:dataScroller>                                                                                                                     
      <t:dataScroller                                                                                                          
        for="aprvd_prj_dtbl"                                                                                                                          
        rowsCountVar="rowsCount"                                                                                                            
        displayedRowsCountVar="displayedRowsCountVar"                                                                                       
        firstRowIndexVar="firstRowIndex"                                                                                                    
        lastRowIndexVar="lastRowIndex"                                                                                                      
        pageCountVar="pageCount"                                                                                                            
        pageIndexVar="pageIndex">                                                                                                           
        <h:outputFormat value="#{bundle['aplsProjectsDataScroller_pages']}" styleClass="textarea" escape="false">                                
          <f:param value="#{rowsCount}" />                                                                                                  
          <f:param value="#{displayedRowsCountVar}" />                                                                                      
          <f:param value="#{firstRowIndex}" />                                                                                              
          <f:param value="#{lastRowIndex}" />                                                                                               
          <f:param value="#{pageIndex}" />                                                                                                  
          <f:param value="#{pageCount}" />                                                                                                  
        </h:outputFormat>                                                                                                                   
        </t:dataScroller>                                                                                                                   
     </h:panelGrid> 
     
  </h:panelGrid> 
  