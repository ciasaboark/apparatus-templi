package org.apparatus_templi;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apparatus_templi.driver.ControllerModule;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.driver.SensorModule;

/**
 * Coordinates message passing and driver loading.  Handles setting
 * up the environment, querying remote modules, loading drivers,
 * and starting the front end support.  Coordinator provides
 * wrapper methods to facilitate communication between the
 * front end, drivers, and the remote modules. 
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public class Coordinator {
	private static final String TAG = "Coordinator";
	private static HashMap<String, String> remoteModules   = new HashMap<String, String>();
	private static ConcurrentHashMap<String, Driver> loadedDrivers     = new ConcurrentHashMap<String, Driver>();
	private static ConcurrentHashMap<Driver, Thread> driverThreads     = new ConcurrentHashMap<Driver, Thread>();
	private static ConcurrentHashMap<Driver, Long>   scheduledWakeUps = new ConcurrentHashMap<Driver, Long>();
	private static ConcurrentHashMap<Event, ArrayList<EventWatcher>> eventWatchers = new ConcurrentHashMap<Event, ArrayList<EventWatcher>>();
	private static SerialConnection serialConnection;
	private static MessageCenter messageCenter = MessageCenter.getInstance();
	private static SimpleHttpServer webServer;
	private static Prefs prefs = Prefs.getInstance();
	private static boolean connectionReady = false;	

	/**
	 * Sends the given message to the correct driver specified by 
	 * {@link Message#getDestination()}. If the module that sent this
	 * message was not previously known, then a record of its presence
	 * is saved. If a driver matching that destination is loaded and its
	 * state is not {@link Thread.State.TERMINATED} then the message
	 * contents will be routed to the drivers
	 * {@link Driver#receiveCommand(String)} or {@link Driver#receiveBinary(byte[])}
	 * methods.  If the driver is currently TERMINATED then the
	 * message contents will be placed in the drivers appropriate
	 * queue, and the driver will be woken. 
	 * @param m the {@link Message} to route
	 */
	private static synchronized void routeIncomingMessage(Message m) {
		assert m != null;	//we should always have a valid message to work with
		
		//Log.d(TAG, "routeIncomingMessage()");
		String destination = m.getDestination();
		if (!isModulePresent(destination)) {
			Log.d(TAG, "adding remote module '" + destination + "' to the list of known modules");
			remoteModules.put(destination, "");
		}
		
		if (loadedDrivers.containsKey(destination)) {
			Driver driver = loadedDrivers.get(destination);
			if (driverThreads.get(driver).getState() == Thread.State.TERMINATED) {
				//If the driver is not currently running, then we will re-initialize it,
				//+ queue the message, then start execution.
				Log.d(TAG, "restarting terminated driver '" + destination + "' for incoming message");
				try {
					Driver newDriver = wakeDriver(driver.getName(), false);
					if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
						newDriver.queueBinary(m.getData());
					} else {
						newDriver.queueCommand(new String(m.getData()));
					}
					driverThreads.get(newDriver).start();
				} catch (Exception e) {
					Log.e(TAG, "error restarting driver '" + driver.getName() + "', incoming message will " +
							"be discarded");
				}
			} else {
				if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
					driver.receiveBinary(m.getData());
				} else {
					driver.receiveCommand(new String(m.getData()));
				}
				
				//If this driver was scheduled to sleep until a new message arrives
				//+ then we need to notify it to wake
				if (scheduledWakeUps.get(driver) != null && scheduledWakeUps.get(driver) == 0) {
					scheduledWakeUps.remove(driver);
					driver.notify();
				}
			}
		} else {
			Log.w(TAG, "incoming message to " + destination + " could not be routed: no such driver loaded");
		}
	}

	/**
	 * Sends a query string to all remote modules "ALL:READY?"
	 */
	private static synchronized void queryRemoteModules() {
		assert messageCenter != null;
		
		messageCenter.sendCommand("ALL", "READY?");
	}
	
	/**
	 * Loads the given driver, provided that the driver is not null,
	 * has a valid name, is not of type {@link Driver.TYPE}, and
	 * a driver with a matching name has not already been loaded. 
	 * @param d the driver to load.
	 * @return true if the given driver was loaded, false otherwise.
	 */
	private static boolean loadDriver(Driver d) {
		assert d != null;
		
		boolean isDriverLoaded = false;
		if (d.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(d.getModuleName()) && (d instanceof ControllerModule || d instanceof SensorModule)) {
		        loadedDrivers.put(d.getModuleName(), d);
		        Log.d(TAG, "driver " + d.getModuleName() + " of type " + d.getClass().getName() + " initialized");
		        isDriverLoaded = true;
        	} else {
        		Log.e(TAG, "error loading driver " + d.getClass().getName() + " a driver with the name " +
        				d.getModuleName() + " already exists");
        	}
		}
        
		return isDriverLoaded;
	}
	
	/**
	 * A wrapper method for restartDriver that automatically restarts
	 * the driver's thread after re-initialization.
	 * @param driverName the name of the {@link Driver} to restart.
	 * @return a reference to the newly restarted Driver.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private static synchronized Driver wakeDriver(String driverName)  {
		assert driverName != null;
		
		return wakeDriver(driverName, true);
	}
	
	/**
	 * Re-initializes the given driver if it's thread state is TERMINATED.
	 * The thread will be started 
	 * @param driverName the name of the {@link Driver} to re-initialize.
	 * @param autoStart if true the driver's thread will be started,
	 * otherwise the thread will have to be started manually.
	 * @return a reference to the newly restarted Driver.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private static synchronized Driver wakeDriver(String driverName, boolean autoStart) {
		assert driverName != null;
		assert loadedDrivers.containsKey(driverName);
		
		Driver d = loadedDrivers.get(driverName);
		Thread t = driverThreads.get(d);
		assert d != null && t != null;
		
		if (t.getState() == Thread.State.TERMINATED) {
			Log.d(TAG, "restarting driver '" + d.getName() + "' of class '" + d.getClass() + "' of type '" + d.getClass().getName() + "'");
			scheduledWakeUps.remove(d);
			driverThreads.remove(d);
			Thread newThread = new Thread(d);
			driverThreads.put(d, newThread);
			if (autoStart) {
				newThread.start();
			}
		} else {
			try {
				scheduledWakeUps.remove(d);
				d.wake();
			} catch (Exception e) {
				Log.d(TAG, "could not nofity object");
			}
		}
		return d;
	}
	
	private static void parseCommandLineOptions(String[] argv) {
		//Using apache commons cli to parse the command line options
		Options options = new Options();
		options.addOption("help", false, "Display this help message.");
		
		@SuppressWarnings("static-access")
		Option portOption = OptionBuilder.withArgName(Prefs.Keys.portNum)
				.hasArg()
				.withDescription("Bind the server to the given port number")
				.create(Prefs.Keys.portNum);
		options.addOption(portOption);
		
		@SuppressWarnings("static-access")
		Option serialOption = OptionBuilder.withArgName(Prefs.Keys.serialPort)
				.hasArg()
				.withDescription("Connect to the arduino on serial interface")
				.create(Prefs.Keys.serialPort);
		options.addOption(serialOption);
		
		@SuppressWarnings("static-access")
		Option configOption = OptionBuilder.withArgName(Prefs.Keys.configFile)
				.hasArg()
				.withDescription("Path to the configuration file")
				.create(Prefs.Keys.configFile);
		options.addOption(configOption);
		
		@SuppressWarnings("static-access")
		Option resourcesOption = OptionBuilder.withArgName(Prefs.Keys.webResourceFolder)
				.hasArg()
				.withDescription("Web frontend will use resources in the specified folder")
				.create(Prefs.Keys.webResourceFolder);
		options.addOption(resourcesOption);
		
		CommandLineParser cliParser = new org.apache.commons.cli.PosixParser();
		try {
			CommandLine cmd = cliParser.parse(options, argv);
			if (cmd.hasOption("help")) {
				//show help message and exit
				HelpFormatter formatter = new HelpFormatter();
				formatter.setOptPrefix("--");
				formatter.setLongOptPrefix("--");

				formatter.printHelp(TAG, options);
				System.exit(0);
			}
			
			//Load the configuration file URI
			String configFile;
			if (cmd.hasOption(Prefs.Keys.configFile)) {
				configFile = cmd.getOptionValue(Prefs.Keys.configFile);
				if (configFile.startsWith("~" + File.separator)) {
					configFile = System.getProperty("user.home") + configFile.substring(1);
				}
			} else {
				configFile = Prefs.DEF_PREFS.get(Prefs.Keys.configFile);
			}
			prefs.putPreference(Prefs.Keys.configFile, configFile);
			
			//Read in preferences from the config file
			prefs.readPreferences(configFile);
			
			//Read additional preferences from the command line options,
			//+ overwriting preferences in the config file.
			
			//If the user specified a port number then we will only
			//try binding to that port, else we try the default port number
			if (cmd.hasOption(Prefs.Keys.portNum)) {
				prefs.putPreference(Prefs.Keys.portNum, cmd.getOptionValue(Prefs.Keys.portNum));
			}
			
			if (cmd.hasOption(Prefs.Keys.webResourceFolder)) {
				prefs.putPreference(Prefs.Keys.webResourceFolder, cmd.getOptionValue(Prefs.Keys.webResourceFolder));
			}
			
			//if we were given a preferred port we will pass it to SerialConnection
			//+ when initialized
			if (cmd.hasOption(Prefs.Keys.serialPort)) {
				prefs.putPreference(Prefs.Keys.serialPort, cmd.getOptionValue(Prefs.Keys.serialPort));
			}
			
			
			
		}  catch (ParseException e) {
			System.out.println("Error processing options: " + e.getMessage());
			new HelpFormatter().printHelp("Diff", options);
			Coordinator.exitWithReason("Error parsing command line options");
		}
	}
	
	private static void openSerialConnection() {
		String serialPortName = prefs.getPreference(Prefs.Keys.serialPort);
		if (serialPortName == null) {
			serialConnection = new UsbSerialConnection();
		} else if (serialPortName.equals("dummy")) {
			serialConnection = new DummySerialConnection();
		} else {
			serialConnection = new UsbSerialConnection(serialPortName);
		}
		
		if (!serialConnection.isConnected()) {
			Coordinator.exitWithReason("could not connect to serial port '" + serialPortName + "'");
		}
	}
	
	private static void startWebServer() throws UnknownHostException {
		int portNum;
        try {
        	portNum = Integer.valueOf(prefs.getPreference(Prefs.Keys.portNum));
        } catch (NumberFormatException e) {
        	portNum = Integer.parseInt(Prefs.DEF_PREFS.get(Prefs.Keys.portNum));
        }
        if (prefs.getPreference(Prefs.Keys.serverBindLocalhost).equals("true")) {
        	Log.c(TAG, "Starting web server on port " + portNum + " bound to localhost address " +
        			InetAddress.getLocalHost());
        } else {
        	Log.c(TAG, "Starting web server on port " + portNum + " bound to loopback address");
        }
    	
        webServer = new SimpleHttpServer(portNum, portNum == Integer.parseInt(Prefs.DEF_PREFS.get(Prefs.Keys.portNum)) ? false : true,
        		prefs.getPreference(Prefs.Keys.serverBindLocalhost).equals("true") ? true : false);
        webServer.setResourceFolder(prefs.getPreference(Prefs.Keys.webResourceFolder));
        new Thread(webServer).start();
	}
	
	private static void restartWebServer() throws UnknownHostException {
		assert webServer != null;
		
		webServer.terminate();
		webServer = null;
		startWebServer();
	}
	
	private static void startDrivers() {
		//this should never be called when drivers are currently running
		assert loadedDrivers.isEmpty();
		assert driverThreads.isEmpty();
		
		//Instantiate all drivers specified in the config file
		String driverList = prefs.getPreference(Prefs.Keys.driverList);
		if (!driverList.equals("")) {
			Log.c(TAG, "Initializing drivers...");
			String[] drivers = driverList.split(",");
			for (String driverClassName: drivers) {
				try {
					Class<?> c = Class.forName("org.apparatus_templi.driver." + driverClassName);
					Driver d = (Driver)c.newInstance();
					loadDriver(d);
				} catch (Exception e) {
					Log.d(TAG, "unable to load driver '" + driverClassName + "'");
				}
			}
		} else {
			Log.w(TAG, "No drivers were specified in the configuration file: '" +
					prefs.getPreference(Prefs.Keys.configFile) +
					"', nothing will be loaded");
		}
		
	    //Start the driver threads
	    for (String driverName: loadedDrivers.keySet()) {
	    	Log.c(TAG, "Starting driver " + driverName);
	    	Thread t = new Thread(loadedDrivers.get(driverName));
	    	driverThreads.put(loadedDrivers.get(driverName), t);
	    	t.start();
	    }
	}
	
	private static void restartDrivers() {
		Log.d(TAG, "restarting all drivers");
		
		for (Driver d: loadedDrivers.values()) {
			Log.d(TAG, "terminating driver " + d.getName());
			d.terminate();
			wakeDriver(d.getName());
		}
		//Block for a few seconds to allow all drivers to finish their termination
		//+ procedure.  Since the drivers may call methods in this thread we need
		//+ to do a non-blocking wait instead of using a call to Thread.sleep()
		long sleepTime = System.currentTimeMillis() + (1000 * 5);
		while (sleepTime > System.currentTimeMillis()) {}
		
		while (!driverThreads.isEmpty()) {
			for (Driver d: driverThreads.keySet()) {
				Thread t = driverThreads.get(d);
				if (t.getState() == Thread.State.TERMINATED) {
					Log.d(TAG, "driver " + d.getName() + " terminated");
					driverThreads.remove(d);
				} else {
					Log.d(TAG, "waiting on driver " + d.getName() + " to terminate");
				}
			}
			
			//its possible that a driver scheduled a sleep period during
			//+ the termination process
			for (Driver d: scheduledWakeUps.keySet()) {
				wakeDriver(d.getName());
			}
		}
		
		driverThreads.clear();
		loadedDrivers.clear();
		startDrivers();
	}

	static void exitWithReason(String reason) {
		Log.t(TAG, reason);
		System.exit(1);
	}
	
	static void restartModule(String module) {
		switch (module) {
			case "main":
				Log.d(TAG, "restarting drivers");
				restartDrivers();
				break;
			case "web":
				Log.d(TAG, "restarting web server");
				try {
					restartWebServer();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				Log.w(TAG, "could not restart " + module + ", unknown module");
		}
	}

	/**
	 * Sends the given command to a specific remote module. The message will be formatted
	 * 	to fit the protocol version that this module supports (if known), otherwise the
	 * 	message will be formatted as the most recent protocol version.
	 * @param caller a reference to the calling {@link Driver}
	 * @param command the command to send to the remote module
	 */
	public static synchronized boolean sendCommand(Driver caller, String command) {
//		Log.d(TAG, "sendCommand()");
		boolean messageSent = false;
		
		if (connectionReady && caller.getName() != null) {
			messageSent = messageCenter.sendCommand(caller.getName(), command);
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
		}
		
		return messageSent;
	}

	
	/**
	 * Sends a message to a remote module and waits waitPeriod seconds for a response.
	 * @param caller a reference to the calling {@link Driver}
	 * @param command the command to send to the remote module 
	 * @param waitPeriod how many seconds to wait for a response.  Maximum period to wait
	 * 	is 6 seconds.
	 * @return the String of data that the remote module responded with, or null if there
	 * 	was no response. Note that the first incoming response is returned. If another
	 * 	message addressed to this 
	 */
	public static synchronized String sendCommandAndWait(Driver caller, String command, int waitPeriod) {
		//Log.d(TAG, "sendCommandAndWait()");
		String responseData = null;
		if (waitPeriod <= 6 && caller.getName() != null) {
			sendCommand(caller, command);
			long endTime = (System.currentTimeMillis() + ((1000) * waitPeriod));
			while (System.currentTimeMillis() < endTime) {
				if (messageCenter.isMessageAvailable()) {
					Message m = messageCenter.getMessage();
					if (m.getDestination().equals(caller.getName())) {
						try {
							responseData = new String(m.getData(), "UTF-8");
							break;
						} catch (UnsupportedEncodingException e) {
							Log.d(TAG, "sendCommandAndWait() error converting returned data to String");
							break;
						}
					} else {
						routeIncomingMessage(m);
					}
				}
			}
		}
		return responseData;
	}
	
	/**
	 * Sends binary data over the serial connection to a remote module.
	 * 	Does not yet break byte[] into chunks for transmission.  Make sure
	 * 	that the size of the transmission is not larger than a single packet's
	 * 	max payload size (around 80 bytes).
	 * @param caller a reference to the calling {@link Driver}
	 * @param data the binary data to send
	 */
	public static synchronized boolean sendBinary(Driver caller, byte[] data) {
		//Log.d(TAG, "sendBinary()");
		boolean messageSent = false;
		
		if (connectionReady && caller.getName() != null) {
			messageCenter.sendBinary(caller.getName(), data);
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
			messageSent = false;
		}
		
		return messageSent;
	}

	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver
	 * 	name as well as a data tag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a tag to assign to this data.  This tag should be specific for each data block
	 * 	that your driver stores.  If there already exits data for the given dataTag the old data
	 * 	will be overwritten.
	 * @param data the string of data to store
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	public static synchronized int storeTextData(String driverName, String dataTag, String data) {
		Log.d(TAG, "storeTextData()");
		return 0;
	}
	
	/**
	 * Stores the given data to persistent storage. Data is stored based off the given driverName
	 * 	and dataTag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a unique tag to assign to this data. This tag should be specific for each data
	 * 	block that will be stored. If data has already been stored with the same driverName and
	 * 	dataTag the old data will be overwritten.
	 * @param data the data to be stored
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	public static synchronized int storeBinData(String driverName, String dataTag, byte[] data) {
		Log.d(TAG, "storeBinData()");
		return 0;
	}
	
	/**
	 * Returns text data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	public static synchronized String readTextData(String driverName, String dataTag) {
		Log.d(TAG, "readTextData()");
		return null;
	}
	
	/**
	 * Returns binary data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored binary data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	public static synchronized Byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "readBinData()");
		return null;
	}
	
	/**
	 * Schedules a {@link Driver} to sleep for a indefinite period.
	 * The calling driver will be woken only when the system is going
	 * down or when an incoming message addressed to it is found.
	 * @param caller A reference to the driver to sleep
	 */
	public static synchronized void scheduleSleep(Driver caller) {
		if (caller != null) {
			scheduledWakeUps.put(caller, (long)0);
			Log.d(TAG, "scheduled an indefinite sleep for driver '" + caller.getName() + "'");
		}
	}
	
	/**
	 * Schedules a {@link org.apparatus_templi.driver.Driver} to re-create
	 * this driver at the given time. If a driver's state
	 * {@link java.lang.Tread} is
	 * {@link java.lang.Thread.State.TERMINATED} at the time of the wake up the
	 * Driver will be re-created. If the Driver's state is not
	 * TERMINATED then the driver will be woken by calling its
	 * {@link Object#notify()} method. WARNING: make sure
	 * that your driver stores any needed information before
	 * exiting its run() method.
	 * @param caller
	 * @param wakeTime
	 * @throws InterruptedException 
	 */
	public static synchronized void scheduleSleep(Driver caller, long wakeTime) {
		if (caller != null) {
			scheduledWakeUps.put(caller, wakeTime);
			String time;
			long diff = wakeTime - System.currentTimeMillis();
			if (diff <= 0) {
				time = "now";
			} else if (diff < 1000) {
				time = String.valueOf(diff) + " milliseconds";
			} else if (diff < 60000) {
				time = String.valueOf(diff/1000) + " seconds";
			} else {
				time = String.valueOf(diff/60000) + " minutes";
			}
			Log.d(TAG, "scheduled a wakup for driver '" + caller.getModuleName() + "' in " + time + ".");
		}
	}
	
	
	/**
	 * Pass a message to the driver specified by name.
	 * @param destination the unique name of the driver
	 * @param source the source of this message, either the name of the calling driver
	 * 	or null. If null, this command originated from the Coordinator
	 * @param command the command to pass
	 */
	public static synchronized boolean passCommand(String source, String destination, String command) {
		Log.d(TAG, "passCommand()");
		//TODO verify source name
		//TODO check for reserved name in toDriver
		boolean messagePassed = false;
		if (loadedDrivers.containsKey(destination)) {
			Driver destDriver = loadedDrivers.get(destination);
			if (driverThreads.get(destDriver).getState() != Thread.State.TERMINATED) {
				loadedDrivers.get(destination).receiveCommand(command);
				messagePassed = true;
			}
		}
		return messagePassed;
	}
	
	/**
	 * Requests the XML data representing a drivers status from the given driver.
	 * @param driverName the unique name of the driver to query
	 * @return the String representation of the XML data, or null if the driver does
	 * 	not exist
	 */
	public static synchronized String requestWidgetXML(String driverName) {
		Log.d(TAG, "requestWidgetXML()");
		String xmlData = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver driver = loadedDrivers.get(driverName);
			xmlData = driver.getWidgetXML();
		}
		return xmlData;
	}
	
	/**
	 * Requests the XML data representing a driver's detailed controls.
	 * @param driverName the unique name of the driver to query
	 * @return the String representation of the XML data, or null if the driver does
	 * 	not exist
	 */
	public static synchronized String requestFullPageXML(String driverName) {
		Log.d(TAG, "requestFullPageXML()");
		String xmlData = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver driver = loadedDrivers.get(driverName);
			xmlData = driver.getFullPageXML();
		}
		return xmlData;
	}

	/**
	 * Checks the list of known remote modules. If the module is not present the Coordinator
	 * 	may re-query the remote modules for updates.
	 * @param moduleName the name of the remote module to check for
	 * @return true if the remote module is known to be up, false otherwise
	 */
	public static synchronized boolean isModulePresent(String moduleName) {
		return remoteModules.containsKey(moduleName);
	}
	
	
	/**
	 * Returns a list of sensors that the given driver monitors.
	 * @param driverName the unique name of the driver to query
	 * @return an ArrayList of Strings representing all sensors monitored
	 * by the driver.  If the given driver is not of type 
	 * {@link SensorModule#TYPE} or if the driver is not loaded then
	 * returns null. 
	 */
	public static synchronized ArrayList<String> getSensorList(String driverName) {
		ArrayList<String> results = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver d = loadedDrivers.get(driverName);
			if (d instanceof org.apparatus_templi.driver.SensorModule) {
					results = ((SensorModule)d).getSensorList();
			}
		}
		return results;
	}
	
	/**
	 * Returns a list of controllers that the given driver interacts
	 * with.
	 * @param driverName the unique name of the driver to query
	 * @return an ArrayList of Strings representing all controllers
	 * this driver interacts with.  If the given driver is not of type 
	 * {@link ControllerModule#TYPE} or if the driver is not loaded then
	 * returns null. 
	 */
	public static synchronized ArrayList<String> getControllerList(String driverName) {
		ArrayList<String> results = new ArrayList<String>();
		if (loadedDrivers.containsKey(driverName)) {
			Driver d = loadedDrivers.get(driverName);
			if (d instanceof org.apparatus_templi.driver.ControllerModule) {
					results = ((ControllerModule)d).getControllerList();
			}
		}
		return results;
	}
	
	
	/**
	 * Returns a list of all loaded drivers.
	 * @return an ArrayList of Strings of driver names.  If no
	 * drivers are loaded then returns an empty list.
	 */
	public static synchronized ArrayList<String> getLoadedDrivers() {
		Log.d(TAG, "getLoadedDrivers()");
		ArrayList<String> driverList = new ArrayList<String>();
		for (String driverName: loadedDrivers.keySet()) {
			driverList.add(driverName);
		}
		
		return driverList;
	}
	
	public static void receiveEvent(Driver d, Event e) {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}
		
		if (d instanceof EventGenerator) {
			Log.d(TAG, "incoming event '" + e.eventType + "' from driver '" + d.getName() + "'");
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' not allowed to generate events");
		}
		
		ArrayList<EventWatcher> list = eventWatchers.get(e);
		if (list != null) {
			for (EventWatcher dvr: list){
				Log.d(TAG, "notifying driver " + d.getName() + " of event " + e.getClass().getSimpleName() + ".");
				dvr.receiveEvent(e);
			}
		}
	}
	
	
	public static void registerEventWatch(Driver d, Event e) throws IllegalArgumentException {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}
		
		if (d instanceof EventWatcher) {
			Log.d(TAG, " driver '" + d.getName() + "' requested to be notified of events of type '" + e.eventType + "'.");
			ArrayList<EventWatcher> curList = eventWatchers.get(e);
			if (curList == null) {
				curList = new ArrayList<EventWatcher>();
			}
			curList.add((EventWatcher)d);
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' can not listen for events of type '" + e.eventType + "', must implement EventWatcher.");
			throw new IllegalArgumentException();
		}
	}
	
	public static void removeEventWatch(Driver d, Event e) {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}
		
		if (d instanceof EventWatcher) {
			ArrayList<EventWatcher> list = eventWatchers.get(e);
			if (list != null) {
				list.remove(d);
			} else {
				Log.d(TAG, "could not remove driver " + d.getName() + " from watch list for event " +
					e.getClass().getSimpleName() + ", driver was not registered for watch.");
			}
		} else {
			Log.w(TAG, "could not remove driver " + d.getName() + " from watch list for event " +
					e.getClass().getSimpleName() + ", driver is not an event watcher.");
		}
	}
	
	public static void main(String argv[]) throws InterruptedException, IOException {
		//turn off debug messages
//		Log.setLogLevel(Log.LEVEL_WARN);
		
		Log.c(TAG, "Starting");
		parseCommandLineOptions(argv);
		
		//open the serial connection
		openSerialConnection();
		
		//start the message center
		messageCenter =  MessageCenter.getInstance();
		messageCenter.setSerialConnection(serialConnection);
		
		//block until the local Arduino is ready
		System.out.print(TAG + ":" +  "Waiting for local link to be ready.");
		if (!(serialConnection instanceof DummySerialConnection)) {
			byte[] sBytes = messageCenter.readBytesUntil((byte)0x0A);
			String sString = new String(sBytes);
			if (sString.endsWith("READY")) {
				connectionReady = true;
			}
	
			if (!connectionReady) {
				Coordinator.exitWithReason("could not find a local Arduino connection, exiting");
			} else {
				Log.c(TAG, "Local link ready.");
			}
		} else {
			connectionReady = true;
			Log.c(TAG, "Local dummy link ready");
		}
		
		//query for remote modules.  Since the modules may be slow in responding
		//+ we will wait for a few seconds to make sure we get a complete list
		System.out.print(TAG + ":" + "Querying modules (6s wait).");
		messageCenter.beginReadingMessages();
		//begin processing incoming messages
		new Thread(messageCenter).start();
		
		//if we are using the dummy serial connection then there is no point in waiting
		//+ 6 seconds for a response from the modules
		if (!(serialConnection instanceof DummySerialConnection)) {
			queryRemoteModules();
			for (int i = 0; i < 6; i++) {
				if (messageCenter.isMessageAvailable()) {
					routeIncomingMessage(messageCenter.getMessage());
				}
				
				System.out.print(".");
				Thread.sleep(1000);
			}
		}
		System.out.println();
		
		if (remoteModules.size() > 0) {
			Log.c(TAG, "Found " + remoteModules.size() + " modules :" + remoteModules.toString());
		} else {
			Log.c(TAG, "Did not find any remote modules.");
		}
        
        
		startDrivers();
        
        //Add a shutdown hook so that the drivers can be notified when
        //+ the system is going down.
        Runtime.getRuntime().addShutdownHook( new Thread() {
        	public void run() {
        		Log.d(TAG, "system is going down. Notifying all drivers.");
        		//cancel any pending driver restarts
        		scheduledWakeUps.clear();
				for (String driverName: loadedDrivers.keySet()) {
					Log.d(TAG, "terminating driver '" + driverName + "'");
					loadedDrivers.get(driverName).terminate();
					Log.d(TAG, "notified driver '" + driverName + "'");
				}
				//give the drivers ~4s to finalize their termination
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
				}
			}
		});
        
        //start the web interface
        startWebServer();
        
        
        
		//enter main loop
        while (true) {
        	//TODO keep track of the last time a message was sent to/received from each remote module
        	//+ if the time period is too great, then re-query the remote modules, possibly removing
        	//+ them from the known list if no response is detected
        	
        	//Check for incoming messages, only process the first byte before breaking off
        	//+ to a more appropriate method
        	if (messageCenter.isMessageAvailable()) {
        		routeIncomingMessage(messageCenter.getMessage());
        	}
        	
        	//Check for scheduled driver wake ups.
        	for (Driver d: scheduledWakeUps.keySet()) {
        		Long wakeTime = scheduledWakeUps.get(d);
        		Long curTime = System.currentTimeMillis();
        		if (wakeTime <= curTime && wakeTime != 0) {
        			try {
        				wakeDriver(d.getName());
        			} catch (Exception e) {
        				Log.e(TAG, "error restarting driver '" + d.getName() + "'");
        				scheduledWakeUps.remove(d);
        			}
        		}
        	}
        	
        	
	    	Thread.yield();
	    	Thread.sleep(100);
        }
	}
}
