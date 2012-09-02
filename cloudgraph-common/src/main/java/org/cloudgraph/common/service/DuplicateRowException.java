package org.cloudgraph.common.service;

 
public class DuplicateRowException extends CloudGraphServiceException
{
    private static final long serialVersionUID = 1L;
    public DuplicateRowException(String message)
    {
        super(message);
    }
    public DuplicateRowException(Throwable t)
    {
        super(t);
    }
}