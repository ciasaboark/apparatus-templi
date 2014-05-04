package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * Generated when a device has been detected. The identifier for the device is stored as a generic
 * String. The device type is recorded as one of the known types below.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class DeviceDetectedEvent extends Event {
	public static final int TYPE_OTHER = -1;
	public static final int TYPE_BT = 0;
	public static final int TYPE_WIFI = 1;

	private final int deviceType;
	private final String deviceID;

	public DeviceDetectedEvent() {
		this(0, null, "", 0);
	}

	public DeviceDetectedEvent(long timeStamp, Driver origin, String deviceID, int deviceType) {
		this.deviceID = deviceID;
		this.deviceType = deviceType;
		this.timestamp = timeStamp;
		this.origin = origin;
	}

	public String getDeviceID() {
		return this.deviceID;
	}

	public int getDeviceType() {
		return this.deviceType;
	}
}