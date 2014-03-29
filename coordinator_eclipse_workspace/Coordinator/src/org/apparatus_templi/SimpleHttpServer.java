package org.apparatus_templi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

/**
 * A simple web server that acts as both a middle man between the Coordinator and any front ends, as
 * well as a host for the bundled web front end. The web server is implemented as a
 * {@link com.sun.net.httpserver.HttpsServer} and defines a series of
 * {@link com.sun.net.httpserver.HttpHandler}s that handle requests for virtual documents.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * @author Christopher Hagler <haglerchristopher@gmail.com>
 */
public class SimpleHttpServer implements Runnable {
	private boolean isRunning = true;
	private HttpServer httpsServer = null;
	private static final String TAG = "SimpleHttpServer";
	private String resourceFolder = "./website/";
	private String protocol;
	private String serverLocation;
	private int portNum;

	/**
	 * Generates a 404 error page for the given resourceName.
	 * 
	 * @param resourceName
	 *            the name of the file, resource, or URI that could not be found.
	 * @return a String representation of the HTML page to be returned to the user agent.
	 */
	private static String get404ErrorPage(String resourceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" + "<body>"
				+ "<h1>404</h1>");
		sb.append("<p>Error locating resource: " + resourceName + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Generates a 400 error page for the given URI.
	 * 
	 * @param uri
	 *            the URI request that could not be completed.
	 * @return a String representation of the HTML page to be returned to the user agent.
	 */
	private static String get400BadRequestPage(URI uri) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" + "<body>"
				+ "<h1>400</h1>");
		sb.append("Malformed request: " + uri + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Attempts to load the contents of the given file into an array of bytes.
	 * 
	 * @param fileName
	 *            the name of the file to open. The file name may not be null.
	 * @return the byte array contents of the file, or null if there was an error reading the file.
	 */
	private byte[] getFileBytes(String fileName) {
		assert fileName != null;

		byte[] returnBytes = null;
		try {
			InputStream is = new FileInputStream(fileName);
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
	 * Attempts to create a Socket on the given port number. If a socket can be created then the
	 * port number is available for binding.
	 * 
	 * @param port
	 *            the port number to check
	 * @return true if the socket could be bound, false otherwise.
	 */
	private boolean isPortAvailable(InetAddress addr, int port) {
		boolean portAvailable = false;
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			portAvailable = true;
		} catch (IOException e) {
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return portAvailable;
	}

	/**
	 * Breaks the given query string into a series of key/value pairs.
	 * 
	 * @param query
	 *            the query string to process. This should be the contents of the request string
	 *            after '?'
	 * @return a HashMap representation of the key/value pairs.
	 */
	private static HashMap<String, String> processQueryString(String query) {
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

	private String bestAddress() {
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

	/**
	 * Notify the web server that it should terminate all current connections and exit its run()
	 * method.
	 */
	void terminate() {
		this.isRunning = false;
	}

	/**
	 * Start the web server bound to the loop-back interface on the given port number. If the port
	 * number is already in use then the server will not attempt to find the next available port and
	 * will trigger a terminal failure.
	 * 
	 * @param portNumber
	 *            the port number the server should bind to
	 */
	public SimpleHttpServer(int portNumber) {
		this(portNumber, false);
	}

	/**
	 * Start the web server bound to the loop-back interface on the given port if possible,
	 * optionally binding to a different port if the given port number is not available.
	 * 
	 * @param portNumber
	 *            the port number the server will attempt to bind to
	 * @param autoIncrement
	 *            if true then the server will attempt to find the next available port if the one
	 *            specified is not available. If false, and the given port number is not available,
	 *            then the server will shutdown the service with a terminal failure.
	 */
	public SimpleHttpServer(int portNumber, boolean autoIncrement) {
		this(portNumber, autoIncrement, false);
	}

	/**
	 * Start the web server on the given port number if available, optionally binding to either the
	 * loop-back interface or the localhost interface, optionally attempting to find the next
	 * available port number if the port specified is not available.
	 * 
	 * @param portNumber
	 *            the port number to attempt to bind to.
	 * @param autoIncrement
	 *            if true the server will attempt to find the next available port if the specified
	 *            one is not available. If false, and the given port number is not available, then
	 *            the server will shutdown the service with a terminal failure.
	 * @param bindLocalhost
	 *            if true then the server will bind to both the localhost interface, and will be
	 *            visible to other computers on the same network. If false then the server will bind
	 *            to the loopback interface and will only be visible on the same computer running
	 *            the service.
	 */
	public SimpleHttpServer(int portNumber, boolean autoIncrement, boolean bindLocalhost) {
		this.isRunning = true;
		try {
			InetAddress address = null;

			if (bindLocalhost) {
				// try to find the best interface to bind to. Build a list of all available
				// addresses
				address = InetAddress.getByName(bestAddress());
			} else {
				address = InetAddress.getLoopbackAddress();
			}

			// create a InetSocket on the port
			InetSocketAddress socket;
			if (autoIncrement) {
				while (!isPortAvailable(address, portNumber)) {
					portNumber++;
				}
			} else {
				// There seems to be a Windows specific bug here. If the server is being restarted
				// then we can not reuse the same port number. We can try checking for a few seconds
				long endWait = System.currentTimeMillis() + 6000;
				while (!isPortAvailable(address, portNumber)
						&& System.currentTimeMillis() < endWait) {
					Log.d(TAG, "waiting for port " + portNumber + " to be available");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
				if (!isPortAvailable(address, portNumber)) {
					Coordinator.exitWithReason("could not start web server on port " + portNumber);
				}
			}

			socket = new InetSocketAddress(address, portNumber);
			try {
				httpsServer = HttpServer.create(socket, 0);
				SSLContext sslContext = SSLContext.getInstance("TLS");

				// initialise the keystore
				char[] password = "simulator".toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				FileInputStream fis = new FileInputStream("lig.keystore");
				ks.load(fis, password);

				// setup the key manager factory
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, password);

				// setup the trust manager factory
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				tmf.init(ks);

				// setup the HTTPS context and parameters
				sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				// httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				// @Override
				// public void configure(HttpsParameters params) {
				// try {
				// // Initialize the SSL context
				// SSLContext c = SSLContext.getDefault();
				// SSLEngine engine = c.createSSLEngine();
				// params.setNeedClientAuth(false);
				// params.setCipherSuites(engine.getEnabledCipherSuites());
				// params.setProtocols(engine.getEnabledProtocols());
				//
				// // Get the default parameters
				// SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
				// params.setSSLParameters(defaultSSLParameters);
				// } catch (Exception ex) {
				// Log.t(TAG, "Could not start https server");
				// Coordinator.exitWithReason("Could not start https server");
				// }
				// }
				// });
				if (httpsServer instanceof HttpsServer) {
					protocol = "https://";
				} else {
					protocol = "http://";
				}
				if (!bindLocalhost) {
					this.serverLocation = "localhost";
				} else {
					this.serverLocation = httpsServer.getAddress().getHostString();
					// this.serverLocation = InetAddress.getLocalHost().getHostAddress();
				}
				this.portNum = socket.getPort();
				// TODO getting the address on localhost does not work, only loopback
				Log.d(TAG, "setting address and port to " + this.serverLocation + " "
						+ this.portNum);
				Log.c(TAG, "server available at " + this.protocol + this.serverLocation + ":"
						+ this.portNum + "/index.html");

			} catch (SocketException e) {
				Log.t(TAG, "could not bind to port " + portNumber + ": " + e.getMessage());
				Coordinator.exitWithReason("could not bind to port " + portNumber + ": "
						+ e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				Log.t(TAG, "could not use requested encryption algorithm " + e.getMessage());
				Coordinator.exitWithReason("could not use requested encryption algorithm "
						+ e.getMessage());
			} catch (KeyStoreException e) {
				Log.t(TAG, "could not load key store " + e.getMessage());
				Coordinator.exitWithReason("could not load key store " + e.getMessage());
			} catch (CertificateException e) {
				Log.d(TAG, "bad certificate " + e.getMessage());
				Coordinator.exitWithReason("bad certificate " + e.getMessage());
			} catch (UnrecoverableKeyException e) {
				Log.d(TAG, "unrecoverable key " + e.getMessage());
				Coordinator.exitWithReason("unrecoverable key " + e.getMessage());
			} catch (KeyManagementException e) {
				Log.d(TAG, "key management exception " + e.getMessage());
				Coordinator.exitWithReason("key management exception " + e.getMessage());
			}
			httpsServer.createContext("/index.html", new IndexHandler());
			httpsServer.createContext("/about.html", new AboutHandler());
			httpsServer.createContext("/info.html", new InfoHandler());
			// httpsServer.createContext("/", new IndexHandler());
			httpsServer.createContext("/drivers.xml", new RunningDriversHandler());
			httpsServer.createContext("/full.xml", new FullXmlHandler());
			httpsServer.createContext("/widget.xml", new WidgetXmlHandler());
			httpsServer.createContext("/resource", new ResourceHandler());
			httpsServer.createContext("/js/default.js", new JsHandler());
			httpsServer.createContext("/settings.html", new SettingsHandler());
			httpsServer.createContext("/update_settings", new UpdateSettingsHandler());
			httpsServer.createContext("/restart_module", new RestartModuleHandler());
			httpsServer.createContext("/log.txt", new LogHandler());
			httpsServer.createContext("/send_command", new SendCommandHandler());
			// httpsServer.createContext("/", new IndexHandler());
			httpsServer.setExecutor(null);
			Log.d(TAG, "waiting on port " + portNumber);
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialize the server");
			e.printStackTrace();
		}
	}

	/**
	 * Set the path of the web resource folder used by the front-end. This folder will be used as
	 * the top level for all resource requests.
	 * 
	 * @param path
	 *            the path of the website resources
	 */
	public void setResourceFolder(String path) {
		// TODO check for a valid path
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		Log.d(TAG, "using resources in '" + path + "'");
		resourceFolder = path;
	}

	/**
	 * Returns the port number the server is bound to.
	 * 
	 */
	public int getPort() {
		return portNum;
	}

	/**
	 * Returns the String representation of the current server address. This address should either
	 * be \"localhost\" or an IP address (i.e. \"192.168.0.1\").
	 * 
	 * @return The String representation of the current address.
	 */
	public String getServerLocation() {
		return serverLocation;
	}

	/**
	 * Returns the protocol type
	 * 
	 * @return one of "http://" or "https://"
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Main loop of the web service. Constantly check if the service should terminate and, if not,
	 * sleep for a short time. If the server should terminate then the internal HttpsServer is
	 * stopped and the run() method will exit.
	 */
	@Override
	public void run() {
		Thread.currentThread().setName("SimpleHttpServer");
		httpsServer.start();
		while (isRunning) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.d(TAG, "terminating");
		httpsServer.stop(2);
	}

	/**
	 * Handles requests for "/index.html". Opens the contents of the template html file, then
	 * replaces the main body content with that of the index.html template.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class IndexHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");

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
		}

		private byte[] getResponse() {
			byte[] returnBytes = null;

			byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.html");
			byte[] indexBytes = getFileBytes(resourceFolder + "inc/index.html");

			if (templateBytes != null && indexBytes != null) {
				String templateHtml = new String(templateBytes);
				String indexHtml = new String(indexBytes);
				templateHtml = templateHtml.replace("!MAIN_CONTENT!", indexHtml.toString());
				returnBytes = templateHtml.getBytes();
			}

			return returnBytes;
		}
	}

	private class InfoHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");

			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("info.html").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}

		private byte[] getResponse() {
			byte[] returnBytes = null;

			byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.html");
			byte[] indexBytes = getFileBytes(resourceFolder + "inc/info.html");

			if (templateBytes != null && indexBytes != null) {
				String templateHtml = new String(templateBytes);
				String indexHtml = new String(indexBytes);
				templateHtml = templateHtml.replace("!MAIN_CONTENT!", indexHtml.toString());
				returnBytes = templateHtml.getBytes();
			}

			return returnBytes;
		}
	}

	/**
	 * Handles requests for "/about.html". Opens the contents of the template html file, then
	 * replaces the main body content with that of the about.html template.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class AboutHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");

			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("about.html").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}

		private byte[] getResponse() {
			byte[] returnBytes = null;

			byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.html");
			byte[] indexBytes = getFileBytes(resourceFolder + "inc/about.html");

			if (templateBytes != null && indexBytes != null) {
				String templateHtml = new String(templateBytes);
				String indexHtml = new String(indexBytes);
				templateHtml = templateHtml.replace("!MAIN_CONTENT!", indexHtml.toString());
				returnBytes = templateHtml.getBytes();
			}

			return returnBytes;
		}
	}

	/**
	 * Handle requests for a list of running drivers. Generates XML in the form of:
	 * 
	 * <pre>
	 * <?xml version='1.0'?>
	 * <ModuleList>
	 * 		<Module name='this driver name' />
	 * 		<Module name='another driver name' />
	 * </ModuleList>
	 * </pre>
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * @author Christopher Hagler <haglerchristopher@gmail.com>
	 * 
	 */
	private class RunningDriversHandler implements HttpHandler {
		private final String xmlVersion = "<?xml version='1.0'?>";
		private final String header = "<ModuleList>";
		private final String footer = "</ModuleList>";

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			// HashMap<String, String> queryTags =
			// SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
			// Log.d(TAG, "value of 'foo': " + queryTags.get("foo"));
			byte[] response = getResponse();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		}

		private byte[] getResponse() {
			String xml = xmlVersion + header;
			ArrayList<String> runningDrivers = Coordinator.getLoadedDrivers();
			for (String s : runningDrivers) {
				xml += "<Module name='" + s + "' />";
			}
			xml += footer;
			return xml.getBytes();
		}
	}

	/**
	 * Handler to return a virtual javascript document containing variables that will change during
	 * runtime. Right now only returns the port number of server. May be removed at a later date.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class JsHandler implements HttpHandler {
		private final Prefs prefs = Prefs.getInstance();

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			byte[] response = getResponse();
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "application/javascript");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		}

		private byte[] getResponse() {
			String jsCode = "$portnum = " + prefs.getPreference(Prefs.Keys.portNum) + ";";
			return jsCode.getBytes();
		}
	}

	/**
	 * Handler to process requests for "/settings.html". This virtual document is populated with the
	 * current preference values, and contains a form to submit preferences back for saving.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class SettingsHandler implements HttpHandler {
		private final String ENC_WARNING = "<i class=\"fa fa-exclamation-triangle\"></i>&nbsp;&nbsp;";

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");

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
		}

		private byte[] getResponse() {
			byte[] returnBytes = null;
			byte[] templateBytes = getFileBytes(resourceFolder + "inc/template.html");
			if (templateBytes != null) {
				String template = new String(templateBytes);

				StringBuilder html = new StringBuilder();
				HashMap<String, String> prefs = Prefs.getInstance().getPreferencesMap();
				// remove any preferences that should be hidden from the frontend
				prefs.remove(Prefs.Keys.autoIncPort);

				String configFile = prefs.get(Prefs.Keys.configFile);

				// if the config file is not the default then we will show the full file path,
				// otherwise only the short name
				if (!configFile.equals(Prefs.DEF_PREFS.get(Prefs.Keys.configFile))) {
					File f = new File(configFile);
					configFile = f.getAbsolutePath();
				}

				// if the user is still using the default config file then show a warning
				boolean usingDefaultConfig = false;
				if (prefs.get(Prefs.Keys.configFile).equals(
						Prefs.DEF_PREFS.get(Prefs.Keys.configFile))) {
					usingDefaultConfig = true;
				}
				if (usingDefaultConfig) {
					html.append("<span class='warning_text'>The default config file can not be overwritten.  If you want to save your "
							+ "preferences, then set a new location below and click save preferences.  To use the "
							+ "new configuration file restart the service with the command line argument: "
							+ "<span class='console'>--configFile path/to/the/new/file</span></span>");
				}

				// TODO update to a form so that the settings can be sent back in a POST request
				html.append("<div id=\"prefs_form\">");

				// Buttons
				html.append("<div id='settings-buttons'>");
				// TODO what modules can be restarted?
				html.append("<span id=\"restart_all_button\" class=\"btn-group closed\" title='Restarting the service will re-read preferences from config file, restart all driver, and re-initialize the web server'>"
						+ "<a class=\"btn btn-danger\" href=\"/restart_module?module=all\"><i class=\"fa fa-refresh fa-fw\"></i> &nbsp;&nbsp;Restart Service</a>"
						+ "<a class=\"btn btn-danger dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">"
						+ "<span class=\"fa fa-caret-down\"></span></a>"
						+ "<ul class=\"dropdown-menu\">"
						+ "<li><a href=\"/restart_module?module=drivers\"><i class=\"fa fa-refresh fa-fw\"></i> Restart Drivers</a></li>"
						+ "<li><a href=\"/restart_module?module=web\"><i class=\"fa fa-refresh fa-fw\"></i> Restart Web Server</a></li>"
						+ "<li><a href=\"/restart_module?module=foobar\"><i class=\"fa fa-refresh fa-fw\"></i> Anything else?</a></li>"
						+ "</ul> </span>");

				// Save preferences button
				// if the config file is the default then we want the save preferences button to be
				// disabled until updated via javascript
				html.append("<span id='form_submit' ");
				if (configFile.equals(Prefs.DEF_PREFS.get(Prefs.Keys.configFile))) {
					html.append("class='btn btn-success disabled'>");
				} else {
					html.append("class ='btn btn-success' ");
					html.append("form=\"prefs\" ");
					html.append("onclick = \"document.getElementById('prefs').submit()\" >");
				}
				html.append("<i class=\"fa fa-save\"></i>&nbsp;&nbsp;"
						+ "Save Preferences to <span id='btn_conf_file'>" + configFile
						+ "</span></div>");

				// end submit span
				html.append("</span>");

				// end buttons div
				// html.append("</div>");

				// clear the elements
				// html.append("<div class=\"clear\"></div>");

				html.append("<form name='prefs' id='prefs' action=\"update_settings\" "
						+ "method=\"POST\" >\n");

				// settings boxes div
				html.append("<div id='settings_boxes'>");

				// Preferences for the main section
				html.append("<div id='prefs_section_main' class='prefs_section info-box'><div class='title'>"
						+ "<i  class=\"fa fa-code-fork\"></i>&nbsp;Main" + "</div>");
				html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
						+ "<i class=\"fa fa-question-circle\" "
						+ "title=\""
						+ StringEscapeUtils.escapeHtml4(Prefs.getInstance().getPreferenceDesc(
								Prefs.Keys.configFile))
						+ "\"></i>&nbsp;"
						+ Prefs.Keys.configFile
						+ "</span><span "
						+ "class=\"pref_value\"><input id='f_config_file' type=\"text\" name=\""
						+ Prefs.Keys.configFile
						+ "\" value=\""
						+ prefs.get(Prefs.Keys.configFile)
						+ "\" onChange='updateConfigFile()' onkeypress='updateConfigFile()' onkeyup='updateConfigFile()' onBlur='updateConfigFile()' /></span></div><br />\n");
				prefs.remove(Prefs.Keys.configFile);
				for (String key : new String[] { Prefs.Keys.serialPort, Prefs.Keys.driverList,
						Prefs.Keys.logFile }) {
					String value = prefs.get(key);
					// the serial port name can be a null value, but writing a null string
					// + will print "null" (a non-existent serial port). Write "" instead.
					if (key.equals(Prefs.Keys.serialPort) && value == null) {
						value = "";
					}
					html.append("<div class=\"pref_input\"><span class=\"pref_key \" title='test'>"
							+ "<i class=\"fa fa-question-circle\" "
							+ "title=\""
							+ StringEscapeUtils.escapeHtml4(Prefs.getInstance().getPreferenceDesc(
									key)) + "\"></i>&nbsp;" + key + "</span><span "
							+ "class=\"pref_value\"><input "
							+ (usingDefaultConfig ? "disabled='disabled'" : "")
							+ " type=\"text\" name=\"" + key + "\" value=\"" + value
							+ "\" /></span></div><br />\n");
					prefs.remove(key);
				}
				html.append("</div>");

				// Preferences for web server
				html.append("<div id='prefs_section_webserver'  class='prefs_section info-box'><div class='title'>"
						+ "<i class=\"fa fa-cloud\"></i>&nbsp;Web Server" + "</div>");
				for (String key : new String[] { Prefs.Keys.portNum, Prefs.Keys.serverBindLocalhost }) {
					String value = prefs.get(key);
					// TODO this is an ugly hack. If the user specified no port number in the config
					// file then a flag will be set to auto increment the port number, and the port
					// number would have been read from the default preferences. We need to simulate
					// this here by blanking the port entry if the auto increment flag was set.
					if (key.equals(Prefs.Keys.portNum)
							&& Prefs.getInstance().getPreference(Prefs.Keys.autoIncPort)
									.equals("true")) {
						value = "";
					}
					html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
							+ "<i class=\"fa fa-question-circle \" "
							+ "title=\""
							+ StringEscapeUtils.escapeHtml4(Prefs.getInstance().getPreferenceDesc(
									key)) + "\"></i>&nbsp;" + key + "</span><span "
							+ "class=\"pref_value\"><input "
							+ (key.equals((Prefs.Keys.portNum)) ? " type='number' " : "")
							+ (usingDefaultConfig ? " disabled='disabled' " : "")
							+ " type=\"text\" name=\"" + key + "\" value=\"" + value
							+ "\" /></span></div><br />\n");
					prefs.remove(key);
				}
				html.append("</div>");

				// Preferences for web frontend
				html.append("<div id='prefs_section_frontend' class='prefs_section info-box'><div class='title'>"
						+ "<i  class=\"fa fa-globe\"></i>&nbsp;Web Frontend" + "</div>");
				for (String key : new String[] { Prefs.Keys.webResourceFolder }) {
					html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
							+ "<i class=\"fa fa-question-circle \" "
							+ "title=\""
							+ StringEscapeUtils.escapeHtml4(Prefs.getInstance().getPreferenceDesc(
									key)) + "\"></i>&nbsp;" + key + "</span><span "
							+ "class=\"pref_value\"><input "
							+ (usingDefaultConfig ? "disabled='disabled'" : "")
							+ " type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key)
							+ "\" /></span></div><br />\n");
					prefs.remove(key);
				}
				html.append("</div>");

				// Preferences for the Twitter service
				html.append("<div id='prefs_section_twitter' class='prefs_section info-box'><div class='title'>"
						+ "<i  class=\"fa fa-twitter\"></i>&nbsp;Twitter Service" + "</div>");
				for (String key : new String[] { Prefs.Keys.twtrAccess, Prefs.Keys.twtrAccessKey }) {
					html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
							+ "<i class=\"fa fa-question-circle \" "
							+ "title=\""
							+ StringEscapeUtils.escapeHtml4(Prefs.getInstance().getPreferenceDesc(
									key)) + "\"></i>&nbsp;" + key + "</span><span "
							+ "class=\"pref_value\"><input "
							+ (usingDefaultConfig ? "disabled='disabled'" : "")
							+ " type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key)
							+ "\" /></span></div><br />\n");
					prefs.remove(key);
				}
				html.append("<p class='warning'>" + ENC_WARNING
						+ "All passwords are stored in plaintext.");
				html.append("</div>");

				// Any remaining unclassified preferences
				if (!prefs.isEmpty()) {
					html.append("<div id='prefs_section_unknown' class='prefs_section info-box'><div class='title'>"
							+ "<i  class=\"fa fa-question\"></i>&nbsp;Uncategorized" + "</div>");
					for (String key : prefs.keySet()) {
						html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
								+ "<i class=\"fa fa-question-circle \" "
								+ "title=\""
								+ StringEscapeUtils.escapeHtml4(Prefs.getInstance()
										.getPreferenceDesc(key)) + "\"></i>&nbsp;" + key
								+ "</span><span " + "class=\"pref_value\"><input "
								+ (usingDefaultConfig ? "disabled='disabled'" : "")
								+ " type=\"text\" name=\"" + key + "\" value=\"" + prefs.get(key)
								+ "\" /></span></div><br />\n");
						prefs.remove(key);
					}
					html.append("</div>");
				}
				// end settings boxes div
				html.append("</div>");
				html.append("</form>");
				// clear the elements
				html.append("<div class=\"clear\"></div>");
				// html.append("<hr class=\"fancy-line\"></hr>");

				// buttons div
				html.append("<div id=\"settings_buttons_div\">");

				html.append("</div></div>");

				template = template.replace("!MAIN_CONTENT!", html.toString());
				returnBytes = template.getBytes();
			}
			return returnBytes;
		}
	}

	/**
	 * Handler to return the contents of any arbitrary file within the web resources folder. The
	 * MIME type of the file is guessed using the Apache tika libary.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class ResourceHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			String query = exchange.getRequestURI().getQuery();
			if (query != null) {
				HashMap<String, String> queryTags = SimpleHttpServer.processQueryString(exchange
						.getRequestURI().getQuery());
				if (queryTags.containsKey("file")) {
					// Log.d(TAG, "value of 'file': '" + queryTags.get("file") + "'");
					String resourceName = queryTags.get("file");
					resourceName = resourceName.replaceAll("\\.\\./", "");
					// the file was found and read correctly
					byte[] response = getResponse(resourceName);
					// get the MIME type of the file
					String file = Prefs.getInstance().getPreference(Prefs.Keys.webResourceFolder)
							+ resourceName;
					String mime = null;
					try (InputStream is = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(is)) {
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
				} else {
					// If the query string did not contain a key/value pair for file then the
					// request
					// + is malformed
					byte[] response = get400BadRequestPage(exchange.getRequestURI()).getBytes();
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST,
							response.length);
					exchange.getResponseBody().write(response);
				}
			}
			exchange.close();
		}

		private byte[] getResponse(String resourceName) throws IOException {
			return getFileBytes(resourceFolder + resourceName);
		}
	}

	/**
	 * Handle request for a driver's full page XML. Calls
	 * {@link Coordinator#requestFullPageXML(String)} to get the driver's full page XML
	 * representation.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class FullXmlHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			HashMap<String, String> queryMap = processQueryString(exchange.getRequestURI()
					.getQuery());
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

		private byte[] getResponse(String driverName) {
			byte[] response = null;
			String fullXml = Coordinator.requestFullPageXML(driverName);
			if (fullXml != null) {
				response = fullXml.getBytes();
			}
			return response;
		}
	}

	/**
	 * Handle request for a driver's widget XML. Calls {@link Coordinator#requestWidgetXML(String)}
	 * to retrieve the Driver's widget XML data.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class WidgetXmlHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			HashMap<String, String> queryMap = processQueryString(exchange.getRequestURI()
					.getQuery());
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
					response = get404ErrorPage("Driver " + driverName + " did not respond")
							.getBytes();
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
					exchange.getResponseBody().write(response);
				}
			} else {
				response = get404ErrorPage("No driver specified").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}

		private byte[] getResponse(String driverName) {
			byte[] response = null;
			String widgetXml = Coordinator.requestWidgetXML(driverName);
			if (widgetXml != null) {
				response = widgetXml.getBytes();
			}
			return response;
		}
	}

	/**
	 * A handler to accept new preference values from POST data, then write that data back to the
	 * configuration file specified.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class UpdateSettingsHandler implements HttpHandler {
		private final Prefs prefs = Prefs.getInstance();

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");
			boolean newServerLoopback = true;
			try {
				InputStream in = exchange.getRequestBody();
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				byte buf[] = new byte[4096];
				for (int n = in.read(buf); n > 0; n = in.read(buf)) {
					bao.write(buf, 0, n);
				}
				String query = new String(bao.toByteArray());
				query = java.net.URLDecoder.decode(query, "UTF-8");
				// Log.d(TAG, "update_settings query body: " + query);
				if (query != null && !query.equals("")) {
					HashMap<String, String> newPrefs = processQueryString(query);
					prefs.savePreferences(newPrefs);
					if (newPrefs.get(Prefs.Keys.serverBindLocalhost).equals("true")) {
						newServerLoopback = false;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "unable to update preferences");
			}

			byte[] response = getResponse();
			// headers.add("Location", getProtocol() + getServerLocation() + ":" + getPort()
			// + "/settings.html");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		}

		private byte[] getResponse() {
			byte[] returnBytes = null;

			byte[] templateBytes = getFileBytes(resourceFolder + "redirect.html");

			if (templateBytes != null) {
				String templateHtml = new String(templateBytes);
				templateHtml = templateHtml.replace("!TIMEOUT!", "4");
				templateHtml = templateHtml.replace("!LOCATION!", getProtocol()
						+ getServerLocation() + ":" + getPort() + "/settings.html");
				returnBytes = templateHtml.getBytes();
			}

			return returnBytes;
		}
	}

	/**
	 * Handles request to restart service modules. Expects a query string in the form of
	 * ?module=some_module. Does not check that 'some_model' is a valid module name, only passes the
	 * given module name to {@link Coordinator#restartModule(String)} for further processing.
	 * Returns a HTML page with a timed javascript redirect to send the user to the correct hostname
	 * and port after a few seconds.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class RestartModuleHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/html");

			String query = exchange.getRequestURI().getQuery();
			HashMap<String, String> queryTags = null;
			if (query != null) {
				queryTags = SimpleHttpServer
						.processQueryString(exchange.getRequestURI().getQuery());
			}

			byte[] response = getResponse(queryTags.get("module"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();

			if (queryTags != null) {
				if (queryTags.containsKey("module")) {
					Coordinator.restartModule(queryTags.get("module"));
				}
			}
		}

		private byte[] getResponse(String module) {
			assert module != null : "module to restart can not be null";
			byte[] returnBytes = null;

			byte[] templateBytes = getFileBytes(resourceFolder + "redirect.html");

			if (templateBytes != null) {
				String templateHtml = new String(templateBytes);
				if (module.equals("all")) {
					templateHtml = templateHtml.replace("!TIMEOUT!", "15");
				} else {
					templateHtml = templateHtml.replace("!TIMEOUT!", "8");
				}
				if ((module != null) && (module.equals("web") || module.equals("all"))) {
					// The address the new server will be listening on may have changed
					String newAddress = null;
					if (Prefs.getInstance().getPreference(Prefs.Keys.serverBindLocalhost)
							.equals("true")) {
						newAddress = bestAddress();
					} else {
						newAddress = "localhost";
					}

					templateHtml = templateHtml.replace("!LOCATION!", getProtocol() + newAddress
							+ ":" + Prefs.getInstance().getPreference(Prefs.Keys.portNum)
							+ "/settings.html");
				} else {
					templateHtml = templateHtml.replace("!LOCATION!", getProtocol()
							+ getServerLocation() + ":" + getPort() + "/settings.html");
				}
				returnBytes = templateHtml.getBytes();
			}

			return returnBytes;
		}
	}

	private class LogHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// Log.d(TAG,
			// "received request from " + exchange.getRemoteAddress() + " "
			// + exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/plain");

			byte[] response = getResponse();
			if (response != null) {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = get404ErrorPage("logfile").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}

		private byte[] getResponse() {
			StringBuilder sb = new StringBuilder("");
			for (String logLine : Log.getRecentLog()) {
				sb.append(logLine + "\n");
			}

			return sb.toString().getBytes();
		}
	}

	private class SendCommandHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Log.d(TAG,
					"received request from " + exchange.getRemoteAddress() + " "
							+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
			com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
			headers.add("Content-Type", "text/plain");

			String query = exchange.getRequestURI().getQuery();
			HashMap<String, String> queryTags = null;
			if (query != null) {
				queryTags = SimpleHttpServer
						.processQueryString(exchange.getRequestURI().getQuery());
			}

			boolean commandSent = Coordinator.passCommand(TAG, queryTags.get("driver"),
					queryTags.get("command"));
			if (commandSent) {
				byte[] response = "OK".getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				byte[] response = "FAIL".getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
			exchange.close();
		}
	}
}
