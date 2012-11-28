package org.cloudgraph.state;


public class StateMarshallingContext {
    private StatelNonValidatinglDataBinding binding;
	@SuppressWarnings("unused")
	private StateMarshallingContext() {}
	public StateMarshallingContext(StatelNonValidatinglDataBinding binding) {
		this.binding = binding;
	}
	public StatelNonValidatinglDataBinding getBinding() {
		return binding;
	}
	
}
