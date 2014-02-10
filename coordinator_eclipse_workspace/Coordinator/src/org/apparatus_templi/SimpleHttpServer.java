package org.apparatus_templi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.Socket;

public class SimpleHttpServer extends HttpServer implements Runnable {
	
	private Socket sock;
	private InetSocketAddress addr;
	
	/*
	 * Removed portNum parameter form constructor because you have to call
	 * bind before starting the server
	 */
	public SimpleHttpServer() {
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void bind(InetSocketAddress address, int port) throws IOException {
		try {
			addr = address;
			if(addr != null) {
				sock = new Socket(addr.getAddress(), port);
			} else {
				Log.e("error", "InetSocketAddress was not initialized");
			}
		}
		catch(IOException e) {
			Log.e("error","The socket failed to initialize");
		}

	}

	@Override
	public HttpContext createContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpContext createContext(String arg0, HttpHandler arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getAddress() {
		return addr;
	}

	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeContext(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeContext(HttpContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExecutor(Executor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(int arg0) {
		// TODO Auto-generated method stub

	}

}
