package org.cloudgraph.hbase.scan;

import org.cloudgraph.common.CloudGraphRuntimeException;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ScanException extends CloudGraphRuntimeException {
	private static final long serialVersionUID = 1L;

	public ScanException() {
		super();
	}

	public ScanException(String msg) {
		super(msg);
	}

	public ScanException(Throwable t) {
		super(t);
	}

}
