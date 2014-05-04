package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * Generated when the humidty level has changed from the previously recorded value. The originating
 * driver is responsible for determining how much of a change is required before generating a new
 * event.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class HumidityChangedEvent extends Event {
	int humidity;
	int prevHumidity;

	public HumidityChangedEvent() {
		this(0, null, 0, 0);
	}

	public HumidityChangedEvent(long timestamp, Driver origin, int humidity, int prevHumidity) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.humidity = humidity;
		this.prevHumidity = prevHumidity;
	}
}
