package org.apparatus_templi;
import gnu.io.SerialPort;

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
	
	
	private static String incommingBuffer;
	private static ArrayList<String> remoteModules = new ArrayList<String>();
//	private static ArrayList<Driver> runningDrivers;
	private static int portNum;
	private static final int defaultPort = 2024;
	private static String serialPortName = "";
	private static final String TAG = "Coordinator";
	private static SerialConnection serialConnection = null;
	private static boolean ioReady = false;
	
	private static HashMap<String, Driver> runningDrivers = new HashMap<String, Driver>();
	
	
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
	public static synchronized String sendCommandAndWait(String name, String command, int waitPeriod) {
		//TODO: this could easily be abused by the drivers to bring down the system.  It might need
		//+ to be removed, or to limit the number of times any driver can call this method in a given
		//+ time period.
		return null;
	}
	
	public static synchronized void setIoReady(boolean state) {
		ioReady = state;
	}
	
	/**
	 * Sends the given command to a specific remote module
	 * @param name the unique name of the remote module
	 * @param command the command to send to the remote module
	 */
	public static synchronized void sendCommand(String name, String command) {
		ioReady = true;
		serialConnection.writeData(name + ":" + command + "\n");
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
	public static synchronized int storeData(String driverName, String dataTag, String data) {
		return 0;
	}
	
	/**
	 * Returns data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	public static synchronized String readData(String driverName, String dataTag) {
		return null;
	}
	
	
	/**
	 * Checks the list of known remote modules. If the module is not present the Coordinator
	 * 	may re-query the remote modules for updates.
	 * @param moduleName the name of the remote module to check for
	 * @return true if the remote module is known to be up, false otherwise
	 */
	public static synchronized boolean isModulePresent(String moduleName) {
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
		super();
		incommingBuffer = "";
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
					portNum = defaultPort;
				}
			} else {
				Log.d(TAG, "default port " + defaultPort + " selected");
				portNum = defaultPort;
			}
			
			//try to guess a good serial port if we weren't given one
			if (cmd.hasOption("serial")) {
					serialPortName = cmd.getOptionValue("serialName");
			} else {
				String osName = System.getProperty("os.name","").toLowerCase();
				if ( osName.startsWith("windows") ) {
			        Log.d(TAG, "detected windows OS, default serial port COM1"); 
					serialPortName = "COM1";
			      } else if (osName.startsWith("linux")) {
			    	  Log.d(TAG, "detected linux OS, default serial port /dev/ttyUSB0");
			    	  serialPortName = "/dev/ttyUSB0";
			      } else if ( osName.startsWith("mac") ) {
			    	  Log.d(TAG, "detected mac OS, default serial port /dev/tty.usbmodemfa121");
			    	  serialPortName = "/dev/tty.usbmodemfa121";
			      } else {
			    	  Log.w(TAG, "Could not guess a good default serial port, trying COM1");
			    	  serialPortName = "COM1";
			      }
			}
		}  catch (ParseException e) {
			System.out.println("Error processing options: " + e.getMessage());
			new HelpFormatter().printHelp("Diff", options);
			Log.e(TAG, "Error parsing options");
			System.exit(1);
		}
		
		//setup the serial connection
		serialConnection = new SerialConnection();
		if (serialConnection.connect("/dev/tty.usbmodemfa131")) {
            if (serialConnection.initIOStream() == true) {
                serialConnection.initListener();
            }
        }
        
        
        //start the drivers
        LedFlash driver1 = new LedFlash();
        (new Thread(driver1)).start();
        runningDrivers.put(driver1.getModuleName(), (Driver)driver1);
        //right now just add the driver name to the remote module list
        remoteModules.add(driver1.getModuleName());
        
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
