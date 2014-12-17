package org.cloudgraph.state;

public interface StateMarshalingContext {

	public abstract NonValidatingDataBinding getBinding();

	public abstract void returnBinding(NonValidatingDataBinding binding);

}