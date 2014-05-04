package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * Generated when a proximity sensor is triggered. The distance recorded is in millimeters. A
 * distance value of -1 indicates that the proximity sensor was not capable of recording a distance
 * value.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class ProximityEvent extends Event {
	private final long distance;

	public ProximityEvent() {
		this(0, null, 0);
	}

	public ProximityEvent(long timestamp, Driver origin, long distance) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.distance = distance;
	}

	public long getDistance() {
		return distance;
	}
}
