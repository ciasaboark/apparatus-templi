package org.apparatus_templi;

import org.apparatus_templi.driver.Driver;

/**
 * Events represent an abstract way for Drivers to communicate with each other. Each event
 * encapsulates all of the data needed to make a decision about some event that has occurred. Events
 * allow Drivers to communicate without having to know another Driver's driver level protocol. Any
 * events generated should be passed to {@link Coordinator#receiveEvent(Driver, Event)} so they can
 * be shared with any interested Drivers. The general contract of an Event should be write once/read
 * many. Since a reference to an event may be shared among many different drivers the implementing
 * Event's constructor should be the only point from which data can be changed. Every event must
 * have at least an origin and a creation timestamp.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public abstract class Event {
	protected long timestamp;
	protected Driver origin;

	/**
	 * Every event has an attached references to the Driver that generated the event.
	 * 
	 * @return a reference to the Driver that created this event.
	 */
	final public Driver getOrigin() {
		return origin;
	}

	/**
	 * Every event has an attached timestamp to indicate its creation time.
	 * 
	 * @return the Unix epoch timestamp of this Events creation.
	 */
	final public long getTimestamp() {
		return timestamp;
	}
}
