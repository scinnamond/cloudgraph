package org.cloudgraph.hbase.service;

import org.cloudgraph.state.StateMarshallingContext;

public class ServiceContext {
    private StateMarshallingContext marshallingContext;
    @SuppressWarnings("unused")
	private ServiceContext() {}
    public ServiceContext(StateMarshallingContext marshallingContext) {
    	this.marshallingContext = marshallingContext;
    }
	public StateMarshallingContext getMarshallingContext() {
		return marshallingContext;
	}
    
}
