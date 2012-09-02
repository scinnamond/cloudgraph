package org.cloudgraph.key;

import org.cloudgraph.CloudGraphRuntimeException;

public class CloudGraphKeyException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CloudGraphKeyException(String message)
    {
        super(message);
    }
    public CloudGraphKeyException(Throwable t)
    {
        super(t);
    }
}