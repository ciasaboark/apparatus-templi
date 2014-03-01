package org.apparatus_templi;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;


public class UsbSerialConnection extends SerialConnection {
	private static final String TAG = "SerialConnection";
	
	SerialPort serialPort;
	private static LinkedHashSet<String> portNames;
	private InputStream input;
	private OutputStream output;
	

	private static final int TIME_OUT = 2000;

	private static final int DATA_RATE = 9600;
	
	private boolean connected = false;
	
	public UsbSerialConnection() {
		this(null);
	}
	
	public UsbSerialConnection(String preferredConnection) {
		portNames = new LinkedHashSet<String>();
		if (preferredConnection != null) {
			portNames.add(preferredConnection);
		} else {
			portNames.add("/dev/tty.usbmodemfd121");	//MacOS
			portNames.add("/dev/tty.usbmodemfa131");	//MacOS
			portNames.add("/dev/ttyUSB0");				//Linux
			portNames.add("/dev/ttyUSB1");				//Linux
			portNames.add("/dev/ttyUSB2");				//Linux
			portNames.add("dev/ttyACM0");				//Linux
			portNames.add("dev/ttyACM1");				//Linux
			portNames.add("dev/ttyACM2");				//Linux
			portNames.add("COM4");						//Windows
			portNames.add("COM3");						//Windows
			portNames.add("COM2");						//Windows
			portNames.add("COM1");						//Windows
		}
		
		this.initialize(preferredConnection);
	}
	
	@Override
	protected void initialize(String preferredConnection) {
		connected = false;
		
		CommPortIdentifier portId = null;
		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		//TODO clean this up.  if a preferred port is given only try to connect to that port, else scan the defaults list
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
			if (preferredConnection != null) {
				Coordinator.exitWithReason("Could not connect to port '" + preferredConnection + "'");
			} else {
				Coordinator.exitWithReason("Could not find COM port.");
			}
		}

		try {
			//open the serial port
			serialPort = (SerialPort) portId.open(TAG, TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			connected = true;
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * reads a single byte of data from the serial connection
	 * Since SerialConnection does not understand the protocol
	 * 	formats it passes the byte back to Coordinator for processing
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int readInt = input.read();
				if (readInt != -1) {
					MessageCenter.getInstance().incomingSerial((byte)readInt);
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

	@Override
	synchronized boolean isDataAvailable() {
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
	
	@Override
	synchronized int readInputByte() throws IOException {
		return input.read();
	}
	
	
	@Override
	synchronized boolean writeData(byte[] data) {
		boolean dataWritten = false;
		try {
			//Log.d(TAG,  "writing byte[] data to output: " + new String(data));
			output.write(data);
			output.flush();
			dataWritten = true;
		} catch (IOException e) {
			Log.e(TAG, "error writing byte[] data to output, trying to re-connect:");
			initialize(null);
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dataWritten;
	 }
}
