package org.cloudgraph.common.key;

import org.cloudgraph.common.CloudGraphRuntimeException;

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