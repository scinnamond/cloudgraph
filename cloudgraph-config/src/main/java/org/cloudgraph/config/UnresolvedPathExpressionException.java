package org.cloudgraph.config;

/**
 * Thrown when a configured SDO XPath path expression cannot be resolved. 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class UnresolvedPathExpressionException extends CloudGraphConfigurationException{

	private static final long serialVersionUID = 1L;

	public UnresolvedPathExpressionException(String message) {
		super(message);
	}

	public UnresolvedPathExpressionException(Throwable t) {
		super(t);
	}
}
