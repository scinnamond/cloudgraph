package org.cloudgraph.context;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class ContextException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public ContextException(String message)
    {
        super(message);
    }
    public ContextException(Throwable t)
    {
        super(t);
    }
}