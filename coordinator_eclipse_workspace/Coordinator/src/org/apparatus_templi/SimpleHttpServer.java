package org.apparatus_templi;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;

import com.sun.net.httpserver.*;

public class SimpleHttpServer implements Runnable {
	private static HttpServer server;
	
	public SimpleHttpServer(int portNumber) {
		try {
			server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), portNumber), 0);
		} catch (IOException e) {
			Log.e("error", "Failed to initialize the server");
		}
	}

	@Override
	public void run() {
		server.start();
	}
}