package org.cloudgraph.common;

public class CloudGraphRuntimeException extends RuntimeException {
    
	private static final long serialVersionUID = 1L;
	public CloudGraphRuntimeException() {
        super();
    }

    public CloudGraphRuntimeException(Throwable t) {
        super(t);
    }
    public CloudGraphRuntimeException(String msg) {
        super(msg);
    }
}
