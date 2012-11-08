package org.cloudgraph.common.service;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphServiceException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public GraphServiceException(String message)
    {
        super(message);
    }
    public GraphServiceException(Throwable t)
    {
        super(t);
    }
}