package org.apparatus_templi.web;

import java.net.InetSocketAddress;

public abstract class AbstractWebServer {
	public abstract void start();

	public abstract void terminate();

	public abstract void setResourceFolder(String path) throws IllegalArgumentException;

	public abstract int getPort();

	public abstract String getServerLocation();

	public abstract String getProtocol();

	public abstract InetSocketAddress getSocket();

	public abstract String getResourceFolder();

}
