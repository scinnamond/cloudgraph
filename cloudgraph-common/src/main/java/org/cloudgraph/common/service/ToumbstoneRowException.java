package org.cloudgraph.common.service;

 
/**
 * Exception used for conditions related to toumbstone rows.  
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ToumbstoneRowException extends GraphServiceException
{
    private static final long serialVersionUID = 1L;
    public ToumbstoneRowException(String message)
    {
        super(message);
    }
    public ToumbstoneRowException(Throwable t)
    {
        super(t);
    }
}