package org.apparatus_templi;

public final class DeviceDetectedEvent extends Event {
	public final String eventType = "DeviceDetected";
	
	private String deviceID;
	protected long timestamp;
	protected Driver origin;
	
	public DeviceDetectedEvent() {
		
	}
	
	public DeviceDetectedEvent(String deviceID, long timeStamp, Driver origin) {
		this.deviceID = deviceID;
		this.timestamp = timeStamp;
		this.origin = origin;
	}
	
	public String getDeviceID() {
		return this.deviceID;
	}
}