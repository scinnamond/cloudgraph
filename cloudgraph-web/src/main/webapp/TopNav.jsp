<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:form id="topnav_form">
  <rich:toolBar height="34" itemSeparator="line">
      <rich:toolBarGroup styleClass="#{ControlNavigationBean.datafiltersAction.selected ? 'TopNavActive' : 'TopNav'}"
          rendered="#{!ControlNavigationBean.datafiltersAction.selected}">
          <h:graphicImage value="#{ControlNavigationBean.datafiltersAction.icon}" />
          <a4j:commandLink
              reRender="body_panel"
              action="#{ControlNavigationBean.datafiltersAction.select}"
              styleClass="#{ControlNavigationBean.datafiltersAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{ControlNavigationBean.datafiltersAction.label}" 
              title="#{ControlNavigationBean.datafiltersAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.dataAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.dataAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.dataAction.onAction}"
              styleClass="#{NavigationBean.dataAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.dataAction.label}" 
              title="#{NavigationBean.dataAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.workspaceAction.selected ? 'TopNavActive' : 'TopNav'}"
          rendered="true">
          <h:graphicImage value="#{NavigationBean.workspaceAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.workspaceAction.onAction}"
              styleClass="#{NavigationBean.workspaceAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.workspaceAction.label}" 
              title="#{NavigationBean.workspaceAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.campaignAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.campaignAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.campaignAction.onAction}"
              styleClass="#{NavigationBean.campaignAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.campaignAction.label}" 
              title="#{NavigationBean.campaignAction.tooltip}"/>
      </rich:toolBarGroup>
      <rich:toolBarGroup styleClass="#{NavigationBean.configurationAction.selected ? 'TopNavActive' : 'TopNav'}">
          <h:graphicImage value="#{NavigationBean.configurationAction.icon}" />
          <h:commandLink
              action="#{NavigationBean.configurationAction.onAction}"
              styleClass="#{NavigationBean.configurationAction.selected ? 'TopNavActive' : 'TopNav'}"
              value="#{NavigationBean.configurationAction.label}" 
              title="#{NavigationBean.configurationAction.tooltip}"/>
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


