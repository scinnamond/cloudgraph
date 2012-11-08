package org.cloudgraph.state;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class StateException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public StateException(String message)
    {
        super(message);
    }
    public StateException(Throwable t)
    {
        super(t);
    }
}