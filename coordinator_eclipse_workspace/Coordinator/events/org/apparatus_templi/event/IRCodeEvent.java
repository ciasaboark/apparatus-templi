package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * An Event representing the reception of an infrared signal.
 * The signal code is represented as an array of integer timing
 * values, with the first value representing the first on state.
 * The timings are stored is microseconds.  A valid code should
 * have an odd number of timing events, beginning with the length
 * of the first on code, and ending with the length of the last
 * on code. 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class IRCodeEvent extends Event {
	private int[] code;
	
	public IRCodeEvent() {
		this(0, null, null);
	}
	
	public IRCodeEvent(long timestamp, Driver origin, int[] code) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.code = code;
		this.eventType = "IRCodeEvent";
	}
	
	public int[] getCode() {
		return code;
	}
}
