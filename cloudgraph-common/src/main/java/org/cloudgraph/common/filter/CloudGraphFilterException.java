package org.cloudgraph.common.filter;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class CloudGraphFilterException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CloudGraphFilterException(String message)
    {
        super(message);
    }
    public CloudGraphFilterException(Throwable t)
    {
        super(t);
    }
}