package org.cloudgraph.config;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class CloudGraphConfigurationException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CloudGraphConfigurationException(String message)
    {
        super(message);
    }
    public CloudGraphConfigurationException(Throwable t)
    {
        super(t);
    }
}