package org.apparatus_templi.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

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
import org.apparatus_templi.web.handler.UpdatePasswordHandler;
import org.apparatus_templi.web.handler.UpdateSettingsHandler;
import org.apparatus_templi.web.handler.WidgetXmlHandler;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

//import java.io.FileInputStream;

/**
 * A simple web server that acts as both a middle man between the Coordinator and any front ends, as
 * well as a host for the bundled web front end. The web server is implemented as a
 * {@link com.sun.net.httpserver.HttpsServer} and defines a series of
 * {@link com.sun.net.httpserver.HttpHandler}s that handle requests for virtual documents. A simple
 * {@link com.sun.net.httpserver.Authenticator} is attached to most of the handlers if a username
 * and password has been set.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * @author Christopher Hagler <haglerchristopher@gmail.com>
 */
public class EncryptedWebServer extends org.apparatus_templi.web.AbstractWebServer {
	private static final String TAG = "EncryptedHttpServer";
	private HttpsServer httpsServer = null;
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
		httpsServer.stop(0);

		this.serverLocation = null;
		this.portNum = 0;
		this.resourceFolder = null;
		this.protocol = null;
		this.prefs = null;
	}

	@Override
	public void start() {
		httpsServer.start();
	}

	/**
	 * Starts the web server bound to the loopback interface
	 * 
	 * @throws Exception
	 */
	public EncryptedWebServer() throws Exception {
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
	public EncryptedWebServer(InetSocketAddress socket) throws Exception {
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
				socket = new InetSocketAddress(InetAddress.getByName(HttpHelper.bestAddress()),
						portNum);
			} else {
				socket = new InetSocketAddress(InetAddress.getLoopbackAddress(), portNum);
			}
		}

		// save the socket so we can reuse it to restart the server later on
		this.socket = socket;

		// create the HttpServer
		try {
			httpsServer = HttpsServer.create(socket, 0);

			SSLContext sslContext = SSLContext.getInstance("TLS");

			if (httpsServer instanceof HttpsServer) {
				try {
					// initialise the keystore
					char[] password = "simulator".toCharArray();
					KeyStore ks = KeyStore.getInstance("JKS");
					// FileInputStream fis = new FileInputStream("lig.keystore");
					// TODO reading from a fileinputstream is fine for now, but the dist jar will
					// need to read as resource stream instead. Is there a way to do both?
					// InputStream fis = this.getClass().getResourceAsStream("lig.keystore");
					InputStream fis = new FileInputStream("lig.keystore");
					ks.load(fis, password);

					// setup the key manager factory
					KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
					kmf.init(ks, password);

					// setup the trust manager factory
					TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
					tmf.init(ks);

					// setup the HTTPS context and parameters
					sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
					Log.d(TAG, "setting configurator");

					httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
						@Override
						public void configure(HttpsParameters params) {
							try {
								// Initialize the SSL context
								SSLContext c = SSLContext.getDefault();
								SSLEngine engine = c.createSSLEngine();
								params.setNeedClientAuth(false);
								params.setCipherSuites(engine.getEnabledCipherSuites());
								params.setProtocols(engine.getEnabledProtocols());

								// Get the default parameters
								SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
								params.setSSLParameters(defaultSSLParameters);
							} catch (Exception ex) {
								Log.e(TAG, "Could not start https server");
								// throw new Exception("Could not start https server");
							}
						}
					});
				} catch (Exception e) {
					Log.e(TAG, "unable to initalize secure server");
					throw new Exception("unable to initalize secure server");
				}
			}
			protocol = "https://";

			this.serverLocation = httpsServer.getAddress().getAddress().getHostAddress();

			// this.serverLocation = InetAddress.getLocalHost().getHostAddress();
			// }
			this.portNum = httpsServer.getAddress().getPort();
			// TODO getting the address on localhost does not work, only loopback
			Log.d(TAG, "setting address and port to " + this.serverLocation + " " + this.portNum);
			Log.c(TAG, "server available at " + this.protocol + this.serverLocation + ":"
					+ this.portNum + "/index.html");

		} catch (SocketException e) {
			Log.e(TAG, "could not bind to port " + socket.getPort() + ": " + e.getMessage());
			throw new SocketException("could not bind to port " + socket.getPort() + ": "
					+ e.getMessage());
		}

		ArrayList<HttpContext> contexts = new ArrayList<HttpContext>();
		contexts.add(httpsServer.createContext("/", new IndexHandler(this)));
		contexts.add(httpsServer.createContext("/index.html", new IndexHandler(this)));
		contexts.add(httpsServer.createContext("/about.html", new AboutHandler(this)));
		contexts.add(httpsServer.createContext("/info.html", new InfoHandler(this)));
		contexts.add(httpsServer.createContext("/drivers.xml", new RunningDriversHandler(this)));
		contexts.add(httpsServer.createContext("/full.xml", new FullXmlHandler(this)));
		contexts.add(httpsServer.createContext("/widget.xml", new WidgetXmlHandler(this)));
		contexts.add(httpsServer.createContext("/settings.html", new SettingsHandler(this)));
		contexts.add(httpsServer.createContext("/update_settings", new UpdateSettingsHandler(this)));
		contexts.add(httpsServer.createContext("/restart_module", new RestartModuleHandler(this)));
		contexts.add(httpsServer.createContext("/log.txt", new LogHandler(this)));
		contexts.add(httpsServer.createContext("/send_command", new SendCommandHandler(this)));
		contexts.add(httpsServer.createContext("/sys_status", new GetSysStatusHandler(this)));

		// for the sake of simplicity these handlers do not use any authenticator. This was done to
		// make sure that the redirect and password change pages can load resources. This should be
		// changed later
		// TODO replace with an authenticator
		httpsServer.createContext("/resource", new ResourceHandler(this));
		httpsServer.createContext("/js/default.js", new JsHandler(this));

		// Provides a form for setting a username/password pair. This handler uses no authenticator,
		// since authentication is done internally
		httpsServer.createContext("/set_password", new UpdatePasswordHandler(this));

		// if the user has specified a user/pass then restrict access based on those values
		if (Prefs.isCredentialsSet()) {
			for (HttpContext context : contexts) {
				context.setAuthenticator(new BasicAuthenticator("apparatus_templi") {
					@Override
					public boolean checkCredentials(String user, String pwd) {
						boolean allowed = false;
						if (Prefs.isCredentialsSet()) {
							final String username = Coordinator.readTextData("SYSTEM", "USERNAME");
							final String passHash = Coordinator.readTextData("SYSTEM", "PASSWORD");
							boolean goodUser = username.equals(user);
							boolean goodPass = false;
							try {
								goodPass = PasswordHash.validatePassword(pwd, passHash);
							} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
								Log.e(TAG, "unable to validate password with hash");
							}
							allowed = goodUser && goodPass;
						} else {
							allowed = true;
						}
						return allowed;
					}
				});
			}
		}

		// TODO there should probably be a limit on the number of posible threads
		httpsServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
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

	@Override
	public String getResourceFolder() {
		return this.resourceFolder;
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

}
