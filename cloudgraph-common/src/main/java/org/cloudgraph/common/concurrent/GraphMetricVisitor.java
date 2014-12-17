package org.cloudgraph.common.concurrent;

import java.util.HashSet;

import org.cloudgraph.common.CloudGraphConstants;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.core.CoreNode;

import commonj.sdo.DataObject;

public class GraphMetricVisitor implements PlasmaDataGraphVisitor {

	private long count = 0;
	private long depth = 0;
	private HashSet<String> threadNames = new HashSet<String>();

	@Override
	public void visit(DataObject target, DataObject source,
			String sourcePropertyName, int level) {
		count++;
		if (level > depth)
			depth = level;
		
		CoreNode node = (CoreNode)target;
		String thread = (String)node.getValueObject().get(
        		CloudGraphConstants.GRAPH_NODE_THREAD_NAME);
        if (thread != null)
        	this.threadNames.add(thread);

	}

	public long getCount() {
		return count;
	}

	public long getDepth() {
		return depth;
	}
	
	public long getThreadCount() {
		return threadNames.size();
	}
}

