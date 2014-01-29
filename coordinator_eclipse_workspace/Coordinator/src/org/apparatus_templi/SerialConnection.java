package org.apparatus_templi;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;

import javax.xml.bind.DatatypeConverter;


public class SerialConnection implements SerialPortEventListener {
	private static final String TAG = "SerialTest";
	
	SerialPort serialPort;
        /** The port we're normally going to use. */
	private static LinkedHashSet<String> portNames;
	
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader bReader;
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	private static String preferredConnection;
	
	private boolean connected = false;
	
	public SerialConnection() {
		this(null);
	}
//preferred
	public SerialConnection(String preferredConnection) {
		portNames = new LinkedHashSet<String>();
		if (preferredConnection != null) {
			portNames.add(preferredConnection);
		}
		portNames.add("/dev/tty.usbmodemfa131");	//MacOS
		portNames.add("/dev/tty.usbmodemfd121");	//MacOS
		portNames.add("/dev/ttyUSB0");				//Linux
		portNames.add("COM3");						//Windows
		
		this.initialize(preferredConnection);
	}
	
	private void initialize(String preferredConnection) {
		connected = false;
		
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : portNames) {
				if (currPortId.getName().equals(portName)) {
					if (!currPortId.getName().equals(preferredConnection) && preferredConnection != null) {
						Log.w(TAG,  "could not connect using preferred port " + preferredConnection + ", using " +
								currPortId.getName() + " instead");
					} else {
						Log.d(TAG, "connected to port " + currPortId.getName());
					}
					portId = currPortId;
					break;
				}
			}
		}
		
		if (portId == null) {
			System.out.println("Could not find COM port.");
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(TAG, TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			bReader = new BufferedReader(new InputStreamReader(input));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			connected = true;
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	public boolean isConnected() {
		return connected;
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
//				String inputLine = bReader.readLine();
				byte[] inputByte = new byte[1];
				input.read(inputByte);
				Log.d(TAG, "read hex value: " + DatatypeConverter.printHexBinary(inputByte));
				Log.d(TAG, "Read char value: " + new String(inputByte));
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public synchronized boolean isDataAvailable() {
		boolean available = false;
		try {
				if (input.available() > 0) {
					available = true;
				}
		} catch (IOException e) {
			Log.e(TAG, "isDataAvaiable() error reading input stream");
		}
		return available;
	}
	
	public synchronized int readInputByte() throws IOException {
		return input.read();
	}
	
	
	
	 public synchronized void writeData(byte[] data) {
	    	try {
	    		Log.d(TAG,  "writing byte[] data to output");
				output.write(data);
				output.flush();
			} catch (IOException e) {
				Log.e(TAG, "error writing byte[] data to output, trying to re-connect:");
				initialize(null);
			}
			
	    	
	    }
}