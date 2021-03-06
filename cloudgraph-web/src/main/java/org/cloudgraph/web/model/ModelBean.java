package org.cloudgraph.web.model;


// java imports
import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.AppMessageUtils;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.util.BeanFinder;



/**
 *
 */
public abstract class ModelBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(
        ModelBean.class);

    protected BeanFinder beanFinder = new BeanFinder();
    protected UserBean user;

	public ModelBean() {
		user = beanFinder.findUserBean();
		AppMessageUtils.setMessageBundle(user.getBundleName());
    }
    
        
    /**
     * forward
     * 
     * @param url
     *            String
     * @param response
     *            HttpServletResponse
     */
    protected void forward(String url, HttpServletResponse response) {
        try {
			if (log.isDebugEnabled()) {
				log.debug("redirecting to: " + url);
			}            
			response.sendRedirect(url);
        } catch (java.io.IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    
}
