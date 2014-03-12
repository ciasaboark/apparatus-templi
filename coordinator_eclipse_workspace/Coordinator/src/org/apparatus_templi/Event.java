package org.apparatus_templi;

import org.apparatus_templi.driver.Driver;

/**
 * Events represent an abstract way for Drivers to communicate with each other. Each event
 * encapsulates all of the data needed to make a decision about some event that has occurred. Events
 * allow Drivers to communicate without having to know another Driver's driver level protocol. Any
 * events generated should be passed to {@link Coordinator#receiveEvent(Driver, Event)} so they can
 * be shared with any interested Drivers. Every event must have at least an origin, and a timestamp
 * of the event creation.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public abstract class Event {
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
