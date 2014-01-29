package org.apparatus_templi;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

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
	
	
//	private static  incommingBuffer;
	private static ArrayList<String> remoteModules = new ArrayList<String>();
	private static HashMap<String, Driver> loadedDrivers = new HashMap<String, Driver>();
	private static int portNum;
	private static final int DEFAULT_PORT = 2024;
	private static String serialPortName = null;
	private static final String TAG = "Coordinator";
	private static boolean ioReady = false;
	
	//bitmask flags for the transmission start byte
	private static final byte TEXT_TRANSMISSION = (byte)0b0000_0000;
	private static final byte BIN_TRANSMISSION  = (byte)0b1000_0000;
	//the safety bit is reserved and always 1.  This is to make sure that the
	//+ header byte is never equal to 0x0A (the termination byte)
	private static final byte SAFETY_BIT   = (byte)0b0010_0000;
	private static final byte PROTOCOL_V0  = (byte)0b0000_0000;
	private static final byte PROTOCOL_V1  = (byte)0b0000_0001;
	private static final byte PROTOCOL_V2  = (byte)0b0000_0010;
	
	private static final byte protocolVersion = PROTOCOL_V0;
	
	//Separates the destination from the command
	private static String headerSeperator = ":";
	
	//a single line-feed char marks the end of the transmission.  If the command
	//+ contains any matching bytes they must be doubled to avoid early term.
	private static byte termByte = (byte)0x0A;
	
	private static SerialConnection serialConnection;
	
	
	
	
	//TODO change this to a singleton
	public Coordinator() {
		super();
	}

	/**
	 * broadcasts a command to all points in the Zigbee network
	 * 	This addresses to header to the reserved destination "ALL".
	 * 	Any remote modules that go into a sleep state might not
	 * 	receive the message.
	 * @param command the command to broadcast.
	 * TODO add list of available broadcast commands
	 */
	private void broadCastCommand(String command) {
		
	}

	/**
	 * Reads a single byte of data from the incoming serial connection
	 * @return the Byte value of the read byte, null if there was nothing
	 * 	to read or if the read failed.
	 */
	private Byte readSerial() {
		Byte b = null;
		if (serialDataAvailable()) {
			try {
				b = new Byte((byte)serialConnection.readInputByte());
			} catch (IOException e) {
				Log.e(TAG, "readSerial() error reading input byte");
			}
		}
		return b;
	}
	
	/**
	 * Checks the serial connection to see if there is any data available for reading
	 * @return true if data is available for reading, false otherwise
	 */
	private boolean serialDataAvailable() {
		//TODO
		return false;
	}
	
	/**
	 * Send a command using protocol version 0
	 * @param moduleName
	 * @param command
	 */
	private static synchronized void sendCommandV0(String moduleName, String command) {
		Log.d(TAG, "sending message as protocol 0");
		boolean sendMessage = true;
		byte[] bytes = {0b0};
		
		String message = moduleName + ":" + command + "\n";
		try {
			bytes = message.getBytes("US-ASCII");
			Log.d(TAG, "message in hex: '" + DatatypeConverter.printHexBinary(bytes) + "'");
			Log.d(TAG, "message in ascii (trimmed): '" + new String(bytes).trim() + "'");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "sendCommandV0() error converting message to ASCII, discarding");
			sendMessage = false;
		}
		
		if (sendMessage) {
			serialConnection.writeData(bytes);
		}
	}
	
	/**
	 * Send a command using protocol version 1
	 * @param moduleName
	 * @param command
	 */
	private static synchronized void sendCommandV1(String moduleName, String command) {
		Log.d(TAG, "sending message as protocol 1");
		//TODO make sure size of command bytes is not larger than a single zigbee packet,
		//+ break into chunks if needed
		
		//TODO check command for line-feed chars, replace with double newlines (or refuse
		//+ to route the command).
		
		if (!serialConnection.isConnected()) {
			return;
		}
		
		ioReady = true;
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
			
			Log.d(TAG, "sending bytes 0x" + DatatypeConverter.printHexBinary(bBuffer.array()));
			Log.d(TAG, "sending ascii string '" + new String(bBuffer.array()) + "'");
			serialConnection.writeData(bBuffer.array());
		} else {
			Log.w(TAG, "error converting message to ascii byte[]. Message not sent");
		}
	}
	
	/**
	 * Sends the given command to a specific remote module
	 * @param moduleName the unique name of the remote module
	 * @param command the command to send to the remote module
	 */
	static synchronized void sendCommand(String moduleName, String command) {
		switch (protocolVersion) {
			case 0:
				sendCommandV0(moduleName, command);
				break;
			case 1:
				sendCommandV1(moduleName, command);
				break;
			default:
				Log.e(TAG, "unknown protocol version: " + (int)protocolVersion + ", discarding message");
		}
	}

	/*
	 * Public methods.  The drivers should make use of these 
	 */
	
	/**
	 * Sends a message to a remote module and waits waitPeriod seconds for a response.
	 * @param name the unique name of the remote module
	 * @param command the command to send to the remote module 
	 * @param waitPeriod how many seconds to wait for a response.  Maximum period to wait
	 * 	is 6 seconds.
	 * @return the response string of the remote module or null if no response was found
	 */
	static synchronized String sendCommandAndWait(String name, String command, int waitPeriod) {
		//TODO: since this is a blocking method this could easily be abused by the drivers to bring down
		//+ the system.  It might need to be removed, or to limit the number of times any driver can call
		//+ this method in a given time period.
		return null;
	}
	
	/**
	 * Sends binary data over the serial connection to a remote module.
	 * 	Does not yet break byte[] into chunks for transmission.  Make sure
	 * 	that the size of the transmission is not larger than a single packet.
	 * @param moduleName the unique name of the remote module
	 * @param data the binary data to send
	 */
	static synchronized void sendBinary(String moduleName, byte[] data) {
		//TODO find out the max size of a single Zigbee packet and break the data into
		//+ chunks for multiple transmissions.
		
		//TODO look for bytes matching 0x0A in the data and replace with 0x0A0A
		
		if (!serialConnection.isConnected()) {
			return;
		}
		
		ioReady = true;
		//the arduino expects chars as 1 byte instead of two, convert command
		//+ to ascii byte array, then tack on the header, name, and footer
		byte startByte = (byte)(BIN_TRANSMISSION | SAFETY_BIT | protocolVersion);
		byte[] destinationBytes = {0b0};
		byte[] headerSeperatorByte = {0b0};
		
		boolean sendMessage = true;
		
		//convert the destination address to ascii byte array
		try {
			destinationBytes = moduleName.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "error converting remote module name '" + moduleName + "' to US-ASCII encoding.");
			sendMessage = false;
		}
		
		//convert the destination separator to ascii byte array
		try {
			headerSeperatorByte = headerSeperator.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "error converting header seperator to ascii byte array.");
			sendMessage = false;
		}
				
		
		if (sendMessage) {
			//reserve enough room for the start/term bytes, the header, and the command
			ByteBuffer bBuffer = ByteBuffer.allocate(1 + destinationBytes.length + headerSeperatorByte.length + data.length + 1);
			bBuffer.put(startByte);
			bBuffer.put(destinationBytes);
			bBuffer.put(headerSeperatorByte);
			bBuffer.put(data);
			bBuffer.put(termByte);
			
			Log.d(TAG, "sending bytes 0x" + DatatypeConverter.printHexBinary(bBuffer.array()));
			Log.d(TAG, "sending ascii string '" + new String(bBuffer.array()) + "'");
			serialConnection.writeData(bBuffer.array());
		} else {
			Log.w(TAG, "error converting message to ascii byte[]. Message not sent");
		}
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
	synchronized boolean passCommand(String toDriver, String fromDriver, String command) {
		//TODO verify source name
		//TODO check for reserved name in toDriver
		boolean messagePassed = false;
		if (loadedDrivers.containsKey(toDriver)) {
			loadedDrivers.get(toDriver).receiveCommand(command);
			messagePassed = true;
		}
		return messagePassed;
	}

	/**
	 * Checks the list of known remote modules. If the module is not present the Coordinator
	 * 	may re-query the remote modules for updates.
	 * @param moduleName the name of the remote module to check for
	 * @return true if the remote module is known to be up, false otherwise
	 */
	static synchronized boolean isModulePresent(String moduleName) {
		boolean result = false;
		for (String name: remoteModules) {
			if (name.equals(moduleName)) {
				result = true;
				break;
			}
		}
		return result;
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
	
	
	static synchronized void setIoReady(boolean state) {
		ioReady = state;
	}

	public static void main(String argv[]) {
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
        
        
        //initialize the drivers
        Driver driver1 = new LedFlash();
        
        //test adding a driver with the same name
        Driver driver2 = new LedFlash();
     
        //only drivers with valid names are added
        if (driver1.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver1.getModuleName())) {
		        remoteModules.add(driver1.getModuleName());
		        Log.d(TAG, "driver " + driver1.getModuleName() + " of type " + driver1.getModuleType() + " initialized");
		        
		        //right now just add the driver name to the remote module list
		        loadedDrivers.put(driver1.getModuleName(), (Driver)driver1);
        	} else {
        		Log.w(TAG, "error loading driver " + driver1.getClass().getName() + "a driver with the name " +
        				driver1.getModuleName() + " already exists");
        	}
        }
        
        if (driver2.getModuleName() != null) {
        	if (!loadedDrivers.containsKey(driver2.getModuleName())) {
		        remoteModules.add(driver2.getModuleName());
		        Log.d(TAG, "driver " + driver2.getModuleName() + " of type " + driver2.getModuleType() + " initialized");
		        
		        //right now just add the driver name to the remote module list
		        loadedDrivers.put(driver2.getModuleName(), (Driver)driver2);
        	} else {
        		Log.e(TAG, "error loading driver " + driver2.getClass().getName() + " a driver with the name " +
        				driver2.getModuleName() + " already exists");
        	}
        }
        
        
        //start the drivers
        for (String driverName: loadedDrivers.keySet()) {
        	(new Thread(loadedDrivers.get(driverName))).start();
        }
        
        //start the web interface
        new SimpleHttpServer(portNum).start();
        
		//enter main loop
        while (true) {
        	//wait for input or output to be ready
        	while (!ioReady) {

        	}
        	
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	ioReady = false;
        	Thread.yield();
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		
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
}
