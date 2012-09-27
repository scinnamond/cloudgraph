<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>


<h:panelGrid columns="2" width="30%" border="0"
    cellpadding="4" cellspacing="4">  
	<h:commandButton id="create_project_button" 
	    value="#{bundle.aplsSearch_search_label}"
	    title="#{bundle.aplsSearch_search_tooltip}" 
	    action="#{SearchBean.search}">
    </h:commandButton>
	<h:commandButton id="clear_button" 
	    value="#{bundle.aplsSearch_refresh_label}"
	    title="#{bundle.aplsSearch_refresh_tooltip}" 
	    action="#{SearchBean.clear}"
	    immediate="true">
	</h:commandButton>
 
</h:panelGrid>                    	                                

  
  	