package org.cloudgraph.state.model;

import java.util.HashMap;

public class SequenceMapping {
	private HashMap<String, Integer> sequenceMap;
	private HashMap<Integer, String> uuidMap;
	private String uri;
	private String typename;
	public SequenceMapping(String uri, String typename) {
		this.sequenceMap = new HashMap<String, Integer>();
		this.uuidMap = new HashMap<Integer, String>();
		this.uri = uri;
		this.typename = typename;
	}	
	
	public void put(String uuid, Integer sequence) {
		this.sequenceMap.put(uuid, sequence);
		this.uuidMap.put(sequence, uuid);
	}
	
	public Integer create(String uuid) {
		Integer seq = new Integer(uuidMap.size() + 1);
		this.sequenceMap.put(uuid, seq);
		this.uuidMap.put(seq, uuid);
		return seq;
	}	

	public String remove(Integer sequence) {
		String uuid = this.uuidMap.remove(sequence);
		if (uuid != null)
		    this.sequenceMap.remove(uuid);
		return uuid;
	}
	
	public Integer remove(String uuid) {
		Integer sequence = this.sequenceMap.remove(uuid);
		if (sequence != null)
		    this.uuidMap.remove(sequence);
		return sequence;
	}
	
	public String getUUID(Integer sequence) {
		return this.uuidMap.get(sequence);
	}
	
	public Integer getSequence(String uuid) {
		return this.sequenceMap.get(uuid);
	}
	
	public HashMap<String, Integer> getSequenceMap() {
		return this.sequenceMap;
	}
	
	public String getTypeName() {
		return typename;
	}
	
	public String getUri() {
		return uri;
	}
	
}
