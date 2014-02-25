package org.apparatus_templi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer implements Runnable {
	private static HttpServer server = null;
	private static final String TAG = "SimpleHttpServer";
	
	/*
	 * As of right now the server will fail if it is has more than one instance trying to run on the
	 * same port. It needs to be changed to a singleton instance or keep tack of port numbers that
	 * are already in use.
	 */
	public SimpleHttpServer(int portNumber) {
		try {
			server = HttpServer.create(new InetSocketAddress(portNumber), 0);
			server.createContext("/get_running_drivers", new RunningDriversHandler());
			server.createContext("/get_full_xml?driver", new FullXmlHandler());
			server.createContext("/get_driver_widget", new WidgetXmlHandler());
			server.setExecutor(null);
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialize the server");
			e.printStackTrace();
		}
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
	    
	    public byte[] getResponse() {
	    	String xml = xmlVersion + header;
	    	ArrayList<String> runningDrivers = Coordinator.getLoadedDrivers();
	    	for (String s: runningDrivers) {
	    		xml += "<Module name='" + s + "' />";
	    	}
	    	xml += footer;
	    	return xml.getBytes();
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
