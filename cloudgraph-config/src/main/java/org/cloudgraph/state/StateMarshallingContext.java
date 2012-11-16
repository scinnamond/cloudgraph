package org.cloudgraph.state;

import org.cloudgraph.state.model.StateModeNonValidatinglDataBinding;

public class StateMarshallingContext {
    private StateModeNonValidatinglDataBinding binding;
	@SuppressWarnings("unused")
	private StateMarshallingContext() {}
	public StateMarshallingContext(StateModeNonValidatinglDataBinding binding) {
		this.binding = binding;
	}
	public StateModeNonValidatinglDataBinding getBinding() {
		return binding;
	}
	
}
