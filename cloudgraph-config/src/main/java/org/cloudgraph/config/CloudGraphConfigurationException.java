package org.cloudgraph.config;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
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