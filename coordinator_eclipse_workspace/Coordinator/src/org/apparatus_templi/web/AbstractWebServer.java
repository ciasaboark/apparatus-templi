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

import java.net.InetSocketAddress;

/**
 * An abstract representation of our web server.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public abstract class AbstractWebServer {
	/**
	 * Starts the HttpServer or HttpsServer than underlies the web server
	 */
	public abstract void start();

	/**
	 * Stops the HttpServer or HttpsServer that underlies the web server.
	 */
	public abstract void terminate();

	/**
	 * Change the location of the web resources folder that this web server will use. This change
	 * will be reflected immediately.
	 * 
	 * @param path
	 *            a String representation of the path to the web resources folder.
	 * @throws IllegalArgumentException
	 *             if the path is not a director, does not exists, or can not be opened.
	 */
	public abstract void setResourceFolder(String path) throws IllegalArgumentException;

	/**
	 * Returns the port number that the HttpServer or HttpsServer is listening on. If the underlying
	 * server has been stopped then the returned port number may no longer be in use.
	 */
	public abstract int getPort();

	/**
	 * Returns the IP address or hostname that the server can be reached at. If the underlying
	 * server has been stopped then the returned location may no longer be in use.
	 */
	public abstract String getServerLocation();

	/**
	 * Returns the protocol that must be used to access the running web server. This will be one of
	 * "http://" or "https://"
	 */
	public abstract String getProtocol();

	/**
	 * Returns the InetSocketAddress that the web server is attached to. Since some operating
	 * systems *cough* Windows *cough* do not like processes re-binding to the same port soon after
	 * a socket is closed this socket should be reused unless the host or port number will be
	 * changed.
	 */
	public abstract InetSocketAddress getSocket();

	/**
	 * Returns the String representation of the path of the web resources folder being used by this
	 * web server. This value may be null if no resource folder has been set.
	 */
	public abstract String getResourceFolder();

}
