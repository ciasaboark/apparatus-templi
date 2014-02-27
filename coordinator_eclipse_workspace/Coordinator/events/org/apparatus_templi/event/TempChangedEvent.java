package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * An event generated when the recorded temperature
 * has varied from the previously recorded temperature.
 * The originating driver is responsible for defining
 * how much of a change must be recorded before generating
 * a new event.  Temperatures are recorded in degrees Celsius.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class TempChangedEvent extends Event {
	int temp;
	int prevTemp;
	
	public TempChangedEvent() {
		this(0, null, 0, 0);
	}
	
	public TempChangedEvent(long timestamp, Driver origin, int prevTemp, int newTemp) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.prevTemp = prevTemp;
		this.temp = newTemp;
		this.eventType = "TempChanged";
	}
	
	public int getTemp() {
		return temp;
	}
	
	public int getPrevTemp() {
		return prevTemp;
	}
}
