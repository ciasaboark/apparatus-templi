package org.apparatus_templi;
import java.io.IOException;

import gnu.io.SerialPortEventListener;

public abstract class SerialConnection implements SerialPortEventListener {
	protected abstract void initialize(String preferredConnection);
	public abstract boolean isConnected();
	public abstract void close();
	abstract boolean isDataAvailable();
	abstract int readInputByte() throws IOException;
	abstract boolean writeData(byte[] data);
}