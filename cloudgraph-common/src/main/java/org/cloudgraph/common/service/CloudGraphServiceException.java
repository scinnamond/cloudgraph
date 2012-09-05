package org.cloudgraph.common.service;

import org.cloudgraph.common.CloudGraphRuntimeException;

public class CloudGraphServiceException extends CloudGraphRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CloudGraphServiceException(String message)
    {
        super(message);
    }
    public CloudGraphServiceException(Throwable t)
    {
        super(t);
    }
}