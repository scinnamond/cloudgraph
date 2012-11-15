package org.cloudgraph.state;

import org.cloudgraph.state.model.StateModelDataBinding;

public class StateMarshallingContext {
    private StateModelDataBinding binding;
	@SuppressWarnings("unused")
	private StateMarshallingContext() {}
	public StateMarshallingContext(StateModelDataBinding binding) {
		this.binding = binding;
	}
	public StateModelDataBinding getBinding() {
		return binding;
	}
	
}
