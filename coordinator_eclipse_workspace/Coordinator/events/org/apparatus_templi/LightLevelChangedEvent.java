package org.apparatus_templi;

/**
 * Generated when the light level has changed from
 * the previously recorded value.  The originating
 * driver is responsible for determining how much
 * of a change is required before issuing a new
 * event.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class LightLevelChangedEvent extends Event {
	private int lightLevel;
	private int prevLightLevel;
	
	public LightLevelChangedEvent() {
		this(0, null, 0, 0);
	}
	
	public LightLevelChangedEvent(long timestamp, Driver origin, int prevLightLevel, int lightLevel) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.prevLightLevel = prevLightLevel;
		this.lightLevel = lightLevel;
		this.eventType = "LightLevelChanged";
	}
	
	public int getLightLevel() {
		return lightLevel;
	}
	
	public int getPrevLightLevel() {
		return prevLightLevel;
	}
}
