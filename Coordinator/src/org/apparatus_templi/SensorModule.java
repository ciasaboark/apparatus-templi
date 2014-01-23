package org.apparatus_templi;

public interface SensorModule extends Driver {
	public String getSensorList();
	public String getSensorData(String sensorName);
}
