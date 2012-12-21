<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:form id="topnav_form">
  <rich:toolBar height="34" itemSeparator="line">
      <rich:toolBarGroup styleClass="#{ControlNavigationBean.datafiltersAction.selected ? 'TopNavActive' : 'TopNav'}"
          rendered="false">
          <h:graphicImage value="#{ControlNavigationBean.datafiltersAction.icon}" />
          <a4j:commandLink
              reRender="body_panel"
              action="#{ControlNavigationBean.datafiltersAction.select}"
              styleClass="#{ControlNavigationBean.datafiltersAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{ControlNavigationBean.datafiltersAction.label}" 
              title="#{ControlNavigationBean.datafiltersAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.documentationAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.documentationAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.documentationAction.onAction}"
              styleClass="#{NavigationBean.documentationAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.documentationAction.label}" 
              title="#{NavigationBean.documentationAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.demoAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.demoAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.demoAction.onAction}"
              styleClass="#{NavigationBean.demoAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.demoAction.label}" 
              title="#{NavigationBean.demoAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.downloadAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.downloadAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.downloadAction.onAction}"
              styleClass="#{NavigationBean.downloadAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.downloadAction.label}" 
              title="#{NavigationBean.downloadAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.workspaceAction.selected ? 'TopNavActive' : 'TopNav'}"
          rendered="#{NavigationBean.workspaceAction.selected}">
          <h:graphicImage value="#{NavigationBean.workspaceAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.workspaceAction.onAction}"
              styleClass="#{NavigationBean.workspaceAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.workspaceAction.label}" 
              title="#{NavigationBean.workspaceAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.administrationAction.selected ? 'TopNavActive' : 'TopNav'}"
          rendered="#{UserBean.roleName == 'SUPERUSER'}">
          <h:graphicImage value="#{NavigationBean.administrationAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.administrationAction.onAction}"
              styleClass="#{NavigationBean.administrationAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.administrationAction.label}" 
              title="#{NavigationBean.administrationAction.tooltip}"/>
      </rich:toolBarGroup>

     </rich:toolBar>

</h:form>


