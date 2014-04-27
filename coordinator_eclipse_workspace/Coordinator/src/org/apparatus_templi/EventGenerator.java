package org.apparatus_templi;

import java.util.ArrayList;

/**
 * An interface for all Drivers that can generate events.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface EventGenerator {
	// abstract public Event generateEvent();
	/**
	 * Every EventGenerator is expected to keep an internal list of all Event types that it can
	 * generate.
	 * 
	 * @return an ArrayList of Events that this Driver can generate.
	 * @deprecated this method will be removed in a future release. It has no particular use.
	 */
	@Deprecated
	abstract public ArrayList<Event> getEventTypes();

}
