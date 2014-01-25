package org.apparatus_templi;

import java.util.ArrayList;

public class Coordinator {
	private String incommingBuffer;
	private ArrayList<String> remoteModules;
	private ArrayList<Driver> runningDrivers;
	
	/**
	 * Pass a message to the driver specified by name.
	 * @param driverName the unique name of the driver
	 * @param message the message to pass
	 */
	private void passMessage(String driverName, String message) {
		//TODO
	}
	
	/**
	 * Reads a single byte of data from the incoming serial connection
	 * @return the Byte value of the read byte, null if there was nothing
	 * 	to read or if the read failed.
	 */
	private Byte readSerial() {
		//TODO
		return null;
	}
	
	/**
	 * Checks the serial connection to see if there is any data available for reading
	 * @return true if data is available for reading, false otherwise
	 */
	private boolean serialDataAvailable() {
		//TODO
		return false;
	}
	
	
	
	
	/*
	 * Protected methods.  The drivers should make use of these 
	 */
	
	/**
	 * Sends a message to a remote module and waits waitPeriod seconds for a response.
	 * @param name the unique name of the remote module
	 * @param command the command to send to the remote module 
	 * @param waitPeriod how many seconds to wait for a response.  Maximum period to wait
	 * 	is 6 seconds.
	 * @return the response string of the remote module or null if no response was found
	 */
	protected synchronized String sendCommandAndWait(String name, String command, int waitPeriod) {
		//TODO: this could easily be abused by the drivers to bring down the system.  It might need
		//+ to be removed, or to limit the number of times any driver can call this method in a given
		//+ time period.
		return null;
	}
	
	/**
	 * Sends the given command to a specific remote module
	 * @param name the unique name of the remote module
	 * @param command the command to send to the remote module
	 */
	protected synchronized void sendCommand(String name, String command) {
		//TODO
	}
	
	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver
	 * 	name as well as a data tag.
	 * @param driverName the name of the module
	 * @param dataTag a tag to assign to this data.  This tag should be specific for each data block
	 * 	that your driver stores.  If there already exits data for the given tag the old data
	 * 	will be overwritten.
	 * @param data the string of data to store
	 * @return -1 if data overwrote information from a previous tag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	protected synchronized int storeData(String driverName, String dataTag, String data) {
		return 0;
	}
	
	/**
	 * Returns data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	protected synchronized String readData(String driverName, String dataTag) {
		return null;
	}
	
	/**
	 * Writes the given message to the log file. The log entry will be formatted with the module
	 * 	name.
	 * @param moduleName the name of the calling module
	 * @param message the message to be logged.
	 */
	protected synchronized void logMessage(String moduleName, String message) {
		//TODO
	}
	
	/**
	 * Checks the list of known remote modules. If the module is not present the Coordinator
	 * 	may re-query the remote modules for updates.
	 * @param moduleName the name of the remote module to check for
	 * @return true if the remote module is known to be up, false otherwise
	 */
	protected synchronized boolean isModulePresent(String moduleName) {
		boolean result = false;
		for (String name: remoteModules) {
			if (name.equals(moduleName)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	//TODO change this to a singleton
	public Coordinator() {
		incommingBuffer = "";
	}
	
	
	public static void main(String argv[]) {
		/*
		 * TODO:
		 * 	-wait for the local arduino to respond with "READY" on the serial line
		 * 	-broadcast a message to every remote module asking for their name
		 * 	-store those names
		 * 	-load all drivers
		 * 	-start the web server to listen for connections from a frontend
		 * 	-enter infinite loop checking for input from the local arduino
		 */
	}
	
	/**
	 * Logging facilities for Coordinator and the drivers
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	protected static class Log {
		/**
		 * write debugging information to the log
		 * @param tag a String to identify this message
		 * @param message the debugging message to be logged
		 */
		public static void d(String tag, String message) {
			System.out.println(System.currentTimeMillis() + ": " + tag + ":" +  message);
		}
		
		/**
		 * write warning message to the log
		 * @param tag a String to identify this message
		 * @param message the warning message to be logged
		 */
		public static void w(String tag, String message) {
			System.out.println(System.currentTimeMillis() + ": " + tag + ":" +  message);
		}
		
		/**
		 * write error message to the log
		 * @param tag a String to identify this message
		 * @param message the error message to be logged
		 */
		public static void e(String tag, String message) {
			System.err.println(System.currentTimeMillis() + ": " + tag + ":" + message);
		}
	}
}
