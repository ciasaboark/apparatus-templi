package org.apparatus_templi;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
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
import org.apparatus_templi.driver.Echo;
import org.apparatus_templi.driver.LargeCommands;
import org.apparatus_templi.driver.LazyDriver;
import org.apparatus_templi.driver.LedFlash;
import org.apparatus_templi.driver.Local;
import org.apparatus_templi.driver.MotionGenerator;
import org.apparatus_templi.driver.SensorModule;
import org.apparatus_templi.driver.SleepyDriver;
import org.apparatus_templi.driver.StatefullLed;

import com.sun.org.apache.xerces.internal.util.URI;

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
	private static final int DEFAULT_PORT = 8000;
	private static final String DEFAULT_CONFIG = "./coordinator.conf";

	private static HashMap<String, String> remoteModules   = new HashMap<String, String>();
	private static ConcurrentHashMap<String, Driver> loadedDrivers     = new ConcurrentHashMap<String, Driver>();
	private static ConcurrentHashMap<Driver, Thread> driverThreads     = new ConcurrentHashMap<Driver, Thread>();
	private static ConcurrentHashMap<Driver, Long>   scheduledWakeUps = new ConcurrentHashMap<Driver, Long>();
	private static ConcurrentHashMap<Event, ArrayList<Driver>> eventWatchers = new ConcurrentHashMap<Event, ArrayList<Driver>>();
	private static Integer portNum = null;
	private static String configFile;
	private static boolean dummySerial = false;
	private static String serialPortName = null;
	private static String webResourceFolder;
	private static String driverList = "";
	private static boolean serverBindLocalhost = false;
	private static boolean serverBindLoopback = false;
	private static SerialConnection serialConnection;
	private static MessageCenter messageCenter = MessageCenter.getInstance();
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
					Driver newDriver = restartDriver(driver.getName(), false);
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
			//Log.w(TAG, "incoming message to " + destination + " could not be routed: no such driver loaded");
		}
	}

	/**
	 * Sends a query string to all remote modules "ALL:READY?"
	 */
	private static synchronized void queryRemoteModules() {
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
		boolean isDriverLoaded = false;
		if (d.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(d.getModuleName()) && d.getDriverType() != Driver.TYPE) {
		        loadedDrivers.put(d.getModuleName(), d);
		        Log.d(TAG, "driver " + d.getModuleName() + " of type " + d.getDriverType() + " initialized");
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
	private static synchronized Driver restartDriver(String driverName) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return restartDriver(driverName, true);
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
	private static synchronized Driver restartDriver(String driverName, boolean autoStart) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Driver newDriver = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver d = loadedDrivers.get(driverName);
			Thread t = driverThreads.get(d);
			if (t.getState() == Thread.State.TERMINATED) {
				Log.d(TAG, "restarting driver '" + d.getName() + "' of class '" + d.getClass() + "' of type '" + d.getDriverType() + "'");
				scheduledWakeUps.remove(d);
				driverThreads.remove(d);
//				loadedDrivers.remove(d.getName());
//				//use reflection to generate a new driver of the appropriate class
//				Constructor<?> thisConst = d.getClass().getConstructor();
//				Object o = thisConst.newInstance();
//				newDriver = (Driver)o;
//				loadedDrivers.put(newDriver.getName(), newDriver);
				newDriver = d;
				Thread newThread = new Thread(newDriver);
				driverThreads.put(newDriver, newThread);
				if (autoStart) {
					newThread.start();
				}
				
				d = null; t = null;	//hopefully there are no other references
			} else {
				newDriver = d;
				try {
					scheduledWakeUps.remove(d);
					d.wake();
				} catch (Exception e) {
					Log.d(TAG, "could not nofity object");
				}
			}
		}
		return newDriver;
	}
	
	static void exitWithReason(String reason) {
		Log.t(TAG, reason);
		System.exit(1);
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
	public static synchronized void scheduleWake(Driver caller) {
		if (caller != null) {
			scheduledWakeUps.put(caller, 0l);
//			try {
//				caller.wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
	public static synchronized void scheduleWake(Driver caller, long wakeTime) {
		if (caller != null) {
			scheduledWakeUps.put(caller, wakeTime);
			Log.d(TAG, "scheduled a wakup for driver '" + caller.getModuleName() + "' in " + (wakeTime - System.currentTimeMillis()) / 1000 + " seconds.");
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
			if (d.getDriverType().equals(SensorModule.TYPE)) {
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
			if (d.getDriverType().equals(ControllerModule.TYPE)) {
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
		if (d instanceof EventGenerator) {
			Log.d(TAG, "incoming event '" + e.eventType + "' from driver '" + d.getName() + "'");
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' not allowed to generate events");
		}
		//TODO check a hash table to see who to notify
	}
	
	
	public static void registerEventWatch(Driver d, Event e) {
		if (d instanceof EventWatcher) {
			Log.d(TAG, " driver '" + d.getName() + "' requested to be notified of events of type '" + e.eventType + "'.");
			ArrayList<Driver> curList = eventWatchers.get(e);
			if (curList == null) {
				curList = new ArrayList<Driver>();
			}
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' can not listen for events of type '" + e.eventType + "', must implement EventWatcher.");
		}
	}
	
	public static void removeEventWatch(Driver d, Event e) {
		//TODO remove this driver from the event watcher list
	}

	public static void main(String argv[]) throws InterruptedException, IOException {
		//turn off debug messages
//		Log.setLogLevel(Log.LEVEL_WARN);
		
		Log.c(TAG, "Starting");
		//Using apache commons cli to parse the command line options
		Options options = new Options();
		options.addOption("help", false, "Display this help message.");
		@SuppressWarnings("static-access")
		Option portOption = OptionBuilder.withArgName("port")
				.hasArg()
				.withDescription("Bind the server to the given port number")
				.create("port");
		options.addOption(portOption);
		
		@SuppressWarnings("static-access")
		Option serialOption = OptionBuilder.withArgName("serial")
				.hasArg()
				.withDescription("Connect to the arduino on serial interface")
				.create("serial");
		options.addOption(serialOption);
		
		@SuppressWarnings("static-access")
		Option configOption = OptionBuilder.withArgName("config file")
				.hasArg()
				.withDescription("Path to the configuration file")
				.create("config");
		options.addOption(configOption);
		
		@SuppressWarnings("static-access")
		Option resourcesOption = OptionBuilder.withArgName("folder path")
				.hasArg()
				.withDescription("Web frontend will use resources in the specified folder")
				.create("resources");
		options.addOption(resourcesOption);
		
		@SuppressWarnings("static-access")
		Option dummyOption = OptionBuilder.withArgName("dummyserial")
				.withDescription("Do not initialize a real serial connection.")
				.create("dummyserial");
		options.addOption(dummyOption);
		
		
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
			if (cmd.hasOption("config")) {
				configFile = cmd.getOptionValue("config");
			} else {
				configFile = DEFAULT_CONFIG;
			}
			
			//Read the values from the configuration file.  If any command line
			//+ parameters were passed, they should overwrite values read, so we
			//+ will continue processing them later.
			try {
				Properties props = new Properties();
				FileInputStream fin = new FileInputStream(configFile);
				props.load(fin);
				fin.close();
				
				//read the port number and serial name from the configuration file
				if (props.containsKey("port")) {
					try {
						portNum = Integer.parseInt(props.getProperty("port"));
						Log.d(TAG, "read config file property 'port' as '" + props.getProperty("port") + "'");
					} catch (NumberFormatException e) {
						Log.w(TAG,  "error reading port number from configuration file, setting to default");
						portNum = DEFAULT_PORT;
					}
				}
				if (props.containsKey("serial")) {
					serialPortName = props.getProperty("serial");
					Log.d(TAG, "read config file property 'serial' as '" + props.getProperty("serial") + "'");
				}
				if (props.containsKey("resources")) {
					webResourceFolder = props.getProperty("resources");
				}
				if (props.containsKey("drivers")) {
					driverList = props.getProperty("drivers");
				}
				if (props.containsKey("server_bind_loopback")) {
					if (props.get("server_bind_loopback").toString().toLowerCase().equals("true")) {
						serverBindLoopback = true; 
					}
				}
				if (props.containsKey("server_bind_local")) {
					if (props.get("server_bind_local").toString().toLowerCase().equals("true")) {
						serverBindLocalhost = true; 
					}
				}
			} catch (IOException | NullPointerException e) {
				Log.w(TAG, "unable to read configuration file '" + configFile + "'");
				
			}
			
			//If the user specified a port number then we will only
			//try binding to that port, else we try the default port number
			if (cmd.hasOption("port")) {
				try {
					portNum = Integer.valueOf(cmd.getOptionValue("port"));
				} catch (IllegalArgumentException e) {
					Log.e("Coordinator", "Bad port number given, setting to default value");
					portNum = DEFAULT_PORT;
				}
			}
			
			//Its possible that a port number was not specified as a command line option
			//+ or through the config file.  If so use the default port
			if (portNum == null) {
				Log.d(TAG, "using default port " + DEFAULT_PORT);
				portNum = DEFAULT_PORT;
			}
			
			if (cmd.hasOption("resources")) {
				webResourceFolder = cmd.getOptionValue("resources");
			}
			
			if (cmd.hasOption("dummyserial")) {
				Log.d(TAG, "using dummy serial connection");
				dummySerial = true;
			}
			
			//if we were given a preferred port we will pass it to SerialConnection
			//+ when initialized
			if (cmd.hasOption("serial")) {
				serialPortName = cmd.getOptionValue("serial");
			}
			
			
			
		}  catch (ParseException e) {
			System.out.println("Error processing options: " + e.getMessage());
			new HelpFormatter().printHelp("Diff", options);
			Coordinator.exitWithReason("Error parsing command line options");
		}
		
		//open the serial connection
		if (dummySerial) {
			serialConnection = new DummySerialConnection();
		} else {
			serialConnection = new UsbSerialConnection(serialPortName);
		}
		
		if (!serialConnection.isConnected()) {
			Coordinator.exitWithReason("could not connect to serial port '" + serialPortName + "'");
		}
		
		//start the message center
		messageCenter =  MessageCenter.getInstance();
		messageCenter.setSerialConnection(serialConnection);
		
		//block until the local Arduino is ready
		System.out.print(TAG + ":" +  "Waiting for local link to be ready.");
		if (!dummySerial) {
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
			Log.d(TAG, "Local dummy link ready");
		}
		
		//query for remote modules.  Since the modules may be slow in responding
		//+ we will wait for a few seconds to make sure we get a complete list
		System.out.print(TAG + ":" + "Querying modules (6s wait).");
		messageCenter.beginReadingMessages();
		//begin processing incoming messages
		new Thread(messageCenter).start();
		
		//if we are using the dummy serial connection then there is no point in waiting
		//+ 6 seconds for a response from the modules
		if (!dummySerial) {
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
        
        
		//Load all drivers specified in the config file
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
			Log.w(TAG, "No drivers were specified in the configuration file: '" + configFile + "', nothing will be loaded");
		}
		
        //start the drivers
        for (String driverName: loadedDrivers.keySet()) {
        	Log.c(TAG, "Starting driver " + driverName);
        	Thread t = new Thread(loadedDrivers.get(driverName));
        	driverThreads.put(loadedDrivers.get(driverName), t);
        	t.start();
        }
        
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
        
        //start the web interfaces
        if (serverBindLocalhost) {
        	Log.c(TAG, "Starting web server on port " + portNum + " bound to localhost address " +
        			InetAddress.getLocalHost());
        } else {
        	Log.c(TAG, "Starting web server on port " + portNum + " bound to loopback address");
        }
    	
        SimpleHttpServer server = new SimpleHttpServer(portNum, portNum == DEFAULT_PORT? false : true,
        		serverBindLocalhost ? true : false);
        if (webResourceFolder != null) {
        	server.setResourceFolder(webResourceFolder);
        }
        new Thread(server).start();
        
        
        
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
        				restartDriver(d.getName());
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
