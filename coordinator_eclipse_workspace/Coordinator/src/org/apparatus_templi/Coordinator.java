package org.apparatus_templi;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
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
	private static ConcurrentHashMap<String, Driver> loadedDrivers    = new ConcurrentHashMap<String, Driver>();
	private static ConcurrentHashMap<Driver, Thread> driverThreads    = new ConcurrentHashMap<Driver, Thread>();
	private static ConcurrentHashMap<Driver, Long>   scheduledWakeups = new ConcurrentHashMap<Driver, Long>();
	private static int portNum;
	private static final int DEFAULT_PORT = 2024;
	private static String serialPortName = null;
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
			//Log.d(TAG, "adding remote module '" + destination + "' to the list of known modules");
			remoteModules.put(destination, "");
		}
		
		if (loadedDrivers.containsKey(destination)) {
			Driver driver = loadedDrivers.get(destination);
			if (driverThreads.get(driver).getState() == Thread.State.TERMINATED) {
				Log.d(TAG, "waking terminated driver '" + destination + "' for incoming message");
				if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
					driver.queueBinary(m.getData());
				} else {
					driver.queueCommand(new String(m.getData()));
				}
				wakeDriver(driver);
			} else {
				if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
					driver.receiveBinary(m.getData());
				} else {
					driver.receiveCommand(new String(m.getData()));
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
	 * Wakes the given driver if it's thread state is TERMINATED.
	 * @param d the {@link Driver} to wake.
	 */
	private static void wakeDriver(Driver d) {
		if (driverThreads.get(d).getState() == Thread.State.TERMINATED) {
			new Thread(d).start();
		}
	}
	
	/**
	 * Sends the given command to a specific remote module. The message will be formatted
	 * 	to fit the protocol version that this module supports (if known), otherwise the
	 * 	message will be formatted as the most recent protocol version.
	 * @param caller a reference to the calling {@link Driver}
	 * @param command the command to send to the remote module
	 */
	static synchronized boolean sendCommand(Driver caller, String command) {
//		Log.d(TAG, "sendCommand()");
		boolean messageSent = false;
		
		if (connectionReady && caller.name != null) {
			messageSent = messageCenter.sendCommand(caller.name, command);
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
	static synchronized String sendCommandAndWait(Driver caller, String command, int waitPeriod) {
//		Log.d(TAG, "sendCommandAndWait()");
		//TODO this method provides no security mechanisms. It is possible that the calling
		//+ driver "a" could call this method with a moduleName "b". The first response
		//+ to "b" within the waitPeriod will be routed back to "a".
		String responseData = null;
		if (waitPeriod <= 6 && caller.name != null) {
			sendCommand(caller, command);
			long endTime = (System.currentTimeMillis() + ((1000) * waitPeriod));
			while (System.currentTimeMillis() < endTime) {
				if (messageCenter.isMessageAvailable()) {
					Message m = messageCenter.getMessage();
					if (m.getDestination().equals(caller.name)) {
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
	static synchronized boolean sendBinary(Driver caller, byte[] data) {
		Log.d(TAG, "sendBinary()");
		boolean messageSent = false;
		
		if (connectionReady && caller.name != null) {
			messageCenter.sendBinary(caller.name, data);
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
	static synchronized int storeTextData(String driverName, String dataTag, String data) {
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
	static synchronized int storeBinData(String driverName, String dataTag, byte[] data) {
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
	static synchronized String readTextData(String driverName, String dataTag) {
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
	static synchronized Byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "readBinData()");
		return null;
	}
	
	/**
	 * Schedules a {@link org.apparatus_templi.Driver} to re-create
	 * this driver at the given time. If a driver's state
	 * {@link org.apparatus_templi.Driver#getState()} is
	 * {@link java.lang.Thread.State.TERMINATED} at the time of the wake up the
	 * Driver will be re-created. If the Driver's state is not
	 * TERMINATED then no action will be taken. WARNING: make sure
	 * that your driver stores any needed information before
	 * exiting its run() method.
	 * @param caller
	 * @param wakeTime
	 */
	static synchronized void scheduleWakeup(Driver caller, long wakeTime) {
		if (caller != null) {
			scheduledWakeups.put(caller, wakeTime);
			Log.d(TAG, "scheduled a wakup for driver '" + caller.getModuleName() + "' at time: " + wakeTime);
		}
	}
	
	
	/**
	 * Pass a message to the driver specified by name.
	 * @param destination the unique name of the driver
	 * @param source the source of this message, either the name of the calling driver
	 * 	or null. If null, this command originated from the Coordinator
	 * @param command the command to pass
	 */
	static synchronized boolean passCommand(String source, String destination, String command) {
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
	static synchronized String requestWidgetXML(String driverName) {
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
	static synchronized String requestFullPageXML(String driverName) {
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
	static synchronized boolean isModulePresent(String moduleName) {
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
	static synchronized ArrayList<String> getSensorList(String driverName) {
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
	static synchronized ArrayList<String> getControllerList(String driverName) {
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
	static synchronized ArrayList<String> getLoadedDrivers() {
		Log.d(TAG, "getLoadedDrivers()");
		ArrayList<String> driverList = new ArrayList<String>();
		for (String driverName: loadedDrivers.keySet()) {
			driverList.add(driverName);
		}
		
		return driverList;
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
			
			if (cmd.hasOption("port")) {
				try {
					portNum = Integer.valueOf(cmd.getOptionValue("port"));
				} catch (IllegalArgumentException e) {
					Log.e("Coordinator", "Bad port number given, setting to default value");
					portNum = DEFAULT_PORT;
				}
			} else {
				Log.d(TAG, "default port " + DEFAULT_PORT + " selected");
				portNum = DEFAULT_PORT;
			}
			
			//if we were given a preferred port we will pass it to SerialConnection
			//+ when initialized
			if (cmd.hasOption("serial")) {
					serialPortName = cmd.getOptionValue("serial");
			}
		}  catch (ParseException e) {
			System.out.println("Error processing options: " + e.getMessage());
			new HelpFormatter().printHelp("Diff", options);
			Log.e(TAG, "Error parsing command line options, exiting");
			System.exit(1);
		}
		
		//open the serial connection
		serialConnection = new SerialConnection(serialPortName);
		if (!serialConnection.isConnected()) {
			Log.e(TAG, "could not connect to serial port, exiting");
			System.err.println("could not connect to serial port, exiting");
			System.exit(1);
		}
		
		//start the message center
		messageCenter =  MessageCenter.getInstance();
		messageCenter.setSerialConnection(serialConnection);
		
		//block until the local Arduino is ready
		System.out.print(TAG + ":" +  "Waiting for local link to be ready.");
		byte[] sBytes = messageCenter.readBytesUntil((byte)0x0A);
//		serialConnection.flushSerialLine();
		String sString = new String(sBytes);
		if (sString.endsWith("READY")) {
			connectionReady = true;
		}

		if (!connectionReady) {
			Log.e(TAG, "could not find a local Arduino connection, exiting");
			System.exit(1);
		} else {
			Log.c(TAG, "Local link ready.");
		}
		
		//query for remote modules.  Since the modules may be slow in responding
		//+ we will wait for a few seconds to make sure we get a complete list
		System.out.print(TAG + ":" + "Querying modules (6s wait).");
		messageCenter.beginReadingMessages();
		//begin processing incoming messages
		new Thread(messageCenter).start();
		queryRemoteModules();
		for (int i = 0; i < 6; i++) {
			if (messageCenter.isMessageAvailable()) {
				routeIncomingMessage(messageCenter.getMessage());
			}
			
			System.out.print(".");
			Thread.sleep(1000);
		}
		System.out.println();
		
		if (remoteModules.size() > 0) {
			Log.c(TAG, "Found " + remoteModules.size() + " modules :" + remoteModules.toString());
		} else {
			Log.c(TAG, "Did not find any remote modules.");
		}
        
        
		Log.c(TAG, "Initializing drivers...");
        //initialize the drivers
//        Driver driver1 = new LedFlash();
        
        //test adding a driver with the same name
//        Driver driver2 = new StatefullLed();
        
        //testing large commands
//		Driver driver3 = new LargeCommands();
		
		//testing echo driver
		Driver driver4 = new Echo();
		
		//testing lazy driver
//		Driver driver5 = new LazyDriver();
     
		//testing sleep driver
		Driver driver6 = new SleepyDriver();
		
//		loadDriver(driver1);
//		loadDriver(driver2);
//		loadDriver(driver3);
		loadDriver(driver4);
//		loadDriver(driver5);
//		loadDriver(driver6);
        
        
        //start the drivers
        for (String driverName: loadedDrivers.keySet()) {
        	Log.c(TAG, "Starting driver " + driverName);
        	Thread t = new Thread(loadedDrivers.get(driverName));
        	driverThreads.put(loadedDrivers.get(driverName), t);
        	t.start();
        }
        
        //start the web interface
        Log.c(TAG, "Starting web server on port " + portNum);
        new Thread(new SimpleHttpServer(portNum)).start();
        
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
        	for (Driver d: scheduledWakeups.keySet()) {
        		Long wakeTime = scheduledWakeups.get(d);
        		Long curTime = System.currentTimeMillis();
        		if (wakeTime <= curTime) {
        			if (loadedDrivers.containsKey(d.getName())) {
        				Thread t = driverThreads.get(d);
        				if (t.getState() == Thread.State.TERMINATED) {
        					Log.d(TAG, "waking driver '" + d.getName() + "' of type :" + d.getClass());
        					scheduledWakeups.remove(d);
        					driverThreads.remove(d);
        					loadedDrivers.remove(d.getName());
        					//use reflection to generate a new driver of the appropriate class
        					try {
								Constructor<?> thisConst = d.getClass().getConstructor();
								Object o = thisConst.newInstance();
								Driver newDriver = (Driver)o;
								loadedDrivers.put(newDriver.getName(), newDriver);
								Thread newThread = new Thread(newDriver);
								driverThreads.put(newDriver, newThread);
								newThread.start();
							} catch (Exception e) {
								//something went wrong re-creating the Driver
								Log.e(TAG, "error re-creating driver " + d.getName() + " for wake up, driver is no longer valid.");
							} 
        					d = null; t = null;	//hopefully there are no other references
        					
        				}
        			}
        		}
        	}
        	
        	
	    	Thread.yield();
	    	Thread.sleep(100);
        }
	}
}
