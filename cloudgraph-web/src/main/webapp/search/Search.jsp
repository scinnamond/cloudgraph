
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
  <script type="text/javascript">
	function openAnalysisWin(url)
	{
	    alert(url);
	    theWin = window.open(url, "Project Analysis", "location=1,status=1,scrollbars=1,width=480,height=300");
	    theWin.moveTo(100, 100);
	}
  </script>
</head>
<body>
  <%/* begin boilerplate header */%>  
  <div class="AppTitleDiv">
      <h:panelGrid columns="2" border="0"> 
      <h:graphicImage value="/images/2009-logo-usfs.gif" style="border:0"/>
      <h:graphicImage value="/images/app_title_mirror.png" style="border:0"/>
      </h:panelGrid>
  </div>
  <%/*   
  <div class="TopToolbarDiv">
      <jsp:include page="/TopToolbar.jsp" flush="false"/>
  </div> 
  <div class="LeftNavDiv">
  <jsp:include page="/LeftTreeNav.jsp" flush="false"/>
  </div>
  */%>
  <div class="TopNavDivLeft">
  <a4j:outputPanel id="top_nav_Panel">
      <jsp:include page="/TopNav.jsp" flush="false"/>
  </a4j:outputPanel>
  </div>
  <%/* end boilerplate header */%> 

  <%@ include file="/ajaxloading.jsp" %>
  <div class="ContentDivLeft">
  <h:form id="project_list">                                                                                                                
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 


    <rich:tabPanel switchType="ajax" width="98%">
        <rich:tab label="Projects"
            title="" disabled="false">
        </rich:tab>
    </rich:tabPanel>
</h:panelGrid>
</h:form>     
</div>

<jsp:include page="/search/ProjectDeleteConfirmModalPanel.jsp" flush="false"/>
       
</body>
</html>
</f:view>
