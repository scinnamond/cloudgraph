package org.cloudgraph.connect;

import org.cloudgraph.CloudGraphRuntimeException;

public class ConnectonException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public ConnectonException(String message)
    {
        super(message);
    }
    public ConnectonException(Throwable t)
    {
        super(t);
    }
}