package org.cloudgraph.common.filter;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
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