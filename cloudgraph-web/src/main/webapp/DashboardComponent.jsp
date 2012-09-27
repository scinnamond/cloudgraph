<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="/WEB-INF/chartcreator.tld" prefix="c"%>

  <rich:simpleTogglePanel id="chart_toggle_pannel"
      opened="true" switchType="client" width="#{component.container.width}"
      label="#{component.caption}" 
      rendered="#{component.typeName == 'CHART'}"
      bodyClass="ChartBodyDiv">
      <h:panelGrid columns="6" width="100%" 
          columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
          cellpadding="3" cellspacing="3" border="0" > 
      	  <a4j:commandLink 
      	       rendered="#{component.chartType != 'bar'}"
	           reRender="dashboard_content_panel"
	           action="#{component.toBarChart}" 
	           title="View this data as a bar chart">
              <h:graphicImage value="/images/bar_chart_16_16.gif"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       rendered="#{component.chartType != 'pie'}"
	           reRender="dashboard_content_panel"
	           action="#{component.toPieChart}" 
	           title="View this data as a pie chart">
              <h:graphicImage value="/images/pie_chart2_16_16.gif"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>  	      
      	  <a4j:commandLink 
      	       value="3D"
      	       rendered="#{!component.isThreeDimensional}"
	           reRender="dashboard_content_panel"
	           action="#{component.setIs3D}" 
	           title="Render this chart with a three dimensional (3D) look">
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       value="2D"
      	       rendered="#{component.isThreeDimensional}"
	           reRender="dashboard_content_panel"
	           action="#{component.unsetIs3D}" 
	           title="Render this chart with a two dimensional (2D) look">
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       rendered="#{!component.expanded}"
	           reRender="dashboard_content_panel"
	           action="#{component.expand}" 
	           title="Expand this chart filling the dashboard. (use the collapse icon to restore it)">
              <h:graphicImage value="/images/arrow_out.png"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       rendered="#{component.expanded}"
	           reRender="dashboard_content_panel"
	           action="#{component.collapse}" 
	           title="Collapse this chart back to it's original position within the dashboard.">
              <h:graphicImage value="/images/arrow_in.png"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>    
      	  <a4j:commandLink 
      	       value="LogN"
      	       rendered="#{!component.isLogarithmic}"
	           reRender="dashboard_content_panel"
	           action="#{component.setIsLogarithmic}" 
	           title="Render this chart using a logarithmic scale for the range axis (enhances readability for values which differ widely)">
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       value="Num"
      	       rendered="#{component.isLogarithmic}"
	           reRender="dashboard_content_panel"
	           action="#{component.unsetIsLogarithmic}" 
	           title="Render this chart using a standard numeric scale">
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     	         
      	  <a4j:commandLink 
	           reRender="dashboard_content_panel"
	           action="#{component.refresh}" 
	           title="Refresh the data for this chart">
              <h:graphicImage value="/images/refresh2_16_16.gif"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
	           reRender="dashboard_content_panel, charts1_panel"
	           action="#{component.close}" 
	           title="Close this chart (the chart can be re-selected from the left-side 'Charts' selection area)">
              <h:graphicImage value="/images/chart_close.gif"/>
              <rich:spacer width="18" height="1"/>
	      </a4j:commandLink>     
      </h:panelGrid>
      <t:div styleClass="ChartDiv"> 
      <c:chart id="dashboard_chart"
          datasource="#{component.dataSource}"                                                                
          type="#{component.chartType}"                                                                                                                    
          is3d="#{component.isThreeDimensional}"                                                                                                                   
          isLogarithmic="#{component.isLogarithmic}"                                                                                                                   
          title="#{component.title}"                                                                                
          background="#{component.background}" 
          ylabel="#{component.ylabel}"
          colors="#{component.colorMap}"
          width="#{component.width}"                                                                                                                   
          height="#{component.height}"
          usemap="#{component.usemap}"
          orientation="#{component.orientation}"
          urlgenerator="#{component.urlGenerator}">                                                                                                                 
      </c:chart> 
      </t:div>				          
  </rich:simpleTogglePanel>	
  	
  <rich:simpleTogglePanel id="table_toggle_pannel"
      opened="true" switchType="client" width="#{component.container.width}"
      label="#{component.caption}" 
      rendered="#{component.typeName == 'TABLE'}"
      style="background-color: #C8D6B2;"
      bodyClass="ChartBodyDiv">
      <h:panelGrid columns="3" width="100%" 
          columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
          cellpadding="3" cellspacing="3" border="0" > 
      	  <a4j:commandLink 
      	       rendered="#{!component.expanded}"
	           reRender="dashboard_content_panel"
	           action="#{component.expand}" 
	           title="Expand this table filling the dashboard. (use the collapse icon to restore it)">
              <h:graphicImage value="/images/arrow_out.png"/>
              <rich:spacer width="26" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
      	       rendered="#{component.expanded}"
	           reRender="dashboard_content_panel"
	           action="#{component.collapse}" 
	           title="Collapse this table back to it's original position within the dashboard.">
              <h:graphicImage value="/images/arrow_in.png"/>
              <rich:spacer width="26" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
	           reRender="dashboard_content_panel"
	           action="#{component.refresh}" 
	           title="Refresh the data for this table">
              <h:graphicImage value="/images/refresh2_16_16.gif"/>
              <rich:spacer width="26" height="1"/>
	      </a4j:commandLink>     
      	  <a4j:commandLink 
	           reRender="dashboard_content_panel, alerts_panel, events_panel, tables_panel"
	           action="#{component.close}" 
	           title="Close this table (the table can be re-selected from the left-side selection area)">
              <h:graphicImage value="/images/chart_close.gif"/>
              <rich:spacer width="26" height="1"/>
	      </a4j:commandLink>     
      </h:panelGrid>

	  <rich:dataTable id="compDataTable"
	      value="#{component.data}" 
	      style="width:#{component.container.width - 26}px;"
	      var="row" 
	      rows="#{component.maxRows}">                                                                                            
	      <rich:column rendered="#{component.columnCount gt 0}"
	          headerClass="rich-table-subheadercell"
	          title="#{component.columnArray[0].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[0].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[0]}" 
	              rendered="#{component.columnArray[0].name != 'category' || row.type != 'CATEGORY' || row.isRoot}"/>
	      	  <a4j:commandLink 
	      	       value="#{row.data[0]}"
	      	       rendered="#{component.columnArray[0].name == 'category' && row.type == 'CATEGORY' && !row.isRoot}"
		           reRender="dashboard_content_panel"
		           title="roll-up into this category">
		           <h:graphicImage value="/images/arrow_left.png"/>
    		       <f:setPropertyActionListener value="#{row.data[0]}"   
    			        target="#{component.rollUpCategory}" />                      	                    
		      </a4j:commandLink>     
		  </rich:column>
	      <rich:column rendered="#{component.columnCount gt 1}"
	          headerClass="rich-table-subheadercell"
	          title="#{component.columnArray[1].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[1].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[1]}" 
	              rendered="#{component.columnArray[1].name != 'subCategory' || row.type != 'CATEGORY' || row.isLeaf}"/>
	      	  <a4j:commandLink 
	      	       value="#{row.data[1]}"
	      	       rendered="#{component.columnArray[1].name == 'subCategory' && row.type == 'CATEGORY' && !row.isLeaf}"
		           reRender="dashboard_content_panel"
		           title="drill-down into this sub-category">
		           <h:graphicImage value="/images/arrow_right.png"/>
    		       <f:setPropertyActionListener value="#{row.data[1]}"   
    			        target="#{component.drillDownCategory}" />   			                              	                    
		      </a4j:commandLink>     
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 2}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[2].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[2].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[2]}"/>
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 3}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[3].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[3].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[3]}" />
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 4}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[4].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[4].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[4]}" />
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 5}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[5].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[5].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[5]}" />
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 6}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[6].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[6].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[6]}" />
		  </rich:column>
		  <rich:column rendered="#{component.columnCount gt 7}"
	          headerClass="rich-table-subheadercell"
		      title="#{component.columnArray[7].description}">
	          <f:facet name="header">
	              <h:outputText value="#{component.columnArray[7].displayName}" />
	          </f:facet>                 
	          <h:outputText value="#{row.data[7]}" />
		  </rich:column>
	  </rich:dataTable>
      <rich:datascroller id="compDataScroller"
          align="center"
          for="compDataTable"
          maxPages="20"
          page="#{component.scrollerPage}"
          rendered="#{component.isPaginated}"
          reRender="dashboard_content_panel"/>
  </rich:simpleTogglePanel>
  	