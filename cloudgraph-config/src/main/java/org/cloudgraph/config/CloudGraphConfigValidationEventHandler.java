package org.cloudgraph.config;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.bind.BindingValidationEventHandler;
import org.plasma.config.ConfigurationException;

public class CloudGraphConfigValidationEventHandler implements BindingValidationEventHandler {
	
    private static Log log = LogFactory.getLog(CloudGraphConfigValidationEventHandler.class);
    private int errorCount;
    private boolean cumulative = true;
	
	public int getErrorCount() {
		return errorCount;
	}

	public CloudGraphConfigValidationEventHandler() {}	
	public CloudGraphConfigValidationEventHandler(boolean cumulative) {
		this.cumulative = cumulative;
	}	
	
    public boolean handleEvent(ValidationEvent ve) {
        boolean result = this.cumulative;
        this.errorCount++;        
        ValidationEventLocator vel = ve.getLocator();
        
        String message = "Line:Col:Offset[" + vel.getLineNumber() + ":" + vel.getColumnNumber() + ":" 
            + String.valueOf(vel.getOffset())
            + "] - " + ve.getMessage();
        
        switch (ve.getSeverity()) {
        case ValidationEvent.WARNING:
            log.warn(message);
            break;
        case ValidationEvent.ERROR:
        case ValidationEvent.FATAL_ERROR:
            log.fatal(message);
            throw new ConfigurationException(message);
        default:
            log.error(message);
        }
        return result;
    }
    
    public void reset()
    {
    	this.errorCount = 0;
    }

}
