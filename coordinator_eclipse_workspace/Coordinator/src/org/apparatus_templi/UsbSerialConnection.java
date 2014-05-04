package org.apparatus_templi;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * A serial connection to manage connection and data transfer to and from controller Arduino through
 * a USB connection. Uses the RXTX library to provide hardware abstraction on various host operating
 * systems.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class UsbSerialConnection extends SerialConnection {
	private static final String TAG = "SerialConnection";

	SerialPort serialPort;
	// private static LinkedHashSet<String> portNames;
	private InputStream input;
	private OutputStream output;

	private static final int TIME_OUT = 2000;

	private static final int DATA_RATE = 9600;

	private boolean connected = false;

	// /**
	// * Initialize the serial connection by attempting to automatically find a valid port.
	// */
	// public UsbSerialConnection() {
	// this(null);
	// }

	/**
	 * Initialize the serial connection with the specified port. The given serial port name must not
	 * be null or an empty string.
	 * 
	 * @param serialPortName
	 *            the name of the serial port or null to auto-detect.
	 * @throws IllegalArgumentException
	 *             if serialPortName is null or an empty string.
	 */
	public UsbSerialConnection(String serialPortName) {
		if (serialPortName == null || serialPortName.equals("")) {
			throw new IllegalArgumentException("Serial port name can not be null or empty");
		}

		this.initialize(serialPortName);
	}

	/**
	 * Initializes the serial connection to the given serial port. If a connection could not be
	 * established then will shutdown the system with a terminal failure. If a connection can be
	 * established then adds event listener for
	 * {@link UsbSerialConnection#serialEvent(SerialPortEvent)}.
	 */
	@Override
	protected void initialize(String serialPortName) {
		if (serialPortName == null || serialPortName.isEmpty()) {
			throw new IllegalArgumentException("Serial port name can not be null or empty string");
		}
		connected = false;

		CommPortIdentifier portId = null;
		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		// TODO clean this up. if a preferred port is given only try to connect to that port, else
		// scan the defaults list
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(serialPortName)) {
				Log.d(TAG, "found port '" + serialPortName + "' attempting to connect");
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			Log.t(TAG, "Could not connect to port '" + serialPortName + "'");
			Coordinator.exitWithReason("Could not connect to port '" + serialPortName + "'");
		}

		try {
			// open the serial port
			serialPort = (SerialPort) portId.open(TAG, TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			connected = true;
		} catch (Exception e) {
			System.err.println("unable to connect to port '" + serialPortName + "': "
					+ e.getMessage());
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
	 * Reads a single byte of data from the serial connection. If the byte read was valid (not -1)
	 * then the byte is sent to the {@link MessageCenter} for processing.
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int readInt = input.read();
				if (readInt != -1) {
					MessageCenter.getInstance().incomingSerial((byte) readInt);
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
			Log.d(TAG, "writing byte[] data to output (string value): " + new String(data));
			Log.d(TAG, "writing byte[] data to output (integer value): " + data);
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
