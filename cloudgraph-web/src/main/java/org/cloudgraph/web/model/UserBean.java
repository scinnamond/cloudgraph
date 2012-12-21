package org.cloudgraph.web.model;

import java.io.Serializable;

import org.cloudgraph.web.WebConstants;

public class UserBean implements Serializable {
	
    public String getBundleName() {
    	return WebConstants.BUNDLE_BASENAME;
    }

}
