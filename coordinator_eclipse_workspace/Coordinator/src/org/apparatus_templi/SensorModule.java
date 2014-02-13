package org.apparatus_templi;

import java.util.ArrayList;

abstract class SensorModule extends Driver {
	/*
	 * Abstract methods to be implemented in subclass
	 */
	static final String TYPE = "Sensor";
	public abstract ArrayList<String> getSensorList();
	public abstract String getSensorData(String sensorName);
	
	String getModuleType() {
		return SensorModule.TYPE;
	}

}
