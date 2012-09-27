<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>


<h:panelGrid columns="1" width="90%" border="0"
    columnClasses="AlignLeft">
    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_criteria_help}"/> 
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
</h:panelGrid>
<h:panelGrid rowClasses="FormPanelRow" 
    columnClasses="FormLabelColumn,FormControlColumn,FormLabelColumn,FormControlColumn" 
    columns="4" width="60%" border="0"> 
    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_budgetYear_label}:" 
        title="#{bundle.aplsSearch_budgetYear_tooltip}"/>
    <h:selectOneMenu
        required="false"
        value="#{SearchBean.budgetYear}"
        disabled="false"
        title="#{bundle.aplsSearch_budgetYear_tooltip}">
        <f:selectItems value="#{ReferenceDataCache.budgetYearItems}"/>
    </h:selectOneMenu>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    

    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_deputyArea_label}:" 
        title="#{bundle.aplsSearch_deputyArea_tooltip}"/>
    <h:selectOneMenu
        required="false"
        value="#{SearchBean.deputyArea}"
        disabled="false"
        title="#{bundle.aplsSearch_deputyArea_tooltip}">
        <f:selectItems value="#{ReferenceDataCache.deputyAreaItems}"/>
    </h:selectOneMenu>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    

    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_businessUnit_label}:" 
        title="#{bundle.aplsSearch_businessUnit_tooltip}"/>
    <h:selectOneMenu
        required="false"
        value="#{SearchBean.businessUnit}"
        disabled="false"
        title="#{bundle.aplsSearch_businessUnit_tooltip}">
        <f:selectItems value="#{ReferenceDataCache.businessUnitItems}"/>
    </h:selectOneMenu>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    

    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_investmentName_label}:" 
        title="#{bundle.aplsSearch_investmentName_tooltip}"/>
    <h:selectOneMenu
        required="false"
        value="#{SearchBean.investmentName}"
        disabled="false"
        title="#{bundle.aplsSearch_investmentName_tooltip}">
        <f:selectItems value="#{ReferenceDataCache.uniqueInvestmentNameItems}"/>
    </h:selectOneMenu>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_applicationName_label}:" 
        title="#{bundle.aplsSearch_applicationName_tooltip}"/>
    <h:inputText
        value="#{SearchBean.applicationName}"
        disabled="false"
        title="#{bundle.aplsSearch_applicationName_tooltip}">
    </h:inputText>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
    <h:outputText styleClass="labelBold" 
        value="#{bundle.aplsSearch_projectName_label}:" 
        title="#{bundle.aplsSearch_projectName_tooltip}"/>
    <h:inputText
        value="#{SearchBean.projectName}"
        disabled="false"
        title="#{bundle.aplsSearch_projectName_tooltip}">
    </h:inputText>
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
    <f:verbatim>&nbsp</f:verbatim>    
</h:panelGrid>

  
  	