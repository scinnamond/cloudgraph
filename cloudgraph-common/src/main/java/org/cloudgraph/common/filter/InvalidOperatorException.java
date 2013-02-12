package org.cloudgraph.common.filter;

import org.cloudgraph.common.CloudGraphRuntimeException;
import org.plasma.sdo.DataFlavor;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class InvalidOperatorException extends GraphFilterException
{
    private static final long serialVersionUID = 1L;
    public InvalidOperatorException(String message)
    {
        super(message);
    }
    public InvalidOperatorException(Throwable t)
    {
        super(t);
    }
    public InvalidOperatorException(String operator, DataFlavor dataFlavor)
    {
        super("invalid operator '" + operator + "' for (property type) data flavor '"
        		+ dataFlavor.name() + "'");
    }
}