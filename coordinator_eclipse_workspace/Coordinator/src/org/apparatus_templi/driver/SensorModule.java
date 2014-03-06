package org.apparatus_templi.driver;

import java.util.ArrayList;


/**
 * An abstract class for a driver that interacts with a
 * remote module containing at least one sensor.
 * This driver would likely be paired with a remote module
 * that takes periodic readings or measurements.  It can be
 * used with modules that wait for incoming commands before
 * taking readings, or with modules that periodically send
 * updated readings.  If used with the latter the driver
 * does not need to remain active, and will be restarted
 * after the incoming message is placed in its queue.
 */
public abstract class SensorModule extends Driver {
	/**
	 * Return a list of all sensors on the remote module.
	 * The driver is expected to keep track of which sensors are
	 * available on its corresponding remote module.  The front
	 * ends will use this interface to get a list of what sensors
	 * are available.
	 * @return an ArrayList of Strings of all known sensors on the remote
	 * 	module.
	 */
	public abstract ArrayList<String> getSensorList();
	
	/**
	 * returns a String representation of a reading from the given
	 * 	sensor.  The driver may return cached data if desired, or
	 * 	send a blocking query to the remote module for a fresh
	 * 	reading.
	 * @param sensorName the name of the sensor on the remote module.
	 * @return a String representation of the data read by the
	 * 	sensor.
	 */
	public abstract String getSensorData(String sensorName);
	
//	/**
//	 * Returns the String type of Driver.  This type can be checked
//	 * 	against SensorModule.TYPE and ControllerModule.TYPE to determine
//	 * 	the type of driver.
//	 */
//	public String getDriverType() {
//		return SensorModule.TYPE;
//	}

}
