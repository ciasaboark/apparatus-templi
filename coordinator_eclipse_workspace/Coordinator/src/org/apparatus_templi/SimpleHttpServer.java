package org.apparatus_templi;

import java.net.InetSocketAddress;
import java.io.IOException;

import com.sun.net.httpserver.*;

public class SimpleHttpServer implements Runnable {
	private static HttpServer server = null;
	private static final String Tag = "SimpleHttpServer";
	
	/*
	 * As of right now the server will fail if it is has more than one instance trying to run on the
	 * same port. It needs to be changed to a singleton instance or keep tack of port numbers that
	 * are already in use.
	 */
	public SimpleHttpServer(int portNumber) {
		try {
			server = HttpServer.create(new InetSocketAddress(portNumber), 0);
			server.createContext("/get_running_drivers", new DriverHandler());
			//server.createContext("/get_full_xml?driver", new DriverHandler());
			//server.createContext("/get_driver_widget", new DriverHandler());
			//server.createContext("/get_running_drivers", new DriverHandler());
			server.setExecutor(null);
		} catch (IOException e) {
			Log.e(Tag, "Failed to initialize the server");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		server.start();
	}
}
