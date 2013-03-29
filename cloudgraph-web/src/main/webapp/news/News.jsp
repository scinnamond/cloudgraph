
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
  <script type="text/javascript">                                                          
    function theFrameLoaded(theFrame) {  
        theFrame.style.height ="";
        theFrame.style.height = theFrame.contentWindow.document.body.scrollHeight + 'px';
        theFrame.style.width = "";
        theFrame.style.width = theFrame.contentWindow.document.body.scrollWidth + 'px';
    }                                                                                      
  </script>                                                                                
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
  <a4j:outputPanel id="news_content_panel">
  <f:verbatim><div class="ContentDiv"></f:verbatim>
  <h:panelGrid rowClasses="AlignCenter" columns="1" width="100%" border="0"> 
	  <h:panelGrid width="100%" columns="1" styleClass="DashboardTable"
          rowClasses="DashboardTable,DashboardTable,DashboardTable,DashboardTable,DashboardTable"
          cellpadding="0" cellspacing="0"> 
	    
        <h:graphicImage value="/images/caption_news.png"/>
        
        <h:panelGrid width="100%" styleClass="AlignLeft" columns="1"
            rendered="#{NewsBean.hasItem}">
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <a4j:commandLink>
                  <h:outputText value="#{NewsBean.newsItem.values['Type']}"/>
                </a4j:commandLink>
            </h:panelGrid>
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <h:outputText style="font-size: 250%; font-weight: normal" 
                    value="#{NewsBean.newsItem.values['Title']}"/>    
            </h:panelGrid>
            <h:panelGrid columns="6" styleClass="AlignLeft">
                <h:outputText value="By:"/>
                <a4j:commandLink>
                  <h:outputText value="#{NewsBean.newsItem.values['Author']}"/>
                </a4j:commandLink>
                <f:verbatim>|</f:verbatim>
                <h:outputText value="#{NewsBean.newsItem.values['EventDate']}"/>    
                <f:verbatim>|</f:verbatim>
                <a4j:commandLink>0 Comments</a4j:commandLink>
            </h:panelGrid>
            <rich:spacer height="20" />          
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <h:outputText escape="false" value="#{NewsBean.newsItem.values['Content']}"/>    
            </h:panelGrid>
        </h:panelGrid>
        
        <h:dataTable 
            rendered="#{!NewsBean.hasItem}"
            value="#{DataListBean.dataMap['NewsItem']}" 
            var="news">                                                                                            
         <h:column>
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <a4j:commandLink><h:outputText value="#{news.values['Type']}"/></a4j:commandLink>
            </h:panelGrid>
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <h:outputText style="font-size: 250%; font-weight: normal" 
                    value="#{news.values['Title']}"/>    
            </h:panelGrid>
            <h:panelGrid columns="6" styleClass="AlignLeft">
                <h:outputText value="By:"/>
                <a4j:commandLink>
                  <h:outputText value="#{news.values['Author']}"/>
                </a4j:commandLink>
                <f:verbatim>|</f:verbatim>
                <h:outputText value="#{news.values['EventDate']}"/>    
                <f:verbatim>|</f:verbatim>
                <a4j:commandLink>0 Comments</a4j:commandLink>
            </h:panelGrid>
            <rich:spacer height="20" />          
            <h:panelGrid columns="1" styleClass="AlignLeft">
                <h:outputText escape="false" value="#{news.values['Content']}"/>    
            </h:panelGrid>
            <rich:spacer height="20" />          
            <rich:separator height="4" lineType="double"/>          
            <rich:spacer height="10" />          
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