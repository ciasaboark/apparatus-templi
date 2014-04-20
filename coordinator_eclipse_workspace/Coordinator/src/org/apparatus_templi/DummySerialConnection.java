package org.apparatus_templi;

import gnu.io.SerialPortEvent;

/**
 * A dummy serial connection that does not rely on any hardware. All data given to this connection
 * will be placed into a bit-bucket. This connection will never generate any incoming data. The
 * connection within the dummy serial connection is always considered to be valid.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class DummySerialConnection extends SerialConnection {
	private static final String TAG = "DummySerialConnection";

	private boolean connected = false;

	/**
	 * 
	 */
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
		this.connected = false;
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
		if (data == null) {
			throw new IllegalArgumentException("Can not write null data to serial line");
		}
		return true;
	}

	@Override
	protected void initialize(String preferredConnection) {
		// TODO Auto-generated method stub

	}

}
