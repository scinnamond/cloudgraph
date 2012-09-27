package org.cloudgraph.web.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * PhaseListener to catch the generated image 'src' secondary request created by
 * the chart component rendering.
 */
public class ChartListener implements PhaseListener {

    private static Log log =LogFactory.getLog(ChartListener.class);

    public final static String CHART_REQUEST = "chartcreatorrequest";

    public void afterPhase(PhaseEvent phaseEvent) {
        // log.debug("AFTER " + phaseEvent.getPhaseId());
        FacesContext facesContext = phaseEvent.getFacesContext();
        if (facesContext != null) {
        	UIViewRoot viewRoot = facesContext.getViewRoot();
        	if (viewRoot !=null) {
		        String rootId = viewRoot.getViewId();
		
		        if (rootId.indexOf(CHART_REQUEST) != -1) {
		            handleChartRequest(phaseEvent);
		        }
        	}
        }
    }

    private void handleChartRequest(PhaseEvent phaseEvent) {
        FacesContext facesContext = phaseEvent.getFacesContext();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map requestMap = externalContext.getRequestParameterMap();
        Map sessionMap = externalContext.getSessionMap();
        String id = (String) requestMap.get("id");
        if (id == null)
        	throw new IllegalStateException("could not get component id from request");
        if (log.isDebugEnabled())
            log.debug("client id: " + id);
        ChartImage image = (ChartImage) sessionMap.get(id);
        if (image == null)
        	throw new IllegalStateException("could not chart image from session for client id, '"
        			+ id + "'");
        try {
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            response.getOutputStream().write(image.getData());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            facesContext.responseComplete();
        }
    }

    public void beforePhase(PhaseEvent phaseEvent) {
        // log.debug("BEFORE " + phaseEvent.getPhaseId());
    }

    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    private void emptySession(Map sessionMap, String id) {
        sessionMap.remove(id);
    }
}
