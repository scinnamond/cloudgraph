package org.cloudgraph.web.sdo.adapter;



public abstract class QueueAdapter extends Adapter implements Queueable {
	private static final long serialVersionUID = 1L;
	protected Integer index;

	public Integer getIndex() {
		return index;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}	
    	
	
}
