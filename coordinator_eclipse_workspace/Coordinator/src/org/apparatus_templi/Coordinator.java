package org.apparatus_templi;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Coordinator
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Coordinator {
	private static final String TAG = "Coordinator";

	private static HashMap<String, String> remoteModules = new HashMap<String, String>();
	private static HashMap<String, Driver> loadedDrivers = new HashMap<String, Driver>();
	private static int portNum;
	private static final int DEFAULT_PORT = 2024;
	private static String serialPortName = null;
	private static SerialConnection serialConnection;
	private static MessageCenter messageCenter = MessageCenter.getInstance();
	private static boolean connectionReady = false;	

	private static synchronized void routeIncomingMessage(Message m) {
//		Log.d(TAG, "routeIncomingMessage()");
		try {
			String destination = new String(m.getDestination(), "UTF-8").trim();
			if (!isModulePresent(destination)) {
				Log.d(TAG, "adding remote module '" + destination + "' to the list of known modules");
				remoteModules.put(destination, "");
			}
			
			if (loadedDrivers.containsKey(destination)) {
				Driver driver = loadedDrivers.get(destination);
				if (driver.getState() == Thread.State.TERMINATED) {
					Log.d(TAG, "waking terminated driver '" + destination + "' for incoming message");
					if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
						driver.queueBinary(m.getData());
					} else {
						driver.queueCommand(new String(m.getData()));
					}
				} else {
					if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
						driver.receiveBinary(m.getData());
					} else {
						driver.receiveCommand(new String(m.getData()));
					}
				}
			} else {
				Log.w(TAG, "incoming message to " + destination + " could not be routed: no such driver loaded");
			}
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "unable to convert message's destination field into a string, discarding");
		}
	}

	/**
	 * Sends a query string to all remote modules "ALL:READY?"
	 */
	private static synchronized void queryRemoteModules() {
//		Log.d(TAG, "queryRemoteModules()");
		sendCommand("ALL", "READY?");
	}
	
	/**
	 * Sends the given command to a specific remote module. The message will be formatted
	 * 	to fit the protocol version that this module supports (if known), otherwise the
	 * 	message will be formatted as the most recent protocol version.
	 * @param moduleName the unique name of the remote module
	 * @param command the command to send to the remote module
	 */
	static synchronized boolean sendCommand(String moduleName, String command) {
//		Log.d(TAG, "sendCommand()");
		boolean messageSent = false;
		
		if (connectionReady) {
			messageSent = messageCenter.sendCommand(moduleName, command);
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
		}
		
		return messageSent;
	}

	
	/**
	 * Sends a message to a remote module and waits waitPeriod seconds for a response.
	 * @param name the unique name of the remote module
	 * @param command the command to send to the remote module 
	 * @param waitPeriod how many seconds to wait for a response.  Maximum period to wait
	 * 	is 6 seconds.
	 * @return the byte[] of data that the remote module responded with, or null if there
	 * 	was no response.
	 */
	static synchronized byte[] sendCommandAndWait(String name, String command, int waitPeriod) {
//		Log.d(TAG, "sendCommandAndWait()");
		//TODO: since this is a blocking method this could easily be abused by the drivers to bring down
		//+ the system.  It might need to be removed, or to limit the number of times any driver can call
		//+ this method in a given time period.
		byte[] data = null;
		
		return data;
	}
	
	/**
	 * Sends binary data over the serial connection to a remote module.
	 * 	Does not yet break byte[] into chunks for transmission.  Make sure
	 * 	that the size of the transmission is not larger than a single packet's
	 * 	max payload size (around 80 bytes).
	 * @param moduleName the unique name of the remote module
	 * @param data the binary data to send
	 */
	static synchronized boolean sendBinary(String moduleName, byte[] data) {
		Log.d(TAG, "sendBinary()");
		boolean messageSent = false;
		
		if (connectionReady) {
			messageCenter.sendBinary(moduleName, data);
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
	 * Pass a message to the driver specified by name.
	 * @param toDriver the unique name of the driver
	 * @param fromDriver the source of this message, either the name of the calling driver
	 * 	or null. If null, this command originated from the Coordinator
	 * @param command the command to pass
	 */
	synchronized boolean passCommand(String fromDriver, String toDriver, String command) {
		Log.d(TAG, "passCommand()");
		//TODO verify source name
		//TODO check for reserved name in toDriver
		boolean messagePassed = false;
		if (loadedDrivers.containsKey(toDriver)) {
			Driver destDriver = loadedDrivers.get(toDriver);
			if (destDriver.getState() != Thread.State.TERMINATED) {
				loadedDrivers.get(toDriver).receiveCommand(command);
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
	synchronized String requestWidgetXML(String driverName) {
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
	synchronized String requestFullPageXML(String driverName) {
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
//		Log.d(TAG, "isModulePresent()");
		return remoteModules.containsKey(moduleName);
	}
	
	/**
	 * Returns a list of all loaded drivers.
	 * @return an ArrayList<String> of driver names.
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
		System.out.print(TAG + ":" + "Querying modules.");
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
        Driver driver2 = new StatefullLed();
        
        //testing large commands
		Driver driver3 = new LargeCommands();
     
        //only drivers with valid names are added
        //TODO make this generic
//        if (driver1.getModuleName() != null) {
//        	if (!loadedDrivers.containsKey(driver1.getModuleName())) {
//		        loadedDrivers.put(driver1.getModuleName(), driver1);
//		        Log.d(TAG, "driver " + driver1.getModuleName() + " of type " + driver1.getModuleType() + " initialized");
//        	} else {
//        		Log.w(TAG, "error loading driver " + driver1.getClass().getName() + "a driver with the name " +
//        				driver1.getModuleName() + " already exists");
//        	}
//        }
        
        if (driver2.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver2.getModuleName())) {
		        loadedDrivers.put(driver2.getModuleName(), driver2);
		        Log.d(TAG, "driver " + driver2.getModuleName() + " of type " + driver2.getModuleType() + " initialized");
        	} else {
        		Log.e(TAG, "error loading driver " + driver2.getClass().getName() + " a driver with the name " +
        				driver2.getModuleName() + " already exists");
        	}
        }
		
		if (driver3.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver3.getModuleName())) {
		        loadedDrivers.put(driver3.getModuleName(), driver3);
		        Log.d(TAG, "driver " + driver3.getModuleName() + " of type " + driver3.getModuleType() + " initialized");
        	} else {
        		Log.e(TAG, "error loading driver " + driver3.getClass().getName() + " a driver with the name " +
        				driver3.getModuleName() + " already exists");
        	}
        }
        
        
        //start the drivers
        for (String driverName: loadedDrivers.keySet()) {
        	Log.c(TAG, "Starting driver " + driverName);
        	(new Thread(loadedDrivers.get(driverName))).start();
        }
        
        //start the web interface
        /*
         * This may not work because there is not reason to pass the port number to the constructor because 
         * you have to call the bind method and send it an InetSockAddress with a port number
         * may need to be something like 
         * new SimpletHttpServer(new InetSockAddress(InetAddress.getHostByName("127.0.0.1"). portNum), portNum).bind().start()
         * Creating the server like this causes you to no longer have a reference to the it, so how do you plan on calling any other methods on
         * behalf of the SimpleHttpServer?
         * 
         */
        Log.c(TAG, "Starting web server on port " + portNum);
        new SimpleHttpServer().start();
        
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
        	
        	
	    	Thread.yield();
	    	Thread.sleep(100);
        }
	}
}
