package org.apparatus_templi.web;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.handler.AboutHandler;
import org.apparatus_templi.web.handler.FullXmlHandler;
import org.apparatus_templi.web.handler.GetSysStatusHandler;
import org.apparatus_templi.web.handler.IndexHandler;
import org.apparatus_templi.web.handler.InfoHandler;
import org.apparatus_templi.web.handler.JsHandler;
import org.apparatus_templi.web.handler.LogHandler;
import org.apparatus_templi.web.handler.ResourceHandler;
import org.apparatus_templi.web.handler.RestartModuleHandler;
import org.apparatus_templi.web.handler.RunningDriversHandler;
import org.apparatus_templi.web.handler.SendCommandHandler;
import org.apparatus_templi.web.handler.SettingsHandler;
import org.apparatus_templi.web.handler.UpdateSettingsHandler;
import org.apparatus_templi.web.handler.WidgetXmlHandler;

import com.sun.net.httpserver.HttpServer;

//import java.io.FileInputStream;

/**
 * A simple web server that acts as both a middle man between the Coordinator and any front ends, as
 * well as a host for the bundled web front end. The web server is implemented as a
 * {@link com.sun.net.httpserver.HttpServer} and defines a series of
 * {@link com.sun.net.httpserver.HttpHandler}s that handle requests for virtual documents.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * @author Christopher Hagler <haglerchristopher@gmail.com>
 */
public class WebServer extends org.apparatus_templi.web.AbstractWebServer {
	private static final String TAG = "SimpleHttpServer";
	private HttpServer httpServer = null;
	private String resourceFolder = "./website/";
	private String protocol;
	private String serverLocation;
	private int portNum;
	private Prefs prefs = Coordinator.getPrefs();
	private final InetSocketAddress socket;

	/**
	 * Notify the web server that it should terminate all current connections and exit its run()
	 * method.
	 */
	@Override
	public void terminate() {
		httpServer.stop(0);

		this.serverLocation = null;
		this.portNum = 0;
		this.resourceFolder = null;
		this.protocol = null;
		this.prefs = null;
	}

	@Override
	public void start() {
		httpServer.start();
	}

	/**
	 * Starts the web server bound to the loopback interface
	 * 
	 * @throws Exception
	 */
	public WebServer() throws Exception {
		this(null);
	}

	/**
	 * Starts the web server bound to the first available port on the given InetSocketAddress
	 * 
	 * @param socket
	 *            the socket to bind the server to. If null then a new InetSocketAddress will be
	 *            constructed based off the users preferences
	 * @throws Exception
	 */
	public WebServer(InetSocketAddress socket) throws Exception {
		if (socket == null) {
			// create the connection based off of preferences
			boolean bindLocalhost = "true".equals(Coordinator.getPrefs().getPreference(
					Prefs.Keys.serverBindLocalhost));
			String portNumName = Coordinator.getPrefs().getPreference(Prefs.Keys.portNum);
			Integer portNum = null;
			try {
				portNum = Integer.parseInt(portNumName);
			} catch (NumberFormatException e) {
				// a port number of 0 will let the system pick up an ephemeral port
				portNum = 0;
			}

			if (bindLocalhost) {
				@SuppressWarnings("deprecation")
				String bestAddress = HttpHelper.bestAddress();
				socket = new InetSocketAddress(InetAddress.getByName(bestAddress), portNum);
			} else {
				socket = new InetSocketAddress(InetAddress.getLoopbackAddress(), portNum);
			}
		}

		// save the socket so we can reuse it to restart the server later on
		this.socket = socket;

		// create the HttpServer
		try {
			httpServer = HttpServer.create(socket, 0);
			protocol = "http://";

			this.serverLocation = httpServer.getAddress().getAddress().getHostAddress();

			this.portNum = httpServer.getAddress().getPort();

			// TODO getting the address on localhost does not work, only loopback
			Log.d(TAG, "setting address and port to " + this.serverLocation + " " + this.portNum);
			Log.c(TAG, "server available at " + this.protocol + this.serverLocation + ":"
					+ this.portNum + "/index.html");

		} catch (SocketException e) {
			Log.e(TAG, "could not bind to port " + socket.getPort() + ": " + e.getMessage());
			throw new SocketException("could not bind to port " + socket.getPort() + ": "
					+ e.getMessage());
		}

		httpServer.createContext("/index.html", new IndexHandler(this));
		httpServer.createContext("/about.html", new AboutHandler(this));
		httpServer.createContext("/info.html", new InfoHandler(this));
		// httpServer.createContext("/", new IndexHandler(this));
		httpServer.createContext("/drivers.xml", new RunningDriversHandler(this));
		httpServer.createContext("/full.xml", new FullXmlHandler(this));
		httpServer.createContext("/widget.xml", new WidgetXmlHandler(this));
		httpServer.createContext("/resource", new ResourceHandler(this));
		httpServer.createContext("/js/default.js", new JsHandler(this));
		httpServer.createContext("/settings.html", new SettingsHandler(this));
		httpServer.createContext("/update_settings", new UpdateSettingsHandler(this));
		httpServer.createContext("/restart_module", new RestartModuleHandler(this));
		httpServer.createContext("/log.txt", new LogHandler(this));
		httpServer.createContext("/send_command", new SendCommandHandler(this));
		httpServer.createContext("/sys_status", new GetSysStatusHandler(this));
		// httpServer.createContext("/", new IndexHandler(this));
		httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		Log.d(TAG, "waiting on port " + this.portNum);
	}

	/**
	 * Set the path of the web resource folder used by the front-end. This folder will be used as
	 * the top level for all resource requests.
	 * 
	 * @param path
	 *            the path of the website resources
	 */
	@Override
	public void setResourceFolder(String path) throws IllegalArgumentException {
		// TODO check for a valid path
		if (path == null) {
			throw new IllegalArgumentException("resource path can not be null");
		}
		if (!Files.exists(new File(path).toPath()) || !Files.isDirectory(new File(path).toPath())) {
			throw new IllegalArgumentException("Invalid Path: " + path);
		}

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
	@Override
	public int getPort() {
		return portNum;
	}

	/**
	 * Returns the String representation of the current server address. This address should either
	 * be \"localhost\" or an IP address (i.e. \"192.168.0.1\").
	 * 
	 * @return The String representation of the current address.
	 */
	@Override
	public String getServerLocation() {
		return serverLocation;
	}

	/**
	 * Returns the protocol type
	 * 
	 * @return one of "http://" or "https://"
	 */
	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public InetSocketAddress getSocket() {
		return this.socket;
	}

	@Override
	public String getResourceFolder() {
		return this.resourceFolder;
	}

}
