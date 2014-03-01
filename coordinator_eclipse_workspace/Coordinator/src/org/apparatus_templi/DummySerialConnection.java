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


public class DummySerialConnection extends SerialConnection {
	private static final String TAG = "DummySerialConnection";
	
	SerialPort serialPort;
	private static LinkedHashSet<String> portNames;
	private InputStream input;
	private OutputStream output;
	

	private static final int TIME_OUT = 2000;

	private static final int DATA_RATE = 9600;
	
	private boolean connected = false;
	
	public DummySerialConnection() {
		this(null);
	}
	
	public DummySerialConnection(String preferredConnection) {
		this.connected = true;
	}
	
	private void initialize(String preferredConnection) {
		this.connected = false;
	}
	
	boolean isConnected() {
		return connected;
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	synchronized void close() {
	}

	/**
	 * reads a single byte of data from the serial connection
	 * Since SerialConnection does not understand the protocol
	 * 	formats it passes the byte back to Coordinator for processing
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
	}

	synchronized boolean isDataAvailable() {
		return false;
	}
	
	synchronized int readInputByte() throws IOException {
		return -1;
	}
	
	
	
	 synchronized boolean writeData(byte[] data) {
		 return true;
	 }
}
