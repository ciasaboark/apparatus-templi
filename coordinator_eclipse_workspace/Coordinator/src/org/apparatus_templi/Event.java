package org.apparatus_templi;

public abstract class Event {
	public String eventType;
	protected long timestamp;
	protected Driver origin;
	
	final public void setOrigin(Driver origin) {
		this.origin = origin;
	}
	
	final public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	final public Driver getOrigin() {
		return origin;
	}
	
	final public long getTimestamp() {
		return timestamp;
	}
}
