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
<rich:modalPanel height="125" width="125" keepVisualState="false" 
         id="ajaxloading" resizeable="false" style="width:125px;
              margin:0px auto;
              text-align:left; 
              padding:15px;
              border:5px ridge;
              background-color:#eee;">
       <h:panelGrid style="margin-left:auto; margin-right:auto;" columns="1" width="100%" columnClasses="columncenter">
       <h:graphicImage  url="/images/ajaxloading.gif"/>
       <h:outputText value="Please Wait..."/>
       </h:panelGrid>
</rich:modalPanel>
 	