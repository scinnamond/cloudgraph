
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<f:view>
<f:loadBundle basename="#{UserBean.bundleName}" var="bundle"/>
<html>
<head>
  <link href="/cloudgraph-web/css/cloudgraph-web.css" rel="stylesheet" type="text/css" />
  <title><h:outputText value="#{bundle.aplsWindowTitle}"/></title>
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
	   
          <h:panelGrid columns="1" styleClass="AlignLeft"
              rendered="#{ChapterBean.hasItem}">
              <h:graphicImage value="/images/caption_major_features.png"
                  rendered="#{DocumentBean.instance.values['Type'] == 'feature'}"/>
              <h:graphicImage value="/images/caption_services.png"
                  rendered="#{DocumentBean.instance.values['Type'] == 'service'}"/>
              <h:graphicImage value="/images/caption_overview.png"
                  rendered="#{DocumentBean.instance.values['Type'] == 'general'}"/>
              <rich:spacer height="20" /> 
              <h:outputText escape="false" value="#{ChapterBean.instance.values['Content']}"/>    
          </h:panelGrid>

        <h:dataTable 
            rendered="#{!DocumentBean.hasItem}"
            value="#{DataListBean.dataMap['Document']}" 
            var="document">                                                                                            
         <h:column>         
            <h:panelGrid columns="1" styleClass="AlignLeft">
              <h:graphicImage value="/images/caption_major_features.png"
                  rendered="#{document.values['Type'] == 'feature'}"/>
              <h:graphicImage value="/images/caption_services.png"
                  rendered="#{document.values['Type'] == 'service'}"/>
              <h:graphicImage value="/images/caption_overview.png"
                  rendered="#{document.values['Type'] == 'general'}"/>
            </h:panelGrid>
            
            <h:dataTable                                                                      
                value="#{document.values['Chapters']}"                                   
                var="chap"
                rendered="#{document.values['Type'] == 'feature' || document.values['Type'] == 'service' || document.values['Type'] == 'general'}">                                                                                        
             <h:column>                                                                       
                <h:panelGrid columns="1" styleClass="AlignLeft">                              
                    <h:outputText escape="false" value="#{chap.values['Content']}"/>      
                </h:panelGrid>                                                                
                <rich:spacer height="20" />                                                   
             </h:column>                                                                      
            </h:dataTable>                                                                    
            <rich:spacer height="20" />          
         </h:column>
        </h:dataTable>
	
      </h:panelGrid>
  </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  </a4j:outputPanel>
</a4j:outputPanel>

       
  <jsp:include page="/common/SettingsModalPanel.jsp" flush="false"/>
  <jsp:include page="/common/LoginModalPanel.jsp" flush="false"/>
  <jsp:include page="/common/ContactModalPanel.jsp" flush="false"/>
</body>
</html>
</f:view>