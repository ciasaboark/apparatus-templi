package org.apparatus_templi;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
//	private static ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
	private static ArrayDeque<Byte> byteBuffer = new ArrayDeque<Byte>();
	private static HashMap<String, Integer> remoteModules = new HashMap<String, Integer>();
//	private static HashSet<String> remoteModules = new HashSet<String>();
	private static HashMap<String, Driver> loadedDrivers = new HashMap<String, Driver>();
	private static int portNum;
	private static final int DEFAULT_PORT = 2024;
	private static String serialPortName = null;
	/*
	 * Bit-mask flags for the transmission start byte
	 */
	private static final byte TEXT_TRANSMISSION = (byte)0b0000_0000;
	private static final byte BIN_TRANSMISSION  = (byte)0b1000_0000;
	//the safety bit is reserved and always 1.  This is to make sure that the
	//+ header byte is never equal to 0x0A (the termination byte)
	private static final byte SAFETY_BIT   		= (byte)0b0010_0000;
	@Deprecated
	private static final byte PROTOCOL_V0 	 	= (byte)0b0000_0000;
	private static final byte PROTOCOL_V1  		= (byte)0b0000_0001;
	private static final byte PROTOCOL_V2  		= (byte)0b0000_0010;
	
	private static final byte protocolVersion = PROTOCOL_V1;
	
	//Separates the destination from the command
	private static String headerSeperator = ":";
	
	//a single line-feed char marks the end of the transmission.  If the command
	//+ contains any matching bytes they must be doubled to avoid early term.
	private static byte termByte = (byte)0x0A;
	
	private static SerialConnection serialConnection;
	private static boolean connectionReady = false;
	
	
	
	
	//TODO change this to a singleton
	public Coordinator() {
		super();
	}
	
	/**
	 * Send a command using protocol version 0
	 * @param moduleName
	 * @param command
	 */
	@Deprecated
	private static synchronized boolean sendCommand_V0(String moduleName, String command) {
		boolean messageSent = false;
		boolean sendMessage = true;
//		Log.d(TAG, "sending message as protocol 0");
		byte[] bytes = {0b0};
		
		String message = moduleName + ":" + command + "\n";
		try {
			bytes = message.getBytes("US-ASCII");
//			Log.d(TAG, "message in hex: '" + DatatypeConverter.printHexBinary(bytes) + "'");
//			Log.d(TAG, "message in ascii (trimmed): '" + new String(bytes).trim() + "'");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "sendCommandV0() error converting message to ASCII, discarding");
			sendMessage = false;
		}
		
		if (sendMessage) {
			messageSent = serialConnection.writeData(bytes);
		}
		
		return messageSent;
	}
	
	/**
	 * Send a command using protocol version 1
	 * @param moduleName
	 * @param command
	 */
	private static synchronized boolean sendCommand_V1(String moduleName, String command) {
		boolean messageSent = false;
//		Log.d(TAG, "sending message as protocol 1");
		//TODO make sure size of command bytes is not larger than a single zigbee packet,
		//+ break into chunks if needed
		
		//TODO check command for line-feed chars, replace with double newlines (or refuse
		//+ to route the command).
		
		if (serialConnection.isConnected()) {
		
			//the arduino expects chars as 1 byte instead of two, convert command
			//+ to ascii byte array, then tack on the header, name, and footer
			byte startByte = (byte)(TEXT_TRANSMISSION | SAFETY_BIT | protocolVersion);
			byte[] destinationBytes = {0b0};
			byte[] headerSeperatorByte = {0b0};
			byte[] commandBytes = {0b0};
			
			boolean sendMessage = true;
			
			//convert the destination address to ascii byte array
			try {
				destinationBytes = moduleName.getBytes("US-ASCII");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "error converting remote module name '" + moduleName + "' to US-ASCII encoding.");
				sendMessage = false;
			}
			
			//convert the destination separator to ascii byte array
			try {
				headerSeperatorByte = headerSeperator.getBytes("US-ASCII");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "error converting header seperator to ascii byte array.");
				sendMessage = false;
			}
			
			//convert the command to ascii byte array
			try {
				commandBytes = command.getBytes("US-ASCII");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "error converting command '" + command +"' to US-ASCII encoding.");
				sendMessage = false;
			}
			
			
			if (sendMessage) {
				//reserve enough room for the start/term bytes, the header, and the command
				ByteBuffer bBuffer = ByteBuffer.allocate(1 + destinationBytes.length + headerSeperatorByte.length + commandBytes.length + 1);
				bBuffer.put(startByte);
				bBuffer.put(destinationBytes);
				bBuffer.put(headerSeperatorByte);
				bBuffer.put(commandBytes);
				bBuffer.put(termByte);
				
	//			Log.d(TAG, "sending bytes 0x" + DatatypeConverter.printHexBinary(bBuffer.array()));
	//			Log.d(TAG, "sending ascii string '" + new String(bBuffer.array()) + "'");
				messageSent = serialConnection.writeData(bBuffer.array());
			} else {
				Log.w(TAG, "error converting message to ascii byte[]. Message not sent");
			}
		}
		
		return messageSent;
	}
	
	@Deprecated
	private static synchronized boolean sendBinary_V0(String moduleName, byte[] data) {
		Log.e(TAG, "error, binary transmission not supported under protocol v0");
		return false;
	}
	
	private static synchronized boolean sendBinary_V1(String moduleName, byte[] data) {
		Log.e(TAG, "error, binary transmission for protocol version 1 not yet completed");
		return false;
	}
	
	/**
	 * Reads a full message from the input. The complexity of the code is due to
	 * 	protocol v1 method of terminating a message. A properly formatted message
	 *	is terminated by a single newline (0x0A). If the binary data contains
	 *	any native newlines they would have been converted to double newlines
	 *	(0x0A0A) before the transmission began. If a newline is found during
	 *	decoding, we have to check the next byte to see if this was the terminating
	 *	byte, or the first of a doubled sequence. A message terminated with a single
	 *	newline might not be immediately followed by another byte, so we have to
	 *	limit the time spent polling.
	 * @param startByte
	 * @throws InterruptedException 
	 */
	private static void readBinMessage_V1(byte startByte) throws InterruptedException {
		boolean messageDone = false;
		ByteArrayOutputStream message = new ByteArrayOutputStream();
		while (!messageDone) {
			//block until input is available
			while (byteBuffer.size() < 1){}
			byte inByte = byteBuffer.poll();
			if (inByte == 0x0A) { //the newline
				//check for a following newline
				//Since we don't know that there will be more input incoming we need
				//+ to limit this check to ~100ms. If a byte can't be read within
				//+ that time frame then we can assume that this was a terminating
				//+ newline
				byte tmpByte;
				for (int i = 0; i < 10; i++) {
					if (byteBuffer.size() > 0) {
						tmpByte = byteBuffer.poll();
						if (tmpByte == 0x0A) { //two newlines in a row, replace with a single
							message.write(inByte);
						} else {
							//the transmission is done, place the tmpByte back onto the front
							//+ of the queue for later processing
							byteBuffer.addFirst(tmpByte);
							messageDone = true;
							processBinMessage(message.toByteArray());
							break;
						}
					} else {
						Thread.sleep(10);
					}
				}
			} else {
				message.write(inByte);
			}
		}
	}

	private static void readTextMessage_V1(byte startByte) {
		String message = "";
		boolean messageDone = false;
		while (!messageDone) {
			//block until input is ready
			while (byteBuffer.size() < 1){}
			byte inByte = byteBuffer.poll();
			if (inByte != 0x0A) {
				message += new String(new byte[] {inByte});
			} else {
				messageDone = true;
			}
		}
		
		processTextMessage(message);	
	}
	
	private static synchronized void processTextMessage(String message) {
		//break the message into a destination and command
		String destination = "";
		String command = "";
		boolean routeMessage = false;
		
		if (message.indexOf(":") == -1) {
			//this message does not appear to be formatted correctly, but it might be the ready
			//+ signal from the attached arduino
			if (message.endsWith("READY")) {
				//this was the startup message from the attached arduino
				connectionReady = true;
			} else {
				Log.w(TAG, "processTextMessage() given an improperly formatted message to decode: " + message);
			}
		} else {
			//pull out the destination
			destination = message.substring(0, message.indexOf(":"));
			command = message.substring(message.indexOf(":"), message.length());
			routeMessage = true;
		}
		
		if (routeMessage) {
			routeIncomingMessage(destination, command);
		}
	}
	
	private static synchronized void processBinMessage(byte[] message) {
		String destination = "";
		String messageAsString = new String(message);
		int headerSeperator = messageAsString.indexOf(":");
		byte[] data = new byte[0];	//re-initialized to the correct size later
		boolean routeMessage = false;
		
		if (headerSeperator == -1) {
			//this does not appear to be a properly formatted message.  The local arduino
			//+ should never send its startup "READY" marked as a binary transmission,
			//+ but we will check for that possibility anyway
			if (messageAsString.endsWith("READY")) {
				//this was the startup message from the arduino
				connectionReady = true;
			} else {
				Log.w(TAG, "processBinMessage() given an improperly formatted message to decode");
			}
		} else {
			destination = messageAsString.substring(0, headerSeperator);
			data = new byte[message.length - headerSeperator];
			//copy the data from the message into the data payload
			for (int i = 0; i < message.length; i++, headerSeperator++) {
				data[i] = message[headerSeperator];
			}
			routeMessage = true;
		}
		
		if (routeMessage) {
			routeIncomingMessage(destination, data);
		}
	}
	
	private static synchronized void routeIncomingMessage(String destination, String command) {
		if (loadedDrivers.containsKey(destination)) {
			Driver driver = loadedDrivers.get(destination);
			if (driver.getState() == Thread.State.TERMINATED) {
				Log.d(TAG, "waking terminated driver '" + destination + "' for incoming message");
				driver.queueCommand(command);
				(new Thread(driver)).start();
			} else {
				driver.receiveCommand(command);
			}
		} else if (destination.equals("DEBUG")) {
			Log.d(TAG, "requested debug message from remote module: '" + command + "'");
		} else {
			Log.w(TAG, "incoming message addressed to '" + destination + "' could not be delivered. No such driver exists");
		}
	}
	
	private static synchronized void routeIncomingMessage(String destination, byte[] data) {
		if (loadedDrivers.containsKey(destination)) {
			Driver driver = loadedDrivers.get(destination);
			if (driver.getState() == Thread.State.TERMINATED) {
				Log.d(TAG, "waking terminated driver '" + destination + "' for incoming message");
				driver.queueBinary(data);
				(new Thread(driver)).start();
			} else {
				driver.receiveBinary(data);
			}
		} else if (destination.equals("DEBUG")) {
			Log.d(TAG, "requested debug message from remote module: '" + data.toString().substring(0, 6) + "...'");
		} else {
			Log.w(TAG, "incoming message addressed to '" + destination + "' could not be delivered. No such driver exists");
		}
	}

	@Deprecated
	private static synchronized void processMessage(byte[] byteArray) {
		//TODO check the protocol version based off the first byte
		//Since we only support protocol version 0 right now we only need to
		//+ convert this to a string using ASCII encoding
		
		String inMessage = "";
		byte startByte = byteArray[0];
		try {
			inMessage = new String(Arrays.copyOfRange(byteArray, 1, byteArray.length), "US-ASCII");
//			Log.d(TAG, "processing incoming message: '" + inMessage + "'");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "unable to format incomming message as ASCII text, discarding");
		}
		
		//mask out the top 4 bits to get the protocol version
		Integer transProt = (startByte & 0x0F);
//		Log.d(TAG, "processing message with protocol " + transProt);
		
		
		String destination;
		String command;
		
		//the local arduino will send "READY\n" after it is finished with
		//+ its setup.  Since this can sometimes become garbled (i.e. "REAREADY\N")
		//+ we have to be a bit loose in how the message is matched.
		if (inMessage.indexOf(":") == -1 && inMessage.trim().endsWith("READY")) {
			//TODO
//			Log.d(TAG, "local arduino link is ready");
			connectionReady = true;
		} else if (inMessage.indexOf(":") != -1) {
			destination = inMessage.substring(0, inMessage.indexOf(":"));
			command = inMessage.substring(inMessage.indexOf(":") + 1, inMessage.length());
//			Log.d(TAG, "read incoming message to: '" + destination + "' contents: '" + command + "'");
			
			//add this remote module to the known list if it isn't already there
			if (destination != null) {
				if (remoteModules.containsKey(destination)) {
//					Log.w(TAG, "remote module " + destination + " is already in the remote modules list");
				} else {
//					Log.d(TAG, "adding module " + destination + " to the list of known modules");
					remoteModules.put(destination, transProt);
				}
			}
			
			
			if (destination.equals("DEBUG")) {
				Log.d(TAG, "requested debug from remote module '" + command + "'");
			} else {
				//route the message to the appropriate driver
				if (loadedDrivers.containsKey(destination)) {
					Driver destDriver = loadedDrivers.get(destination);
					if (destDriver.getState() != Thread.State.TERMINATED) {
//						Log.d(TAG, "passing message to driver");
						destDriver.receiveCommand(command);
					} else {
						//TODO re-launch the driver passing in the command
						destDriver.queueCommand(command);
						Log.w(TAG, "could not route incomming message to driver because it is terminated");
					}
				} else {
//					Log.d(TAG, "incoming message could not be routed to a running driver");
				}
			}
		} else {
			//the incoming message does not match any known format
			Log.w(TAG, "incomming message '" + inMessage + "' does not match any known formats");
		}
	}
	
	/**
	 * Sends a query string to all remote modules "ALL:READY?"
	 */
	private static void queryRemoteModules() {
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
		boolean messageSent = false;
		//default to sending this message using the most recent protocol version
		int preferredProtocol = (int)protocolVersion;
		
		//If the module is known, then we can format the message to use whatever
		//+ protocol version it uses
		if (remoteModules.containsKey(moduleName)) {
			preferredProtocol = remoteModules.get(moduleName);
		}
		
		if (connectionReady) {
			switch (preferredProtocol) {
				case 1:
					messageSent = sendCommand_V1(moduleName, command);
					break;
				default:
					Log.e(TAG, "unknown protocol version: " + (int)protocolVersion + ", discarding message");
					messageSent = false;
					break;
			}
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
			messageSent = false;
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
		boolean messageSent = false;
		//default to sending this message using the most recent protocol version
		int preferredProtocol = (int)protocolVersion;
		
		//If the module is known, then we can format the message to use whatever
		//+ protocol version it uses
		if (remoteModules.containsKey(moduleName)) {
			preferredProtocol = remoteModules.get(moduleName);
		}
		
		if (connectionReady) {
			switch (preferredProtocol) {
				case 1:
					messageSent = sendBinary_V1(moduleName, data);
					break;
				default:
					Log.e(TAG, "unknown protocol version: " + (int)protocolVersion + ", discarding message");
					messageSent = false;
					break;
			}
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
	synchronized String requestWidgetXml(String driverName) {
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
	 * Returns a list of all loaded drivers.
	 * @return an ArrayList<String> of driver names.
	 */
	static synchronized ArrayList<String> getLoadedDrivers() {
		ArrayList<String> driverList = new ArrayList<String>();
		for (String driverName: loadedDrivers.keySet()) {
			driverList.add(driverName);
		}
		
		return driverList;
	}

	static synchronized void incomingSerial(byte b) {
		byteBuffer.push(b);
	}

	public static void main(String argv[]) throws InterruptedException {
		Log.c(TAG, "Starting");
		//Using apache commons cli to parse the command line options
		Options options = new Options();
		options.addOption("help", false, "Display this help message.");
		Option portOption = OptionBuilder.withArgName("port")
				.hasArg()
				.withDescription("Bind the server to the given port number")
				.create("port");
		options.addOption(portOption);
		
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
		
		serialConnection = new SerialConnection(serialPortName);
		if (!serialConnection.isConnected()) {
			Log.e(TAG, "could not connect to serial port, exiting");
			System.err.println("could not connect to serial port, exiting");
			System.exit(1);
		}
		
		//block until the local arduino is ready
		System.out.print(TAG + ":" +  "Waiting for local link to be ready.");
		for (int i = 0; i < 6; i++) {
			if (!connectionReady) {
				System.out.print(".");
				Thread.sleep(1000);
			} else {
				System.out.println();
				break;
			}
		}
		if (!connectionReady) {
			Log.e(TAG, "could not find a local arduino connection, exiting");
			System.exit(1);
		} else {
			Log.c(TAG, "Local link ready.");
		}
		
		//query for remote modules.  Since the modules may be slow in responding
		//+ we will wait for a few seconds to make sure we get a complete list
		System.out.print(TAG + ":" + "Querying modules.");
		
		for (int i = 0; i < 6; i++) {
			queryRemoteModules();
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
        Driver driver1 = new LedFlash();
        
        //test adding a driver with the same name
        Driver driver2 = new StatefullLed();
     
        //only drivers with valid names are added
        //TODO make this generic
        if (driver1.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver1.getModuleName())) {
		        loadedDrivers.put(driver1.getModuleName(), driver1);
		        Log.d(TAG, "driver " + driver1.getModuleName() + " of type " + driver1.getModuleType() + " initialized");
        	} else {
        		Log.w(TAG, "error loading driver " + driver1.getClass().getName() + "a driver with the name " +
        				driver1.getModuleName() + " already exists");
        	}
        }
        
        if (driver2.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver2.getModuleName())) {
		        loadedDrivers.put(driver2.getModuleName(), driver2);
		        Log.d(TAG, "driver " + driver2.getModuleName() + " of type " + driver2.getModuleType() + " initialized");
        	} else {
        		Log.e(TAG, "error loading driver " + driver2.getClass().getName() + " a driver with the name " +
        				driver2.getModuleName() + " already exists");
        	}
        }
        
        
        //start the drivers
        for (String driverName: loadedDrivers.keySet()) {
        	Log.c(TAG, "Starting driver " + driverName);
        	(new Thread(loadedDrivers.get(driverName))).start();
        }
        
        //start the web interface
        Log.c(TAG, "Starting web server on port " + portNum);
        new SimpleHttpServer(portNum).start();
        
		//enter main loop
        while (true) {
        	//TODO keep track of the last time a message was sent to/received from each remote module
        	//+ if the time period is too great, then re-query the remote modules, possibly removing
        	//+ them from the known list if no response is detected
        	
        	//Check for incoming messages, only process the first byte before breaking off
        	//+ to a more appropriate method
        	if (byteBuffer.size() > 0) {
        		//read the start byte
        		byte startByte = byteBuffer.poll();
        		int transType = startByte & (1 << 7);
        		int transProtocol = (startByte & 0x0F);
        		
        		switch (transProtocol) {
        			case 1:
        				switch (transType) {
        					case 0:
        						Log.d(TAG, "incoming text message with protocol 1");
        						readTextMessage_V1(startByte);
        						break;
        					case 1:
        						Log.d(TAG, "incoming binary message with protocol 1");
        						readBinMessage_V1(startByte);
        						break;
        				}
        				break;
        			case 2:
        				Log.e(TAG, "incoming message with protocol version 2, not supported");
        				break;
        			default:
        				Log.e(TAG, "incoming message with unknown protocol version: '" + transProtocol + "' not supported");
        				break;
        		}
        		
        	}
        	
        	
	    	Thread.yield();
	    	Thread.sleep(100);
        }
	}
}
