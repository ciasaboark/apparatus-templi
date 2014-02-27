package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * An event generated when the recorded sound level exceeds
 * some pre-defined maximum decibel level.  The maximum
 * decibel level is defined by the originating driver.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class SoundLevelExceededEvent extends Event {
	private int dbLevel;
	private int maxDbLevel;
	
	public SoundLevelExceededEvent() {
		this(0, null, 0, 0);
	}
	
	public SoundLevelExceededEvent(long timestamp, Driver origin, int maxDbLevel, int dbLevel) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.maxDbLevel = maxDbLevel;
		this.dbLevel = dbLevel;
		this.eventType = "SoundLevelExceeded";
	}
	
	public int getDbLevel() {
		return dbLevel;
	}
	
	public int getMaxDbLevel() {
		return maxDbLevel;
	}
}
