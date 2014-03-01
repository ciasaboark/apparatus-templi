package org.apparatus_templi;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;


public class DummySerialConnection extends SerialConnection {
	private static final String TAG = "DummySerialConnection";
	
	SerialPort serialPort;

	private boolean connected = false;
	
	public DummySerialConnection() {
		this(null);
	}
	
	public DummySerialConnection(String preferredConnection) {
		Log.d(TAG, "initialized new dummy serial connection");
		this.connected = true;
	}
	
	@Override
	public boolean isConnected() {
		return connected;
	}


	@Override
	public synchronized void close() {
	}

	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
	}

	@Override
	synchronized boolean isDataAvailable() {
		return false;
	}
	
	@Override
	synchronized int readInputByte() {
		return -1;
	}
	
	
	@Override
	synchronized boolean writeData(byte[] data) {
		 return true;
	 }

	@Override
	protected void initialize(String preferredConnection) {
		// TODO Auto-generated method stub
		
	}

}
