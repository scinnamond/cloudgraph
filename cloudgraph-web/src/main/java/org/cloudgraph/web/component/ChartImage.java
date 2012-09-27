package org.cloudgraph.web.component;

import java.io.Serializable;

/**
 * Holds the chart image bytes for saving and restoring state via JSF.
 */
public class ChartImage implements Serializable {

	private byte[] data;

    public ChartImage(byte[] data)
    {
        this.data = data; 
    }
    
    public byte[] getData()
    {
        return data;
    }
}