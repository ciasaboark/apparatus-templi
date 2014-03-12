package org.apparatus_templi;

/**
 * An interface for Drivers that wish to be notified of Events. A driver that implements this
 * interface will not be automatically notified of all incoming events. Each Driver must register
 * the types of Events that it wishes to watch through
 * {@link Coordinator#registerEventWatch(org.apparatus_templi.driver.Driver, Event)}.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface EventWatcher {
	/**
	 * Receive an Event.
	 * 
	 * @param e
	 *            The Event to receive.
	 */
	public void receiveEvent(Event e);
}
