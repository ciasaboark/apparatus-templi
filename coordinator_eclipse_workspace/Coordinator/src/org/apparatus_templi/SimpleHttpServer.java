package org.apparatus_templi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer implements Runnable {
	private static HttpServer server = null;
	private static final String TAG = "SimpleHttpServer";
	private static String resourceFolder = "./website/";
	
	/*
	 * As of right now the server will fail if it is has more than one instance trying to run on the
	 * same port. It needs to be changed to a singleton instance or keep tack of port numbers that
	 * are already in use.
	 */
	public SimpleHttpServer(int portNumber) {
		this (portNumber, false);
	}
	
	public SimpleHttpServer(int portNumber, boolean autoIncrement) {
		try {
			//create a InetSocket on the port
			InetSocketAddress socket;
			if (autoIncrement) {
				while (!portAvailable(portNumber)) {
					portNumber++;
				}
			} else if (!portAvailable(portNumber)) {
				Coordinator.exitWithReason("could not start web server on port " + portNumber);
			}
			
			socket = new InetSocketAddress(InetAddress.getLocalHost(), portNumber);
			try {
				server = HttpServer.create(socket, 0);
			} catch (SocketException e) {
				Coordinator.exitWithReason("could not bind to port " + portNumber + ": " + e.getMessage());
			}
			server.createContext("/index.html", new IndexHandler());
			server.createContext("/get_running_drivers", new RunningDriversHandler());
			server.createContext("/get_full_xml", new FullXmlHandler());
			server.createContext("/get_driver_widget", new WidgetXmlHandler());
			server.createContext("/resource", new ResourceHandler());
			server.createContext("/js/default.js", new JsHandler());
			//server.createContext("/", new IndexHandler());
			server.setExecutor(null);
			Log.d(TAG, "waiting on port " + portNumber);
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialize the server");
			e.printStackTrace();
		}
	}
	
	public void setResourceFolder(String path) {
		//TODO check for a valid path
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		Log.d(TAG, "using resources in '" + path + "'");
		resourceFolder = path;
	}
	
	private boolean portAvailable(int port) {
		
		boolean results = false;
	    try {
	    	Socket ignored = new Socket("localhost", port);
	    	Log.d(TAG, "port number " + port + " not available");
	    } catch (IOException ignored) {
	        results = true;
	        Log.d(TAG, "port number " + port + " available");
	    }
	    return results;
	}

	@Override
	public void run() {
		server.start();
	}
	
	public static HashMap<String, String> processQueryString(String query) {
		HashMap<String, String> queryTags = new HashMap<String, String>();
		//break the query string into key/value pairs by '&'
		for (String keyValue: query.split("&")) {
			//break the key/value pairs by '='
			String[] pair = keyValue.split("=");
			//only put in tags that have a value associated
			if (pair.length == 2) {
				queryTags.put(pair[0], pair[1]);
			}
		}
		
		return queryTags;
	}
	
	/**
	 * Handler to return the index.html
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	private class IndexHandler implements HttpHandler {
	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received index.html request from " + exchange.getRemoteAddress());
//			HashMap<String, String> queryTags = SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
//			Log.d(TAG, "value of 'foo': " + queryTags.get("foo"));
			byte[] response = getResponse();
	        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
	        exchange.getResponseBody().write(response);
	        exchange.close();
	    };
	    
	    private byte[] getResponse() throws IOException {
	    	InputStream is = new FileInputStream(resourceFolder + "index.html");
	    	byte[] fileBytes = {};
	    	int streamLength = is.available();
	    	if (streamLength <= Integer.MAX_VALUE) {
	    		fileBytes = new byte[(int)streamLength];
	    		int offset = 0;
	    		int numRead = 0;
	    		while (offset < fileBytes.length && (numRead = is.read(fileBytes, offset, fileBytes.length - offset)) >= 0) {
	    			offset += numRead;
	    		}
	    		
	    		is.close();
	    		
	    		if (offset < fileBytes.length) {
	    			throw new IOException("Could not read index.html");
	    		}
	    		
	    	}
	    	
	    	return fileBytes;
	    }
	}
	
	/**
	 * Handle requests for a list of running drivers.
	 * Generates XML in the form of:
	 * 		<?xml version='1.0'?>
	 * 		<ModuleList>
	 * 			<Module name='this driver name' />
	 * 			<Module name='another driver name' />
	 * 		</ModuleList>
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * @author Christopher Hagler <haglerchristopher@gmail.com>
	 *
	 */
	private class RunningDriversHandler implements HttpHandler {
	    private String xmlVersion = "<?xml version='1.0'?>";
	    private String header = "<ModuleList>";
	    private String footer = "</ModuleList>";
	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress());
//			HashMap<String, String> queryTags = SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
//			Log.d(TAG, "value of 'foo': " + queryTags.get("foo"));
			byte[] response = getResponse();
	        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
	        exchange.getResponseBody().write(response);
	        exchange.close();
	    };
	    
	    private byte[] getResponse() {
	    	String xml = xmlVersion + header;
	    	ArrayList<String> runningDrivers = Coordinator.getLoadedDrivers();
	    	for (String s: runningDrivers) {
	    		xml += "<Module name='" + s + "' />";
	    	}
	    	xml += footer;
	    	return xml.getBytes();
	    }
	}
	
	
	private class JsHandler implements HttpHandler {
	    private String xmlVersion = "<?xml version='1.0'?>";
	    private String header = "<ModuleList>";
	    private String footer = "</ModuleList>";
	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress());
			byte[] response = getResponse();
	        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
	        exchange.getResponseBody().write(response);
	        exchange.close();
	    };
	    
	    private byte[] getResponse() {
	    	String jsCode = "$portnum = 8000;";
	    	return jsCode.getBytes();
	    }
	}
	
	/**
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	private class ResourceHandler implements HttpHandler {
	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received resource request from " + exchange.getRemoteAddress());
			String query = exchange.getRequestURI().getQuery();
			if (query != null) {
				HashMap<String, String> queryTags = SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
				if (queryTags.containsKey("file")) {
					Log.d(TAG, "value of 'file': '" + queryTags.get("file") + "'");
					String resourceName = queryTags.get("file");
					resourceName = resourceName.replaceAll("\\.\\./", "");
					try {
						byte[] response = getResponse(resourceName);
				        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				        exchange.getResponseBody().write(response);
					} catch (IOException e) {
						Log.e(TAG, "error opening resource '" + resourceFolder + resourceName + "' for reading");
					}
				}
			}
			exchange.close();
	    };
	    
	    private byte[] getResponse(String resourceName) throws IOException {
	    	InputStream is = new FileInputStream(resourceFolder + resourceName);
	    	byte[] fileBytes = {};
	    	int streamLength = is.available();
	    	if (streamLength <= Integer.MAX_VALUE) {
	    		fileBytes = new byte[(int)streamLength];
	    		int offset = 0;
	    		int numRead = 0;
	    		while (offset < fileBytes.length && (numRead = is.read(fileBytes, offset, fileBytes.length - offset)) >= 0) {
	    			offset += numRead;
	    		}
	    		
	    		is.close();
	    		
	    		if (offset < fileBytes.length) {
	    			throw new IOException("Could not read " + resourceName);
	    		}
	    		
	    	}
	    	
	    	return fileBytes;
	    }
	}
	
	/**
	 * Handle request for a driver's full page XML
	 * @author 
	 *
	 */
	private class FullXmlHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			//TODO	
		}
		
		public byte[] getResponse() {
			//TODO
			return null;
		}
	}
	
	/**
	 * Handle request for a driver's widget XML
	 * @author 
	 *
	 */
	private class WidgetXmlHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			//TODO	
		}
		
		public byte[] getResponse() {
			//TODO
			return null;
		}
	}
}
