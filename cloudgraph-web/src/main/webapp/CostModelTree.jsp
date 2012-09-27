<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>


<rich:tree id="crmTree" switchType="client"
	value="#{CostModelTreeBean.model}" var="item" nodeFace="#{item.type}"
	nodeSelectListener="#{DashboardBean.costCategorySelectListener}">

	<rich:treeNode type="level_zero"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_one"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_two"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

 	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_three"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_four"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_five"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>

	<rich:treeNode type="level_any"
		iconLeaf="/images/orangedotleaf.gif" 
		icon="/images/orangedotleaf.gif">

	    <a4j:commandLink value="#{item.label}"
            styleClass="LeftNavActive" 
	        action="#{item.onAction}" 
	        reRender="investment_cost_panel"
	        title="#{item.tooltip}" />
	        
	</rich:treeNode>


</rich:tree>



