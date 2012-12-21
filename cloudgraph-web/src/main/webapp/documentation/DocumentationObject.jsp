
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
</head>
<body>
<a4j:outputPanel id="body_panel">
  <%/* begin boilerplate header */%>  
  <div class="TopToolbarDiv">
      <jsp:include page="/TopToolbar.jsp" flush="false"/>
  </div>    
  <a4j:outputPanel id="left_nav_panel">
      <f:verbatim><div class="LeftNavDiv"></f:verbatim>
          <jsp:include page="/LeftTreeNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <a4j:outputPanel id="top_nav_Panel">
      <f:verbatim><div class="TopNavDiv"></f:verbatim>
      <jsp:include page="/TopNav.jsp" flush="false"/>
      <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
  <%/* end boilerplate header */%> 


  <%@ include file="/ajaxloading.jsp" %>
  <a4j:outputPanel id="documentation_content_panel">
  <f:verbatim><div class="ContentDiv"></f:verbatim>
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 
      <h:panelGrid width="100%" columns="1" styleClass="DashboardTable"
          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
          cellpadding="0" cellspacing="0"> 
       
          <f:subview id="help">
          <f:verbatim>
          <!--[if !IE]> <-->
          <object id="frame_obj" 
            name="the_frame_obj" type="text/html"
            style="width=900px; overflow: hidden;" 
            data="</f:verbatim><h:outputText value="#{DocumentBean.url}"/><f:verbatim>"/>
            <p>content not found</p>
          </object>
          <!--> <![endif]-->
          
          <!--[if IE]>
          <object id="frame_obj" 
            name="the_frame_obj" classid="clsid:25336920-03F9-11CF-8FD0-00AA00686F13" 
            width="900" height="200" 
            style="border:0px;"
            data="</f:verbatim><h:outputText value="#{DocumentBean.url}"/><f:verbatim>"/>
            <p>content not found</p>
          </object>
          <![endif]-->
           
          </f:verbatim>
          </f:subview>
    
      </h:panelGrid>
  </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>

       
</body>
</html>
</f:view>