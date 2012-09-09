package org.cloudgraph.common.key;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class GraphKeyException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public GraphKeyException(String message)
    {
        super(message);
    }
    public GraphKeyException(Throwable t)
    {
        super(t);
    }
}