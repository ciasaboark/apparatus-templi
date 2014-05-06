/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
