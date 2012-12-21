<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:form id="leftnav_form">


<h:panelGrid columns="1" border="0">
    
    <rich:dataList var="instance" 
        value="#{DataListBean.dataMap['NewsItem']}" 
        rows="10" type="square">
        <h:outputLink value="#{instance.values['URL']}">
            <h:outputText value="#{instance.caption}"/>            
        </h:outputLink>        
    </rich:dataList>

    <rich:separator lineType="beveled" height="8" width="100%" align="center"/> 

    <rich:panelMenu style="width:230px"  mode="ajax" 
        iconExpandedGroup="disc" iconCollapsedGroup="disc" 
        iconExpandedTopGroup="chevronUp" iconGroupTopPosition="right" 
        iconCollapsedTopGroup="chevronDown"
        rendered="#{NavigationBean.documentationAction.selected}">

        <c:forEach var="document" items="#{DataListBean.dataMap['Document']}">
        <rich:panelMenuGroup 
            label="#{document.values['Title']}"
            expanded="true">
            <c:forEach var="chap" items="#{document.values['Chapters']}">
                <rich:panelMenuItem label="#{chap.values['Name']}" 
		            reRender="documentation_content_panel">                                                                               
		            <f:setPropertyActionListener value="#{chap.values['URL']}"   
		                target="#{DocumentBean.url}" />                                             
                </rich:panelMenuItem>
            </c:forEach>
            <c:forEach var="ref" items="#{document.values['References']}">
                <rich:panelMenuItem label="#{ref.values['Name']}" 
                    onclick="javascript:window.open('#{ref.values['URL']}', '#{ref.values['Target']}', 'dependent=no, menubar=no, toolbar=no');">
                </rich:panelMenuItem>
            </c:forEach>
        </rich:panelMenuGroup>
        </c:forEach>
 
    </rich:panelMenu>  

    <rich:panelMenu style="width:230px"  mode="ajax" 
        iconExpandedGroup="disc" iconCollapsedGroup="disc" 
        iconExpandedTopGroup="chevronUp" iconGroupTopPosition="right" 
        iconCollapsedTopGroup="chevronDown"
        rendered="#{NavigationBean.demoAction.selected}">

        <c:forEach var="modelGroup" items="#{DataListBean.dataMap['ModelGroup']}">
        <rich:panelMenuGroup 
            label="#{modelGroup.values['Title']}"
            expanded="true">
            <c:forEach var="model" items="#{modelGroup.values['Models']}">
                <rich:panelMenuItem label="#{model.values['Name']}" 
                    reRender="demo_content_panel"> 
                    <f:setPropertyActionListener value="#{model.values['Name']}"   
                        target="#{DemoBean.modelDisplayName}" />                                             
                    <f:setPropertyActionListener value="#{model.values['Description']}"   
                        target="#{DemoBean.modelDescription}" />                                             
                    <f:setPropertyActionListener value="#{model.values['ModelURL']}"   
                        target="#{DemoBean.modelUrl}" />                                             
                    <f:setPropertyActionListener value="#{model.values['JavaDocURL']}"   
                        target="#{DemoBean.javaDocUrl}" />                                             
                    <f:setPropertyActionListener value="#{model.values['Type']}"   
                        target="#{DemoBean.modelRootType}" />                                             
                    <f:setPropertyActionListener value="#{model.values['Namespace']}"   
                        target="#{DemoBean.modelRootURI}" />                                             
                </rich:panelMenuItem>
            </c:forEach>
        </rich:panelMenuGroup>
        </c:forEach>
 
    </rich:panelMenu>  
        
    <h:panelGrid columns="1" border="0"
        rendered="#{NavigationBean.administrationAction.selected || NavigationBean.workspaceAction.selected}">
        <rich:spacer width="230" height="1"/>
        
		<rich:simpleTogglePanel opened="true" switchType="ajax" 
			label="Catalogs" bodyClass="ParentPannelBody">
		  <h:panelGrid columns="6" width="100%" 
		      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
		      cellpadding="3" cellspacing="3" border="0" > 
		  	  <a4j:commandLink 
		          reRender="admin_content_panel"
		          title="Refresh this component">
		          <h:graphicImage value="/images/refresh2_16_16.gif"/>
		          <rich:spacer width="18" height="1"/>
		      </a4j:commandLink>
		  	  <a4j:commandLink 
		          title=""         
		          action="#{PackageEditBean.create}"
		          reRender="admin_content_panel">
		          <h:graphicImage value="/images/new_item.gif"/>
		          <rich:spacer width="18" height="1"/>
                  <f:setPropertyActionListener value="true"   
                      target="#{NavigationBean.workspaceSelected}" />                                             
		      </a4j:commandLink>
		   </h:panelGrid> 	        
		   <h:dataTable value="#{SearchBean.packages}" var="pkg">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/book.png"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{pkg.name}"
			            styleClass="LeftNavActive"
			            action="#{InstanceQueueBean.clear}"
				        reRender="lefnav_classes_panel,admin_content_panel"
				        title="#{pkg.name}">			        
			            <f:setPropertyActionListener value="#{pkg.seqId}"
		                    target="#{SearchBean.packageId}" />
		    	    </a4j:commandLink>
			    </h:column>
	            <h:column rendered="#{NavigationBean.administrationAction.selected || NavigationBean.workspaceAction.selected}">
				    <a4j:commandLink value="[edit]"
			            styleClass="LeftNavActive"
			            action="#{PackageEditBean.edit}" 
				        reRender="admin_content_panel"
				        title="#{pkg.definition}">			        
			            <f:setPropertyActionListener value="#{pkg.seqId}"
		                    target="#{PackageEditBean.packageId}" />
	                    <f:setPropertyActionListener value="true"   
	                        target="#{NavigationBean.workspaceSelected}" />                                             
		    	    </a4j:commandLink>
			    </h:column>
            </h:dataTable>
        </rich:simpleTogglePanel>

		<rich:simpleTogglePanel id="lefnav_classes_panel" opened="true" switchType="ajax" 
			label="Business Entities" bodyClass="ParentPannelBody"
			rendered="#{NavigationBean.administrationAction.selected || NavigationBean.workspaceAction.selected}">
		  <h:panelGrid columns="6" width="100%" 
		      columnClasses="ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv,ChartButtonDiv"
		      cellpadding="3" cellspacing="3" border="0" > 
		  	  <a4j:commandLink 
		          reRender="admin_content_panel"
		          title="Refresh this component">
		          <h:graphicImage value="/images/refresh2_16_16.gif"/>
		          <rich:spacer width="18" height="1"/>
		      </a4j:commandLink>
		  	  <a4j:commandLink 
		          title="" 
		          action="#{ClassEditBean.create}"
		          reRender="admin_content_panel">
		          <h:graphicImage value="/images/new_item.gif"/>
		          <rich:spacer width="18" height="1"/>
	              <f:setPropertyActionListener value="true"   
	                  target="#{NavigationBean.workspaceSelected}" />                                             
		      </a4j:commandLink>
		   </h:panelGrid> 	        
		   <h:dataTable value="#{SearchBean.classes}" var="clss">                                                                                            
	            <h:column>
		            <h:graphicImage value="/images/orangedotleaf.gif"/>
		            <f:verbatim>&nbsp</f:verbatim>
				    <a4j:commandLink value="#{clss.classifier.name}"
			            styleClass="LeftNavActive"
			            action="#{InstanceQueueBean.clear}"
				        reRender="admin_content_panel"
				        title="#{clss.classifier.definition}">			        
			            <f:setPropertyActionListener value="#{clss.seqId}"
		                    target="#{SearchBean.clazzId}" />
		    	    </a4j:commandLink>
			    </h:column>
	            <h:column rendered="#{NavigationBean.administrationAction.selected || NavigationBean.workspaceAction.selected}">
				    <a4j:commandLink value="[edit]"
			            styleClass="LeftNavActive"
			            action="#{ClassEditBean.edit}" 
				        reRender="admin_content_panel">			        
			            <f:setPropertyActionListener value="#{clss.seqId}"
		                    target="#{ClassEditBean.clazzId}" />
	                    <f:setPropertyActionListener value="true"   
	                        target="#{NavigationBean.workspaceSelected}" />                                             
		    	    </a4j:commandLink>
			    </h:column>
            </h:dataTable>
        </rich:simpleTogglePanel>
	</h:panelGrid>
	
</h:panelGrid>
</h:form>

