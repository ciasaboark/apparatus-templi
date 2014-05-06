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

import gnu.io.SerialPortEventListener;

import java.io.IOException;

/**
 * An abstract class that defines all methods that a serial connection must implement.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public abstract class SerialConnection implements SerialPortEventListener {
	protected abstract void initialize(String preferredConnection);

	/**
	 * Returns the current state of the connection.
	 */
	public abstract boolean isConnected();

	/**
	 * Shutdown the connection.
	 */
	public abstract void close();

	/**
	 * Returns true if any data is available for reading, false otherwise.
	 * 
	 */
	abstract boolean isDataAvailable();

	/**
	 * Read a single byte of data from the serial connection. The byte data is returned as a signed
	 * integer value.
	 * 
	 * @return the signed integer representation of the read byte. If the byte was read correctly
	 *         then this value will be between 0-255. If the byte could not be read then will return
	 *         a value of -1.
	 * @throws IOException
	 *             if the serial connection could not be read from.
	 */
	abstract int readInputByte() throws IOException;

	/**
	 * Writes the given array of bytes to the serial line using whatever method is appropriate for
	 * the sub-classing connection.
	 * 
	 * @param data
	 *            the data to write to the serial line.
	 * @return true if the data was written to the serial line, false otherwise.
	 */
	abstract boolean writeData(byte[] data);
}