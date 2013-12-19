package org.cloudgraph.web.util;

import java.io.Serializable;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;

import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.model.CommonSessionBean;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.configuration.ClassEditBean;
import org.cloudgraph.web.model.demo.DemoBean;
import org.cloudgraph.web.model.navigation.NavigationBean;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.model.search.SearchBean;



public class BeanFinder
    implements Serializable
{              
    public BeanFinder()                                                                                 
    {                                                                                                     
    }   
    
    public UserBean findUserBean()
    {                  
        return (UserBean) findClassBean(UserBean.class);
    }

    public ErrorHandlerBean findErrorHandlerBean()
    {                  
        return (ErrorHandlerBean) findClassBean(ErrorHandlerBean.class);
    }
    
    public NavigationBean findNavigationBean()
    {                  
        return (NavigationBean) findClassBean(NavigationBean.class);
    }

    public CommonSessionBean findCommonSessionBean()
    {
        return (CommonSessionBean)findClassBean(CommonSessionBean.class);    
    }  
    
    public SearchBean findSearchBean()
    {
        return (SearchBean)findClassBean(SearchBean.class);    
    } 
    
    public ClassEditBean findClassEditBean()
    {
        return (ClassEditBean)findClassBean(ClassEditBean.class);    
    }  
    
    public ReferenceDataCache findReferenceDataCache()
    {
        return (ReferenceDataCache)findClassBean(ReferenceDataCache.class);    
    }  
    
    public DemoBean findDemoBean()
    {
        return (DemoBean)findClassBean(DemoBean.class);    
    }  
    
    private Object findClassBean(Class ClassBean) {
    	FacesContext context = FacesContext.getCurrentInstance();                                           
        ApplicationFactory appFactory =                                                                     
            (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);               
        Application app = appFactory.getApplication();                                                      
                                                                                                             
        String beanName = ClassBean.getName().substring(                                     
        		ClassBean.getName().lastIndexOf(".") + 1);
                
        return app.createValueBinding("#{" + beanName + "}").getValue(context);
    }
    
    
}
