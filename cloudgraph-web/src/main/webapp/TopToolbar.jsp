<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:form id="toptoolbar_form">
    <rich:toolBar height="24" itemSeparator="line">
         <rich:toolBarGroup location="right">
             <h:outputLink id="settingsLink"
                 value="#" 
                 title="">
                 <h:outputText value="My Settings"/>   
                 <rich:componentControl 
                     for="settingsModalPanel" attachTo="settingsLink" 
                     operation="show" 
                     event="onclick"/>
             </h:outputLink>     
         </rich:toolBarGroup>
         <rich:toolBarGroup location="right">
             <h:outputLink
                 value="#"
                 title="">
                 <h:outputText value="Other Information"/> 
             </h:outputLink>     
         </rich:toolBarGroup>
    </rich:toolBar>
</h:form>


