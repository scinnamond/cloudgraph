package org.cloudgraph.hbase.key;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class KeyException extends CloudGraphRuntimeException {
	private static final long serialVersionUID = 1L;

	public KeyException() {
		super();
	}

	public KeyException(String msg) {
		super(msg);
	}

	public KeyException(Throwable t) {
		super(t);
	}

}
