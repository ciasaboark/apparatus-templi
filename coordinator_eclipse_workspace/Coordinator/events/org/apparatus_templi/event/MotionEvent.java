package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * Generated when motion has been detected.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class MotionEvent extends Event {
	public MotionEvent() {
		this(System.currentTimeMillis(), null);
	}

	public MotionEvent(long timestamp, Driver origin) {
		this.timestamp = timestamp;
		this.origin = origin;
	}
}
