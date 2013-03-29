<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:form id="toptoolbar_form">
    <rich:toolBar height="24" itemSeparator="line">
         <rich:toolBarGroup location="right"
             rendered="#{!UserBean.isAuthenticated}">
             <h:outputLink id="registerLink"
                 value="#" 
                 title="">
                 <h:outputText value="Register"/>   
                 <rich:componentControl 
                     for="settingsModalPanel" attachTo="registerLink" 
                     operation="show" 
                     event="onclick"/>
             </h:outputLink>     
         </rich:toolBarGroup>
         <rich:toolBarGroup location="right"
             rendered="#{UserBean.isAuthenticated}">
             <h:outputLink id="settingsLink"
                 value="#" 
                 title="">
                 <h:outputText value="Profile Settings"/>   
                 <rich:componentControl 
                     for="settingsModalPanel" attachTo="settingsLink" 
                     operation="show" 
                     event="onclick"/>
             </h:outputLink>     
         </rich:toolBarGroup>
         <rich:toolBarGroup location="right"
             rendered="#{!UserBean.isAuthenticated}">
             <h:outputLink id="loginLink"
                 value="#" 
                 title="">
                 <h:outputText value="Login"/>   
                 <rich:componentControl 
                     for="loginModalPanel" attachTo="loginLink" 
                     operation="show" 
                     event="onclick"/>
             </h:outputLink>     
         </rich:toolBarGroup>
         <rich:toolBarGroup location="right">
             <h:outputLink id="contactLink"
                 value="#" 
                 title="">
                 <h:outputText value="Contact"/>   
                 <rich:componentControl 
                     for="contactModalPanel" attachTo="contactLink" 
                     operation="show" 
                     event="onclick"/>
             </h:outputLink>     
         </rich:toolBarGroup>
    </rich:toolBar>
</h:form>


