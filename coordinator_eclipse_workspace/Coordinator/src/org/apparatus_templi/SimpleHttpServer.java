package org.apparatus_templi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer implements Runnable {
	private static HttpServer server = null;
	private static final String TAG = "SimpleHttpServer";
	private static String resourceFolder = "./website/";
	
	/**
	 * Generates a 404 error page for the given resourceName
	 * @param resourceName
	 * @return
	 */
	private static String get404ErrorPage(String resourceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" +
					"<body>" + 
					"<h1>404</h1>");
		sb.append("<p>Error locating resource: " + resourceName + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
	private static String get400BadRequestPage(URI uri) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" +
					"<body>" + 
					"<h1>400</h1>");
		sb.append("Malformed request: " + uri + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
	private byte[] getFileBytes(String fileName) {
		byte[] returnBytes = null;
    	try {
    		InputStream is = new FileInputStream(fileName);
    		int streamLength = is.available();
	    	if (streamLength <= Integer.MAX_VALUE) {
	    		byte[] fileBytes = new byte[(int)streamLength];
	    		int offset = 0;
	    		int numRead = 0;
	    		while (offset < fileBytes.length && (numRead = is.read(fileBytes, offset, fileBytes.length - offset)) >= 0) {
	    			offset += numRead;
	    		}
	    		is.close();
	    		returnBytes = fileBytes;
	    		if (offset < fileBytes.length) {
	    			throw new IOException();
	    		}
	    	}
    	} catch (Exception e) {
    		Log.w(TAG, "Error opening '" + fileName + "'");
    	}
    	
    	return returnBytes;
	}
	
	/*
	 * As of right now the server will fail if it is has more than one instance trying to run on the
	 * same port. It needs to be changed to a singleton instance or keep tack of port numbers that
	 * are already in use.
	 */
	public SimpleHttpServer(int portNumber) {
		this (portNumber, false);
	}
	
	public SimpleHttpServer(int portNumber, boolean autoIncrement) {
		this(portNumber, autoIncrement, false);
	}
	
	public SimpleHttpServer(int portNumber, boolean autoIncrement, boolean bindLocalhost) {
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
			
			InetAddress address = null;
			if (!bindLocalhost) {
				address = InetAddress.getLoopbackAddress();
			}
			socket = new InetSocketAddress(address, portNumber);
			try {
				server = HttpServer.create(socket, 1);
			} catch (SocketException e) {
				Coordinator.exitWithReason("could not bind to port " + portNumber + ": " + e.getMessage());
			}
			server.createContext("/index.html", new IndexHandler());
			server.createContext("/about.html", new AboutHandler());
//			server.createContext("/", new IndexHandler());
			server.createContext("/get_running_drivers", new RunningDriversHandler());
			server.createContext("/full_xml", new FullXmlHandler());
			server.createContext("/driver_widget", new WidgetXmlHandler());
			server.createContext("/resource", new ResourceHandler());
			server.createContext("/js/default.js", new JsHandler());
			server.createContext("/settings.html", new SettingsHandler());
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
	    Socket ignored = null;
		try {
			ignored = new Socket("localhost", port);
	    	Log.d(TAG, "port number " + port + " not available");
	    } catch (IOException e) {
	        results = true;
	        Log.d(TAG, "port number " + port + " available");
	    } catch (IllegalArgumentException e) {
	    	//Thrown when the port number is out of range
	    	Coordinator.exitWithReason("port number '" + port + "' out of range");
	    } finally {
	    	if (ignored != null) {
	    		try {
					ignored.close();
				} catch (IOException e) {}
	    	}
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
		if (query != null) {
			for (String keyValue: query.split("&")) {
				//break the key/value pairs by '='
				String[] pair = keyValue.split("=");
				//only put in tags that have a value associated
				if (pair.length == 2) {
					queryTags.put(pair[0], pair[1]);
				}
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
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("index.html").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
	        exchange.close();
	    };
	    
	    private byte[] getResponse() {
	    	byte[] returnBytes = null;
	    	
	    	byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.inc");
	    	byte[] indexBytes = getFileBytes(resourceFolder + "inc/index.inc");
	    	
	    	if (templateBytes != null && indexBytes != null) {
		    	String templateHtml = new String(templateBytes);
		    	String indexHtml = new String(indexBytes);	    	
		    	templateHtml = templateHtml.replace("MAIN_CONTENT", indexHtml.toString());
	    		returnBytes = templateHtml.getBytes();
	    	}
	    	
	    	return returnBytes;
	    }
	}
	
	/**
	 * Handler to return the index.html
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	private class AboutHandler implements HttpHandler {	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("index.html").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
	        exchange.close();
	    };
	    
	    private byte[] getResponse() {
	    	byte[] returnBytes = null;
	    	
	    	byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.inc");
	    	byte[] indexBytes = getFileBytes(resourceFolder + "inc/about.inc");
	    	
	    	if (templateBytes != null && indexBytes != null) {
		    	String templateHtml = new String(templateBytes);
		    	String indexHtml = new String(indexBytes);	    	
		    	templateHtml = templateHtml.replace("MAIN_CONTENT", indexHtml.toString());
	    		returnBytes = templateHtml.getBytes();
	    	}
	    	
	    	return returnBytes;
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
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
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
	
	/**
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	private class JsHandler implements HttpHandler {	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			byte[] response = getResponse();
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "application/javascript");
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
	private class SettingsHandler implements HttpHandler {	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("index.html").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
	        exchange.close();
	    };
	    
	    private byte[] getResponse() {
	    	byte[] returnBytes = null;
	    	byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.inc");
	    	if (templateBytes != null) {
		    	String template = new String(templateBytes);
		    	
	    		StringBuilder html = new StringBuilder();
	    		HashMap<String, String> prefs = Preferences.getInstance().getPreferencesMap();
	    		String configFile = prefs.get(Preferences.values.configFile);
	    		File f = new File(configFile);
	    		configFile = f.getAbsolutePath();
	    		if (configFile.length() > 40) {
	    			
	    		}
	    		prefs.remove(Preferences.values.configFile);
	    		html.append("<p>The settings below represent what the server is currently using. If you want to" +
	    				"reset a setting back to its default value then clear the input field before submitting." +
	    				"Saving the settings will overwrite the entire contents of the configuration file, so it" +
	    				"might be a good idea to have a backup stored elsewhere.</p>");
	    		
	    		//TODO update to a form so that the settings can be sent back in a POST request
	    		html.append("<div id=\"prefs_form\"><form name='prefs' id='prefs' action=\"update_settings\" method=\"POST\" >\n");

	    		//Preferences for the main section
	    		html.append("<div id='prefs_section_main' class='prefs_section'><h2 class='prefs_section_title'>" + "<i  class=\"fa fa-edit\"></i>&nbsp;Main" + "</h2>");
	    		for (String key: new String[] {Preferences.values.serialPort, Preferences.values.driverList}) {
	    			String value = prefs.get(key);
	    			html.append("<div class=\"pref_input\"><span class=\"pref_key\">" + key + "</span><span class=\"pref_value\"><input type=\"text\" name=\"" + key + "\" value=\"" + value + "\" /></span></div><br />\n");
	    			prefs.remove(key);
		    	}
	    		html.append("</div><p class='clear'></p>");
	    		
	    		//Preferences for web server
	    		html.append("<div id='prefs_section_webserver'  class='prefs_section'><h2 class='prefs_section_title'>" + "<i class=\"fa fa-cloud\"></i>&nbsp;Web Server" + "</h2>");
	    		for (String key: new String[] {Preferences.values.portNum, Preferences.values.serverBindLocalhost}) {
	    			html.append("<div class=\"pref_input\"><span class=\"pref_key\">" + key + "</span><span class=\"pref_value\"><input type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
	    			prefs.remove(key);
		    	}
	    		html.append("</div><p class='clear'></p>");
	    		
	    		//Preferences for web frontend
	    		html.append("<div id='prefs_section_frontend' class='prefs_section'><h2 class='prefs_section_title'>" + "<i  class=\"fi-web\"></i>&nbsp;Web Frontend" + "</h2>");
	    		for (String key: new String[] {Preferences.values.webResourceFolder}) {
	    			html.append("<div class=\"pref_input\"><span class=\"pref_key\">" + key + "</span><span class=\"pref_value\"><input type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
	    			prefs.remove(key);
		    	}
	    		html.append("</div><p class='clear'></p>");
	    		
	    		//Preferences for the Twitter service
	    		html.append("<div id='prefs_section_twitter' class='prefs_section'><h2 class='prefs_section_title'>" + "<i  class=\"fa fa-twitter\"></i>&nbsp;Twitter Service" + "</h2>");
	    		for (String key: new String[] {"ACCESS_TOKEN", "ACCESS_TOKEN_KEY"}) {
	    			html.append("<div class=\"pref_input\"><span class=\"pref_key\">" + key + "</span><span class=\"pref_value\"><input type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
	    			prefs.remove(key);
		    	}
	    		html.append("</div><p class='clear'></p>");
	    		
	    		//Any remaining unclassified preferences
	    		if (!prefs.isEmpty()) {
	    			html.append("<div id='prefs_section_unknown' class='prefs_section'><h2 class='prefs_section_title'>" + "<i  class=\"fa fa-question\"></i>&nbsp;Uncategorized" + "</h2>");
		    		for (String key : prefs.keySet()) {
			    		html.append("<div class=\"pref_input\"><span class=\"pref_key\">" + key + "</span><span class=\"pref_value\"><input type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
			    		prefs.remove(key);
			    	}
		    		html.append("</div><p class='clear'></p>");
	    		}
	    		
		    	
		    	html.append("<a id=\"form_submit\" class=\"btn btn-default\" href=\"#\" onclick=\"document.getElementById('prefs').submit()\"><i class=\"fa fa-save\"></i>&nbsp;&nbsp;Save Preferences to " + configFile + "</a>");
		    	html.append("</form>");
		    	html.append("</div>");
		    	
		    	template = template.replace("MAIN_CONTENT", html.toString());
	    		returnBytes = template.getBytes();
	    	}
	    	return returnBytes;
	    }
	}
	
	/**
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 *
	 */
	private class ResourceHandler implements HttpHandler {
	    
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			String query = exchange.getRequestURI().getQuery();
			if (query != null) {
				HashMap<String, String> queryTags = SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
				if (queryTags.containsKey("file")) {
					Log.d(TAG, "value of 'file': '" + queryTags.get("file") + "'");
					String resourceName = queryTags.get("file");
					resourceName = resourceName.replaceAll("\\.\\./", "");
					try {
						//the file was found and read correctly
						byte[] response = getResponse(resourceName);
						//get the MIME type of the file
						//MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
						String file = Preferences.getInstance().getPreference(Preferences.values.webResourceFolder) +  resourceName;
						String mime = null;
						try (InputStream is = new FileInputStream(file);
						        BufferedInputStream bis = new BufferedInputStream(is);) {
						    AutoDetectParser parser = new AutoDetectParser();
						    Detector detector = parser.getDetector();
						    Metadata md = new Metadata();
						    md.add(Metadata.RESOURCE_NAME_KEY, resourceName);
						    MediaType mediaType = detector.detect(bis, md);
						    mime = mediaType.toString();
						}
						if (mime != null) {					
							com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
							headers.add("Content-Type", mime);
						}
					    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				        exchange.getResponseBody().write(response);
					} catch (IOException e) {
						//the file either does not exist or could not be read
						Log.e(TAG, "error opening resource '" + resourceFolder + resourceName + "' for reading");
						byte[] response = get404ErrorPage(resourceName).getBytes();
						exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
						exchange.getResponseBody().write(response);
					}
				} else {
					//If the query string did not contain a key/value pair for file then the request
					//+ is malformed
					byte[] response = get400BadRequestPage(exchange.getRequestURI()).getBytes();
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length);
					exchange.getResponseBody().write(response);
				}
			}
			exchange.close();
	    };
	    
	    private byte[] getResponse(String resourceName) throws IOException {
	    	return getFileBytes(resourceFolder + resourceName);
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
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			HashMap<String, String> queryMap = processQueryString(exchange.getRequestURI().getQuery());
			byte[] response;
			if (queryMap.containsKey("driver")) {
				String driverName = queryMap.get("driver");
				response = getResponse(driverName);
				if (response != null) {
					com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
					headers.add("Content-Type", "application/xml");
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
					exchange.getResponseBody().write(response);
				} else {
					response = get404ErrorPage("").getBytes();
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
					exchange.getResponseBody().write(response);
				}
			} else {
				response = get404ErrorPage("").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}
		
		public byte[] getResponse(String driverName) {
			byte[] response = null;
			String fullXml = Coordinator.requestFullPageXML(driverName);
			if (fullXml != null) {
				response = fullXml.getBytes();
			}
			return response;
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
			Log.d(TAG, "received request from " + exchange.getRemoteAddress() + " " +
					exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			HashMap<String, String> queryMap = processQueryString(exchange.getRequestURI().getQuery());
			byte[] response;
			if (queryMap.containsKey("driver")) {
				String driverName = queryMap.get("driver");
				response = getResponse(driverName);
				if (response != null) {
					com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
					headers.add("Content-Type", "application/xml");
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
					exchange.getResponseBody().write(response);
				} else {
					response = get404ErrorPage("").getBytes();
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
					exchange.getResponseBody().write(response);
				}
			} else {
				response = get404ErrorPage("").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}
		
		public byte[] getResponse(String driverName) {
			byte[] response = null;
			String widgetXml = Coordinator.requestWidgetXML(driverName);
			if (widgetXml != null) {
				response = widgetXml.getBytes();
			}
			return response;
		}
	}
}
