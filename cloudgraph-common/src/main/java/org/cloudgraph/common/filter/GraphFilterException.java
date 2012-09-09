package org.cloudgraph.common.filter;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class GraphFilterException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public GraphFilterException(String message)
    {
        super(message);
    }
    public GraphFilterException(Throwable t)
    {
        super(t);
    }
}