<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j" %>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<a4j:status
  onstart="javascript:Richfaces.showModalPanel('ajaxloading');"
  onstop="javascript:Richfaces.hideModalPanel('ajaxloading');">
</a4j:status>
<%/*
<rich:modalPanel height="32" width="32" keepVisualState="false" 
    id="ajaxloading" resizeable="false" 
    style="filter:alpha(opacity=60);
        -moz-opacity: 0.6;
        opacity: 0.6; 
        padding:0px;">
       <h:graphicImage  url="/images/ajax-loader-32-32.gif"/>
</rich:modalPanel>
*/%> 
<style> 
    .rich-mpnl-body { 
        text-align: center; 
        background-color: #EFEFEF;
        filter: alpha(opacity=60);
        -moz-opacity: 0.6;
        opacity: 0.6;
        padding:6px;
    } 
</style>
<rich:modalPanel height="30" width="110" 
     keepVisualState="false" 
     id="ajaxloading" resizeable="false">
    <h:panelGrid columns="3" border="0">        
          <h:graphicImage width="16" height="16" 
                url="/images/ajax-loader3.gif"/>
          <h:outputText value=""/>
          <h:outputText value="Loading..."/>
    </h:panelGrid>
</rich:modalPanel> 	