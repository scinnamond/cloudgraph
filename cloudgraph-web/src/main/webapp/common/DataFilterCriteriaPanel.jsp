<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

  <h:panelGrid width="100%" columns="1"
      rendered="#{SearchBean.activeParameterCount > 0}">
      <rich:panel style="width: 100%; height: 80px;" >
          <f:facet name="header">
              <h:outputText value="Current Data Filter Criteria"/>
          </f:facet>
      
      <f:subview id="crit_sv">	              
          <f:subview id="prm_sv_0" rendered="#{SearchBean.activeParameterCount > 0}">
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[0].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[0].displayName}:"
			      action="#{SearchBean.activeParameters[0].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[0].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[0].index < SearchBean.activeParameterCount}" />                              
		  </f:subview>
          <f:subview id="prm_sv_1" rendered="#{SearchBean.activeParameterCount > 1}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[1].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[1].displayName}:"
			      action="#{SearchBean.activeParameters[1].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[1].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[1].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
          <f:subview id="prm_sv_2" rendered="#{SearchBean.activeParameterCount > 2}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[2].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[2].displayName}:"
			      action="#{SearchBean.activeParameters[2].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[2].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[2].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>				  
          <f:subview id="prm_sv_3" rendered="#{SearchBean.activeParameterCount > 3}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[3].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[3].displayName}:"
			      action="#{SearchBean.activeParameters[3].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[3].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[3].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
          <f:subview id="prm_sv_4" rendered="#{SearchBean.activeParameterCount > 4}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[4].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[4].displayName}:"
			      action="#{SearchBean.activeParameters[4].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[4].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[4].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
          <f:subview id="prm_sv_5" rendered="#{SearchBean.activeParameterCount > 5}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[5].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[5].displayName}:"
			      action="#{SearchBean.activeParameters[5].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[5].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[5].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
          <f:subview id="prm_sv_6" rendered="#{SearchBean.activeParameterCount > 6}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[6].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[6].displayName}:"
			      action="#{SearchBean.activeParameters[6].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[6].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[6].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
          <f:subview id="prm_sv_7" rendered="#{SearchBean.activeParameterCount > 7}">                  
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="#{SearchBean.activeParameters[7].valueIconName}"/>                              
			  <a4j:commandLink 
			      value="#{SearchBean.activeParameters[7].displayName}:"
			      action="#{SearchBean.activeParameters[7].removeValue}"
			      actionListener="#{DashboardBean.clearDataAction}"	    
		          reRender="dashboard_content_panel,segment_cost_tab"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:outputText value="#{SearchBean.activeParameters[7].displayValue}"
                  styleClass="GlobalSearchCritValueDiv"/>
		      <f:verbatim>&nbsp</f:verbatim>
              <h:graphicImage value="/images/arrow_right.png" 
                  rendered="#{SearchBean.activeParameters[7].index < SearchBean.activeParameterCount}" />                              
	      </f:subview>
	      
     </f:subview>
     </rich:panel>
 </h:panelGrid>
  	