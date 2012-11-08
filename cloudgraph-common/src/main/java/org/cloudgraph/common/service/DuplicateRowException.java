package org.cloudgraph.common.service;

 
/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class DuplicateRowException extends GraphServiceException
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