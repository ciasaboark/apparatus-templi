package org.apparatus_templi;

abstract class SensorModule extends Driver {
	/*
	 * Abstract methods to be implemented in subclass
	 */
	public abstract String getSensorList();
	public abstract String getSensorData(String sensorName);
	
	String getModuleType() {
		return "Sensor";
	}

}
