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

package org.apparatus_templi.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.apparatus_templi.Log;

/**
 * Provides a number of convenience methods for the web server and http handlers to use.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class HttpHelper {
	private static final String TAG = "HttpHelper";

	/**
	 * Attempts to load the contents of the given file into an array of bytes.
	 * 
	 * @param fileName
	 *            the name of the file to open. The file name may not be null.
	 * @return the byte array contents of the file, or null if there was an error reading the file.
	 */
	public static byte[] getFileBytes(String fileName) {
		assert fileName != null;

		byte[] returnBytes = null;
		try {
			InputStream is = new FileInputStream(fileName);
			// InputStream is = this.getClass().getResourceAsStream(fileName);
			int streamLength = is.available();
			if (streamLength <= Integer.MAX_VALUE) {
				byte[] fileBytes = new byte[streamLength];
				int offset = 0;
				int numRead = 0;
				while (offset < fileBytes.length
						&& (numRead = is.read(fileBytes, offset, fileBytes.length - offset)) >= 0) {
					offset += numRead;
				}
				is.close();
				returnBytes = fileBytes;
				if (offset < fileBytes.length) {
					throw new IOException();
				}
			}
		} catch (IOException e) {
			Log.w(TAG, "Error opening '" + fileName + "'");
		}

		return returnBytes;
	}

	/**
	 * Breaks the given query string into a series of key/value pairs.
	 * 
	 * @param query
	 *            the query string to process. This should be the contents of the request string
	 *            after '?'
	 * @return a HashMap representation of the key/value pairs.
	 */
	public static HashMap<String, String> processQueryString(String query) {
		HashMap<String, String> queryTags = new HashMap<String, String>();
		// break the query string into key/value pairs by '&'
		if (query != null) {
			for (String keyValue : query.split("&")) {
				// break the key/value pairs by '='
				String[] pair = keyValue.split("=");
				// put the key/value pairs into the map
				if (pair.length == 2) {
					queryTags.put(pair[0], pair[1]);
				} else if (pair.length == 1) {
					// put in a null value for a key with no value
					queryTags.put(pair[0], null);
				} else {
					// somehow we got a key, key, value or perhaps a key, value, value "pair"
					Log.w(TAG, "unable to process " + keyValue);
				}
			}
		}

		return queryTags;
	}

	/**
	 * Returns a string representation of the best IP address that can be found for this host. The
	 * IP address will either be null or the first IPV4 address on the first non-loopback interface.
	 * 
	 * @deprecated this method should still work, but should no longer be used. This will be
	 *             replaced with binding to a specified IP address or hostname in a future release
	 */
	@Deprecated
	public static String bestAddress() {
		String bestAddress = null;
		System.setProperty("java.net.preferIPv4Stack", "true");
		Enumeration<NetworkInterface> nInterfaces = null;
		try {
			nInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
		while (nInterfaces != null && nInterfaces.hasMoreElements()) {
			NetworkInterface ni = nInterfaces.nextElement();
			Enumeration<InetAddress> niAddresses = ni.getInetAddresses();
			while (niAddresses.hasMoreElements()) {
				addresses.add(niAddresses.nextElement());
			}
		}
		for (InetAddress curAddr : addresses) {
			if (curAddr.isLoopbackAddress() && bestAddress == null) {
				bestAddress = curAddr.getHostAddress();
			} else {
				// stop looking at the first non-loopback address
				if (!(curAddr instanceof Inet6Address)) {
					bestAddress = curAddr.getHostAddress();
					break;
				}

			}
		}
		return bestAddress;
	}

}
