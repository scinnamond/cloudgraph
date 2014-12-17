package org.cloudgraph.state;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * The data binding class <@link StateNonValidatingDataBinding> is both not thread safe and 
 * slow on creation due to the underlying JAXP XML Schema parsing, and therefore
 * this class provides a factory implementation for the associated binding pool
 * in support of concurrent contexts.
 *  
 * @author Scott Cinnamond
 * @since 0.6.2
 * 
 * @see StateNonValidatingDataBinding
 */
public class StateDataBindingFactory extends BasePooledObjectFactory<StateNonValidatingDataBinding> {

	@Override
	public StateNonValidatingDataBinding create() throws Exception {
		return new StateNonValidatingDataBinding();
	}

	@Override
	public PooledObject<StateNonValidatingDataBinding> wrap(
			StateNonValidatingDataBinding binding) {
		return new DefaultPooledObject<StateNonValidatingDataBinding>(binding);	
	}
}
