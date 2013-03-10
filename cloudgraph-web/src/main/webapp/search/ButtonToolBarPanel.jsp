<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>


<h:panelGrid columns="2" width="30%" border="0"
    cellpadding="4" cellspacing="4">  
	<a4j:commandButton id="create_project_button" 
	    value="#{bundle.aplsSearch_createProject_label}"
	    title="#{bundle.aplsSearch_createProject_tooltip}" 
	    action="#{ProjectEditBean.create}">
	    <f:setPropertyActionListener value="true"   
		    target="#{NavigationBean.administrationSelected}" />
    </a4j:commandButton>
    <%/* 
	<h:commandButton id="exit_button" 
	    value="#{bundle.aplsSearch_exit_label}"
	    title="#{bundle.aplsSearch_exit_tooltip}" 
	    action="exit"
	    immediate="true">
	    <f:setPropertyActionListener value="true"   
		    target="#{NavigationBean.dashboardSelected}" />
	</h:commandButton>
	*/%>  
</h:panelGrid>                    	                                

  
  	