package org.cloudgraph.hbase.io;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class OperationException extends CloudGraphRuntimeException {

	private static final long serialVersionUID = 1L;

	public OperationException() {
		super();
	}

	public OperationException(String msg) {
		super(msg);
	}

	public OperationException(Throwable t) {
		super(t);
	}
}
