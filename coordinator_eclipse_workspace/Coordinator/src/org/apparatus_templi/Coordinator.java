package org.apparatus_templi;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apparatus_templi.driver.ControllerModule;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.driver.SensorModule;
import org.apparatus_templi.service.SQLiteDbService;

/**
 * Coordinates message passing and driver loading. Handles setting up the environment, querying
 * remote modules, loading drivers, and starting the front end support. Coordinator provides wrapper
 * methods to facilitate communication between the front end, drivers, and the remote modules.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public class Coordinator {
	private static final String TAG = "Coordinator";
	public static final long threadId = Thread.currentThread().getId();
	private static HashMap<String, String> remoteModules = new HashMap<String, String>();
	private static ConcurrentHashMap<String, Driver> loadedDrivers = new ConcurrentHashMap<String, Driver>();
	private static ConcurrentHashMap<Driver, Thread> driverThreads = new ConcurrentHashMap<Driver, Thread>();
	private static ConcurrentHashMap<Driver, Long> scheduledWakeUps = new ConcurrentHashMap<Driver, Long>();
	private static ConcurrentHashMap<Class<? extends Event>, ArrayList<EventWatcher>> eventWatchers = new ConcurrentHashMap<Class<? extends Event>, ArrayList<EventWatcher>>();
	private static SerialConnection serialConnection = null;
	private static MessageCenter messageCenter = MessageCenter.getInstance();
	private static SimpleHttpServer webServer = null;
	private static Thread webServerThread = null;
	private static Prefs prefs = Prefs.getInstance();
	private static boolean connectionReady = false;

	private static SysTray sysTray;

	// version information
	public static final int VERSION_NUMBER = 0;
	public static final String RELEASE_NUMBER = "20140326";

	/**
	 * Sends the given message to the correct driver specified by {@link Message#getDestination()}.
	 * If the module that sent this message was not previously known, then a record of its presence
	 * is saved. If a driver matching that destination is loaded and its state is not
	 * {@link Thread.State#TERMINATED} then the message contents will be routed to the drivers
	 * {@link Driver#receiveCommand(String)} or {@link Driver#receiveBinary(byte[])} methods. If the
	 * driver is currently TERMINATED then the message contents will be placed in the drivers
	 * appropriate queue, and the driver will be woken.
	 * 
	 * @param m
	 *            the {@link Message} to route
	 */
	private static synchronized void routeIncomingMessage(Message m) {
		assert m != null : "we should always have a valid message to work with";

		// Log.d(TAG, "routeIncomingMessage()");
		String destination = m.getDestination();
		assert destination != null : "messge can not have null destination";

		if (!isModulePresent(destination)) {
			Log.d(TAG, "adding remote module '" + destination + "' to the list of known modules");
			remoteModules.put(destination, "");
		}

		if (loadedDrivers.containsKey(destination)) {
			Driver driver = loadedDrivers.get(destination);
			if (driverThreads.get(driver).getState() == Thread.State.TERMINATED) {
				// If the driver is not currently running, then we will
				// re-initialize it,
				// + queue the message, then start execution.
				Log.d(TAG, "restarting terminated driver '" + destination
						+ "' for incoming message");
				try {
					Driver newDriver = wakeDriver(driver.getName(), false);
					if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
						newDriver.queueBinary(m.getData());
					} else {
						newDriver.queueCommand(new String(m.getData()));
					}
					driverThreads.get(newDriver).start();
				} catch (Exception e) {
					Log.e(TAG, "error restarting driver '" + driver.getName()
							+ "', incoming message will " + "be discarded");
				}
			} else {
				if (m.getTransmissionType() == Message.BINARY_TRANSMISSION) {
					driver.receiveBinary(m.getData());
				} else {
					driver.receiveCommand(new String(m.getData()));
				}

				// If this driver was scheduled to sleep until a new message
				// arrives
				// + then we need to notify it to wake
				if (scheduledWakeUps.get(driver) != null && scheduledWakeUps.get(driver) == 0) {
					scheduledWakeUps.remove(driver);
					try {
						driver.notify();
					} catch (IllegalMonitorStateException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			Log.w(TAG, "incoming message to " + destination
					+ " could not be routed: no such driver loaded");
		}
	}

	/**
	 * Sends a query string to all remote modules "ALL:READY?"
	 */
	private static synchronized void queryRemoteModules() {
		assert messageCenter != null : "message center is not instantiated";

		messageCenter.sendCommand("ALL", "READY?");
	}

	/**
	 * Loads the given driver, provided that the driver is not null, has a valid name, is not of
	 * type {@link Driver}, and a driver with a matching name has not already been loaded.
	 * 
	 * @param d
	 *            the driver to load, must not be null.
	 * @return true if the given driver was loaded, false otherwise.
	 */
	private static boolean loadDriver(Driver d) {
		assert d != null : "given driver must not be null";

		boolean isDriverLoaded = false;
		if (d.getModuleName() != null) {
			if (!loadedDrivers.containsKey(d.getModuleName())
					&& (d instanceof ControllerModule || d instanceof SensorModule)
					&& d.getName().length() <= 10) {
				loadedDrivers.put(d.getModuleName(), d);
				Log.d(TAG, "driver " + d.getModuleName() + " of type " + d.getClass().getName()
						+ " initialized");
				isDriverLoaded = true;
			} else {
				Log.e(TAG, "error loading driver " + d.getClass().getName());
			}
		}

		return isDriverLoaded;
	}

	/**
	 * A wrapper method for wakeDriver that automatically restarts the driver's thread after
	 * re-initialization. This wrapper will also restart TERMINATED threads.
	 * 
	 * @param driverName
	 *            the name of the {@link Driver} to restart.
	 * @return a reference to the newly restarted Driver.
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private static synchronized Driver wakeDriver(String driverName) {
		assert driverName != null : "given driver name can not be null";

		return wakeDriver(driverName, true);
	}

	/**
	 * A wrapper method for wakeDriver() that optionally starts the woken driver. This method will
	 * also wake TERMINATED threads.
	 * 
	 * @param driverName
	 *            the name of the {@link Driver} to wake.
	 * @param autoStart
	 *            whether or not to automatically begin execution of re-created threads
	 * @return a reference to the woken Driver.
	 */
	private static synchronized Driver wakeDriver(String driverName, boolean autoStart) {
		assert driverName != null : "given driver name can not be null";
		return wakeDriver(driverName, autoStart, true);
	}

	/**
	 * 
	 * 
	 * @param driverName
	 * 
	 * @param autoStart
	 *            if true the driver's thread will be started, otherwise the thread will have to be
	 *            started manually.
	 * @return a reference to the newly woken Driver.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */

	/**
	 ** Wake the given driver, optionally creating a new thread for the {@link Driver}, optionally
	 * automatically calling start() on the new thread.
	 * 
	 * @param driverName
	 *            the name of the {@link Driver} to wake.
	 * @param autoStart
	 *            if true the driver's thread will be automatically started after creation
	 * @param wakeTerminated
	 *            if true then drivers whose thread state is TERMINATED will have a new thread
	 *            assigned.
	 * @return a reference to the woken Driver.
	 */
	private static synchronized Driver wakeDriver(String driverName, boolean autoStart,
			boolean wakeTerminated) {
		assert driverName != null : "given driver name can not be null";
		// NOTE using containsKey() does not always work for String values (different hash code may
		// be generated)
		assert loadedDrivers.get(driverName) != null : "driver " + driverName
				+ " must exist within the loaded drivers table";

		Driver d = loadedDrivers.get(driverName);
		Thread t = driverThreads.get(d);
		assert d != null && t != null : "the driver and its thread must be valid objects";

		if (t.getState() == Thread.State.TERMINATED && wakeTerminated) {
			Log.d(TAG, "restarting driver '" + d.getName() + "' of class '" + d.getClass()
					+ "' of type '" + d.getClass().getName() + "'");
			scheduledWakeUps.remove(d);
			driverThreads.remove(d);
			Thread newThread = new Thread(d);
			newThread.setPriority(Thread.MIN_PRIORITY);
			driverThreads.put(d, newThread);
			if (autoStart) {
				newThread.start();
			}
		} else {
			try {
				scheduledWakeUps.remove(d);
				d.wake();
				Log.d(TAG, "waking driver " + driverName);
			} catch (Exception e) {
				Log.d(TAG, "could not wake driver " + driverName);
			}
		}
		return d;
	}

	/**
	 * Parse all command line arguments. Any preferences read from the command like will be put into
	 * the {@link Prefs} singleton.
	 * 
	 * @param argv
	 *            the command line arguments to be parsed
	 */
	private static void parseCommandLineOptions(String[] argv) {
		// Using apache commons cli to parse the command line options
		Options options = new Options();
		options.addOption("help", false, "Display this help message.");

		@SuppressWarnings("static-access")
		Option portOption = OptionBuilder.withArgName(Prefs.Keys.portNum).hasArg()
				.withDescription("Bind the server to the given port number")
				.create(Prefs.Keys.portNum);
		options.addOption(portOption);

		@SuppressWarnings("static-access")
		Option serialOption = OptionBuilder.withArgName(Prefs.Keys.serialPort).hasArg()
				.withDescription("Connect to the arduino on serial interface")
				.create(Prefs.Keys.serialPort);
		options.addOption(serialOption);

		@SuppressWarnings("static-access")
		Option configOption = OptionBuilder.withArgName(Prefs.Keys.configFile).hasArg()
				.withDescription("Path to the configuration file").create(Prefs.Keys.configFile);
		options.addOption(configOption);

		@SuppressWarnings("static-access")
		Option resourcesOption = OptionBuilder.withArgName(Prefs.Keys.webResourceFolder).hasArg()
				.withDescription("Web frontend will use resources in the specified folder")
				.create(Prefs.Keys.webResourceFolder);
		options.addOption(resourcesOption);

		CommandLineParser cliParser = new org.apache.commons.cli.PosixParser();
		try {
			CommandLine cmd = cliParser.parse(options, argv);
			if (cmd.hasOption("help")) {
				// show help message and exit
				HelpFormatter formatter = new HelpFormatter();
				formatter.setOptPrefix("--");
				formatter.setLongOptPrefix("--");

				formatter.printHelp(TAG, options);
				System.exit(0);
			}

			// Load the configuration file URI
			String configFile;
			if (cmd.hasOption(Prefs.Keys.configFile)) {
				configFile = cmd.getOptionValue(Prefs.Keys.configFile);
				if (configFile.startsWith("~" + File.separator)) {
					configFile = System.getProperty("user.home") + configFile.substring(1);
				}
			} else {
				configFile = Prefs.DEF_PREFS.get(Prefs.Keys.configFile);
			}
			prefs.putPreference(Prefs.Keys.configFile, configFile);

			// Read in preferences from the config file
			prefs.readPreferences(configFile);

			// Read additional preferences from the command line options,
			// + overwriting preferences in the config file.

			// If the user specified a port number then we will only
			// try binding to that port, else we try the default port number
			if (cmd.hasOption(Prefs.Keys.portNum)) {
				prefs.putPreference(Prefs.Keys.portNum, cmd.getOptionValue(Prefs.Keys.portNum));
				// If the config file has no port number specified but one was given on the command
				// line then we do not want to auto increment the port number
				prefs.putPreference(Prefs.Keys.autoIncPort, "false");
			}

			if (cmd.hasOption(Prefs.Keys.webResourceFolder)) {
				prefs.putPreference(Prefs.Keys.webResourceFolder,
						cmd.getOptionValue(Prefs.Keys.webResourceFolder));
			}

			// if we were given a preferred port we will pass it to
			// SerialConnection
			// + when initialized
			if (cmd.hasOption(Prefs.Keys.serialPort)) {
				prefs.putPreference(Prefs.Keys.serialPort,
						cmd.getOptionValue(Prefs.Keys.serialPort));
			}

		} catch (ParseException e) {
			System.out.println("Error processing options: " + e.getMessage());
			new HelpFormatter().printHelp("Diff", options);
			Coordinator.exitWithReason("Error parsing command line options");
		}
	}

	/**
	 * Opens a connection to the serial device specified in {@link Prefs}. If the specified serial
	 * address is null then a dummy serial connection will be used. If the serial device specified
	 * can not be bound to then will shutdown the service.
	 */
	private static void openSerialConnection() {
		String serialPortName = prefs.getPreference(Prefs.Keys.serialPort);
		if (serialPortName == null) {
			serialConnection = new UsbSerialConnection();
		} else if (serialPortName.equals("dummy")) {
			serialConnection = new DummySerialConnection();
		} else {
			serialConnection = new UsbSerialConnection(serialPortName);
		}

		if (!serialConnection.isConnected()) {
			Coordinator.exitWithReason("could not connect to serial port '" + serialPortName + "'");
		}
	}

	/**
	 * Start the web server on the port specified in {@link Prefs} bound to the address specified in
	 * {@link Prefs}. If the specified host and port can not be bound to then will sutdown the
	 * entire service.
	 * 
	 * @throws UnknownHostException
	 *             if the address could not be bound to.
	 */
	private static void startWebServer() throws UnknownHostException {
		int portNum;
		boolean autoIncPort = prefs.getPreference(Prefs.Keys.autoIncPort).equals("true") ? true
				: false;
		boolean bindLocalhost = prefs.getPreference(Prefs.Keys.serverBindLocalhost).equals("true") ? true
				: false;

		try {
			portNum = Integer.valueOf(prefs.getPreference(Prefs.Keys.portNum));
		} catch (NumberFormatException e) {
			portNum = Integer.parseInt(Prefs.DEF_PREFS.get(Prefs.Keys.portNum));
		}
		if (bindLocalhost) {
			Log.c(TAG, "Starting web server on "
					+ (autoIncPort ? "random port " : "port " + portNum)
					+ " bound to localhost address " + InetAddress.getLocalHost().getHostAddress());
		} else {
			Log.c(TAG, "Starting web server on "
					+ (autoIncPort ? "random port " : "port " + portNum)
					+ " bound to loopback address");
		}

		webServer = new SimpleHttpServer(portNum, autoIncPort, bindLocalhost);
		webServer.setResourceFolder(prefs.getPreference(Prefs.Keys.webResourceFolder));
		webServerThread = new Thread(webServer);

		webServerThread.start();
		sysTray.setStatus(SysTray.Status.RUNNING);
	}

	private static void restartWebServer() throws UnknownHostException, InterruptedException {
		sysTray.setStatus(SysTray.Status.WAITING);
		assert webServer != null : "attempting to restart web server when one is already active";
		Log.w(TAG, "restarting web server");

		webServer.terminate();
		Thread.sleep(3000);
		while (webServerThread.getState() != Thread.State.TERMINATED) {
			Log.d(TAG, "waiting on web server to terminate");
			Thread.sleep(200);
		}
		webServer = null;
		startWebServer();
	}

	private static void startDrivers() {
		// this should never be called when drivers are currently running
		assert loadedDrivers.isEmpty() : "list of loaded drivers was not empty when starting drivers";
		assert driverThreads.isEmpty() : "list of driver threads was not empty when starting drivers";

		// Instantiate all drivers specified in the config file
		String driverList = prefs.getPreference(Prefs.Keys.driverList);
		assert driverList != null : "driver list should not be null";

		if (driverList.equals("")) {
			Log.w(TAG,
					"No drivers were specified in the configuration " + "file: '"
							+ prefs.getPreference(Prefs.Keys.configFile)
							+ "', nothing will be loaded");
		} else {
			Log.c(TAG, "Initializing drivers...");
			String[] drivers = driverList.split(",");
			for (String driverClassName : drivers) {
				try {
					Class<?> c = Class.forName("org.apparatus_templi.driver." + driverClassName);
					Driver d = (Driver) c.newInstance();
					loadDriver(d);
				} catch (Exception e) {
					Log.d(TAG, "unable to load driver '" + driverClassName + "'");
				}
			}

		}

		// Start the driver threads
		for (String driverName : loadedDrivers.keySet()) {
			Log.c(TAG, "Starting driver " + driverName);
			Thread t = new Thread(loadedDrivers.get(driverName));
			t.setPriority(Thread.MIN_PRIORITY);
			driverThreads.put(loadedDrivers.get(driverName), t);
			t.start();
		}
	}

	private static void stopDrivers() {
		Log.w(TAG, "Restarting all drivers");
		Log.d(TAG, "removing all event watchers");
		eventWatchers.clear();

		for (String driverName : loadedDrivers.keySet()) {
			Driver d = loadedDrivers.get(driverName);
			Log.d(TAG, "terminating driver " + d.getName());
			d.terminate();

			// only wake sleeping drivers, not TERMINATED ones
			wakeDriver(d.getName(), false, false);
			// loadedDrivers.remove(driverName);
		}

		// Block for a few seconds to allow all drivers to finish their
		// termination procedure. Since the drivers may call methods in this
		// thread we need to do a non-blocking wait instead of using a call to
		// Thread.sleep()
		long sleepTime = System.currentTimeMillis() + (1000 * 5);
		while (sleepTime > System.currentTimeMillis()) {
		}

		int tryCount = 0;
		while (!driverThreads.isEmpty()) {
			for (Driver d : driverThreads.keySet()) {
				Thread t = driverThreads.get(d);
				if (t.getState() == Thread.State.TERMINATED) {
					Log.d(TAG, "driver " + d.getName() + " terminated");
					driverThreads.remove(d);
				} else {
					// we don't want to block forever waiting on a non-responsive driver thread
					if (tryCount == 20) {
						Log.w(TAG, "driver " + d.getName()
								+ " non-responsive, interrupting thread.");
						t.interrupt();
						// t.notifyAll();
					}

					// Something is seriously wrong if the driver has not stopped by now. We should
					// probably notify the user that the service is experiencing problems and should
					// be restarted
					if (tryCount == 30) {
						Log.e(TAG, "driver " + d.getName()
								+ " still non-responsive, force stopping");
						t.stop();
					}

					Log.d(TAG, "waiting on driver " + d.getName() + " to terminate (state: "
							+ t.getState().toString() + ")");
					d.terminate();
					wakeDriver(d.getName(), false, false);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			tryCount++;
		}
		scheduledWakeUps.clear();
		loadedDrivers.clear();

		assert driverThreads.isEmpty();
		assert loadedDrivers.isEmpty();
		Log.w(TAG, "all drivers stopped");
	}

	private static void restartDrivers() {
		stopDrivers();
		startDrivers();
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to the given package
	 * and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private static Class<org.apparatus_templi.driver.Driver>[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<org.apparatus_templi.driver.Driver>> classes = new ArrayList<Class<org.apparatus_templi.driver.Driver>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private static List<Class<org.apparatus_templi.driver.Driver>> findClasses(File directory,
			String packageName) throws ClassNotFoundException {
		List<Class<org.apparatus_templi.driver.Driver>> classes = new ArrayList<Class<org.apparatus_templi.driver.Driver>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add((Class<org.apparatus_templi.driver.Driver>) Class.forName(packageName
						+ '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * Shutdown the service after writing reason to the log.
	 * 
	 * @param reason
	 *            the reason the service must be terminated.
	 */
	static synchronized void exitWithReason(String reason) {
		Log.t(TAG, reason);
		System.exit(1);
	}

	/**
	 * Restarts a given module
	 * 
	 * @param module
	 *            the name of the module to restart. This module should be one of "main" (restart
	 *            drivers) or "web" (restart the web server).
	 */
	static synchronized void restartModule(String module) {
		// TODO the chain should be better:
		/*
		 * all ->main ->web main ->drivers ->config read ->serial interface ->message center? web
		 * ->config read? ->web server
		 */
		switch (module) {
		case "all":
			Log.d(TAG, "restarting all modules");
			restartModule("main");
			restartModule("web");
			break;
		case "main":
			// TODO this should eventually restart the serial connection and message center as well.
			Log.d(TAG, "restarting main");
			restartModule("drivers");
			break;
		case "drivers":
			Log.d(TAG, "restarting drivers");
			restartDrivers();
			break;
		case "web":
			Log.d(TAG, "restarting web server");
			try {
				restartWebServer();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				exitWithReason("Error restarting web server");
			} catch (InterruptedException e) {
				// TODO should this be a terminal error?
				e.printStackTrace();
			}
			break;
		default:
			Log.w(TAG, "could not restart " + module + ", unknown module");
		}
	}

	/**
	 * Sends the given command to a specific remote module. The message will be formatted to fit the
	 * protocol version that this module supports (if known), otherwise the message will be
	 * formatted as the most recent protocol version.
	 * 
	 * @param caller
	 *            a reference to the calling {@link Driver}
	 * @param command
	 *            the command to send to the remote module
	 */
	public static boolean sendCommand(Driver caller, String command) {
		assert loadedDrivers.contains(caller) : "driver " + caller
				+ " should exists within loadedDrivers";
		// Log.d(TAG, "sendCommand()");
		boolean messageSent = false;

		if (connectionReady && caller.getName() != null) {
			messageSent = messageCenter.sendCommand(caller.getName(), command);
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
		}

		return messageSent;
	}

	/**
	 * Sends a message to a remote module and waits waitPeriod seconds for a response.
	 * 
	 * @param caller
	 *            a reference to the calling {@link Driver}
	 * @param command
	 *            the command to send to the remote module
	 * @param waitPeriod
	 *            how many seconds to wait for a response. Maximum period to wait is 6 seconds.
	 * @return the String of data that the remote module responded with, or null if there was no
	 *         response. Note that the first incoming response is returned. If another message
	 *         addressed to this
	 */
	public static String sendCommandAndWait(Driver caller, String command, int waitPeriod) {
		// Log.d(TAG, "sendCommandAndWait()");
		String responseData = null;
		if (waitPeriod <= 6 && caller.getName() != null) {
			sendCommand(caller, command);
			long endTime = (System.currentTimeMillis() + ((1000) * waitPeriod));
			while (System.currentTimeMillis() < endTime) {
				if (messageCenter.isMessageAvailable()) {
					Message m = messageCenter.getMessage();
					if (m.getDestination().equals(caller.getName())) {
						try {
							responseData = new String(m.getData(), "UTF-8");
							break;
						} catch (UnsupportedEncodingException e) {
							Log.d(TAG,
									"sendCommandAndWait() error converting returned data to String");
							break;
						}
					} else {
						routeIncomingMessage(m);
					}
				}
			}
		}
		return responseData;
	}

	/**
	 * Sends binary data over the serial connection to a remote module. Does not yet break byte[]
	 * into chunks for transmission. Make sure that the size of the transmission is not larger than
	 * a single packet's max payload size (around 80 bytes).
	 * 
	 * @param caller
	 *            a reference to the calling {@link Driver}
	 * @param data
	 *            the binary data to send
	 */
	public static boolean sendBinary(Driver caller, byte[] data) {
		// Log.d(TAG, "sendBinary()");
		boolean messageSent = false;

		if (connectionReady && caller.getName() != null) {
			messageCenter.sendBinary(caller.getName(), data);
		} else {
			Log.w(TAG, "local arduino connection not yet ready, discarding message");
			messageSent = false;
		}

		return messageSent;
	}

	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver name as well
	 * as a data tag.
	 * 
	 * @param driverName
	 *            the name of the driver to store the data under
	 * @param dataTag
	 *            a tag to assign to this data. This tag should be specific for each data block that
	 *            your driver stores. If there already exits data for the given dataTag the old data
	 *            will be overwritten.
	 * @param data
	 *            the string of data to store
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written
	 *         successfully. 0 if the data could not be written.
	 */
	public static int storeTextData(String driverName, String dataTag, String data) {
		// Log.d(TAG, "storeTextData()");
		return SQLiteDbService.getInstance().storeTextData(driverName, dataTag, data);
	}

	/**
	 * Stores the given data to persistent storage. Data is stored based off the given driverName
	 * and dataTag.
	 * 
	 * @param driverName
	 *            the name of the driver to store the data under
	 * @param dataTag
	 *            a unique tag to assign to this data. This tag should be specific for each data
	 *            block that will be stored. If data has already been stored with the same
	 *            driverName and dataTag the old data will be overwritten.
	 * @param data
	 *            the data to be stored
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written
	 *         successfully. 0 if the data could not be written.
	 */
	public static int storeBinData(String driverName, String dataTag, byte[] data) {
		// Log.d(TAG, "storeBinData()");
		return SQLiteDbService.getInstance().storeBinData(driverName, dataTag, data);
	}

	/**
	 * Returns text data previously stored under the given module name and tag.
	 * 
	 * @param driverName
	 *            the name of the calling driver
	 * @param dataTag
	 *            the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver
	 *         name and tag.
	 */
	public static String readTextData(String driverName, String dataTag) {
		// Log.d(TAG, "readTextData()");
		return SQLiteDbService.getInstance().readTextData(driverName, dataTag);
	}

	/**
	 * Returns binary data previously stored under the given module name and tag.
	 * 
	 * @param driverName
	 *            the name of the calling driver
	 * @param dataTag
	 *            the tag to uniquely identify the data
	 * @return the stored binary data, or null if no data has been stored under the given driver
	 *         name and tag.
	 */
	public static byte[] readBinData(String driverName, String dataTag) {
		// Log.d(TAG, "readBinData()");
		return SQLiteDbService.getInstance().readBinData(driverName, dataTag);
	}

	/**
	 * Schedules a {@link Driver} to sleep for a indefinite period. The calling driver will be woken
	 * only when the system is going down or when an incoming message addressed to it is found.
	 * 
	 * @param caller
	 *            A reference to the driver to sleep
	 */
	public static void scheduleSleep(Driver caller, long threadID) {
		if (caller != null) {
			if (threadID == driverThreads.get(caller).getId()) {
				scheduledWakeUps.put(caller, (long) 0);
				Log.d(TAG, "scheduled an indefinite sleep for driver '" + caller.getName() + "'");
			} else {
				Log.w(TAG, "driver " + caller.getName() + " can only sleep on its own thread.");
			}
		}
	}

	/**
	 * Schedules a {@link org.apparatus_templi.driver.Driver} to re-create this driver at the given
	 * time. If a driver's state {@link java.lang.Tread} is
	 * {@link java.lang.Thread.State.TERMINATED} at the time of the wake up the Driver will be
	 * re-created. If the Driver's state is not TERMINATED then the driver will be woken by calling
	 * its {@link Object#notify()} method. WARNING: make sure that your driver stores any needed
	 * information before exiting its run() method.
	 * 
	 * @param caller
	 * @param wakeTime
	 * @throws InterruptedException
	 */
	public static void scheduleSleep(Driver caller, long wakeTime, long threadID) {
		if (caller != null) {
			scheduledWakeUps.put(caller, wakeTime);
			String time;
			long diff = wakeTime - System.currentTimeMillis();
			if (diff <= 0) {
				time = "now";
			} else if (diff < 1000) {
				time = String.valueOf(diff) + " milliseconds";
			} else if (diff < 60000) {
				time = String.valueOf(diff / 1000) + " seconds";
			} else {
				time = String.valueOf(diff / 60000) + " minutes";
			}
			Log.d(TAG, "scheduled a wakup for driver '" + caller.getModuleName() + "' in " + time
					+ ".");
		}
	}

	/**
	 * Pass a message to the driver specified by name.
	 * 
	 * @param destination
	 *            the unique name of the driver
	 * @param source
	 *            the source of this message, either the name of the calling driver or null. If
	 *            null, this command originated from the Coordinator
	 * @param command
	 *            the command to pass
	 */
	public static synchronized boolean passCommand(String source, String destination, String command) {
		Log.d(TAG, "passCommand() given dest:" + destination + " cmd: " + command);
		// TODO verify source name
		// TODO check for reserved name in toDriver
		boolean messagePassed = false;
		if (loadedDrivers.containsKey(destination)) {
			Driver destDriver = loadedDrivers.get(destination);
			if (driverThreads.get(destDriver).getState() != Thread.State.TERMINATED) {
				loadedDrivers.get(destination).receiveCommand(command);
				messagePassed = true;
			}
		}
		return messagePassed;
	}

	/**
	 * Requests the XML data representing a drivers status from the given driver.
	 * 
	 * @param driverName
	 *            the unique name of the driver to query
	 * @return the String representation of the XML data, or null if the driver does not exist
	 */
	public static synchronized String requestWidgetXML(String driverName) {
		// Log.d(TAG, "requestWidgetXML()");
		String xmlData = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver driver = loadedDrivers.get(driverName);
			xmlData = driver.getWidgetXML();
		}
		return xmlData;
	}

	/**
	 * Requests the XML data representing a driver's detailed controls.
	 * 
	 * @param driverName
	 *            the unique name of the driver to query
	 * @return the String representation of the XML data, or null if the driver does not exist
	 */
	public static synchronized String requestFullPageXML(String driverName) {
		Log.d(TAG, "requestFullPageXML()");
		String xmlData = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver driver = loadedDrivers.get(driverName);
			xmlData = driver.getFullPageXML();
		}
		return xmlData;
	}

	/**
	 * Checks the list of known remote modules. If the module is not present the Coordinator may
	 * re-query the remote modules for updates.
	 * 
	 * @param moduleName
	 *            the name of the remote module to check for
	 * @return true if the remote module is known to be up, false otherwise
	 */
	public static synchronized boolean isModulePresent(String moduleName) {
		return remoteModules.containsKey(moduleName);
	}

	/**
	 * Returns a list of sensors that the given driver monitors.
	 * 
	 * @param driverName
	 *            the unique name of the driver to query
	 * @return an ArrayList of Strings representing all sensors monitored by the driver. If the
	 *         given driver is not of type {@link SensorModule} or if the driver is not loaded then
	 *         returns null.
	 */
	public static synchronized ArrayList<String> getSensorList(String driverName) {
		ArrayList<String> results = null;
		if (loadedDrivers.containsKey(driverName)) {
			Driver d = loadedDrivers.get(driverName);
			if (d instanceof org.apparatus_templi.driver.SensorModule) {
				results = ((SensorModule) d).getSensorList();
			}
		}
		return results;
	}

	/**
	 * Returns a list of controllers that the given driver interacts with.
	 * 
	 * @param driverName
	 *            the unique name of the driver to query
	 * @return an ArrayList of Strings representing all controllers this driver interacts with. If
	 *         the given driver is not of type {@link ControllerModule} or if the driver is not
	 *         loaded then returns null.
	 */
	public static synchronized ArrayList<String> getControllerList(String driverName) {
		ArrayList<String> results = new ArrayList<String>();
		if (loadedDrivers.containsKey(driverName)) {
			Driver d = loadedDrivers.get(driverName);
			if (d instanceof org.apparatus_templi.driver.ControllerModule) {
				results = ((ControllerModule) d).getControllerList();
			}
		}
		return results;
	}

	/**
	 * Returns a list of all loaded drivers.
	 * 
	 * @return an ArrayList of Strings of driver names. If no drivers are loaded then returns an
	 *         empty list.
	 */
	public static synchronized ArrayList<String> getLoadedDrivers() {
		// Log.d(TAG, "getLoadedDrivers()");
		ArrayList<String> driverList = new ArrayList<String>();
		for (String driverName : loadedDrivers.keySet()) {
			// driverList.add(loadedDrivers.get(driverName).getClass().getSimpleName());
			driverList.add(driverName);
		}

		return driverList;
	}

	/**
	 * Returns a list of available drivers. This list is queried from a hard-coded directory, and
	 * may not represent drivers loaded from a different CLASSPATH.
	 * 
	 * @return an ArrayList<String> of available driver classes.
	 */
	public static synchronized ArrayList<String> getAvailableDrivers() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			for (Class<org.apparatus_templi.driver.Driver> c : findClasses(new File(
					"bin/org/apparatus_templi/driver/"), "org.apparatus_templi.driver")) {
				list.add(c.getSimpleName());
			}
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return list;
	}

	public static synchronized void wakeSelf(Driver d) {
		wakeDriver(d.getName(), false, false);
	}

	/**
	 * Receives an event generated by a driver. If any drivers have registered to listen for this
	 * type of event then a reference to this event will be passed to that Driver's
	 * {@link EventWatcher#receiveEvent(Event)} method.
	 * 
	 * @param d
	 *            the Driver that generated this Event, must not be null.
	 * @param e
	 *            the Event generated, must not be null
	 * @throws IllegalArgumentException
	 *             if the given Driver is null
	 */
	public static void receiveEvent(Driver d, Event e) throws IllegalArgumentException {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}

		if (d instanceof EventGenerator) {
			Log.d(TAG,
					"incoming event '" + e.getClass().getSimpleName() + "' from driver '"
							+ d.getName() + "'");
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' not allowed to generate events");
		}

		ArrayList<EventWatcher> list = eventWatchers.get(e.getClass());
		if (list != null) {
			for (EventWatcher dvr : list) {
				Log.d(TAG, "notifying driver " + ((Driver) dvr).getName() + " of event "
						+ e.getClass().getSimpleName() + ".");
				dvr.receiveEvent(e);
			}
		}
	}

	/**
	 * Adds the given driver to the watch list for events of type e.
	 * 
	 * @param d
	 *            The Driver that will be notified when a similar event has been received.
	 * @param e
	 *            The type of Event that this Driver wants to be notified of.
	 * @throws IllegalArgumentException
	 *             if given Driver or Event are null.
	 */
	public static void registerEventWatch(Driver d, Event e) throws IllegalArgumentException {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}

		if (d instanceof EventWatcher) {
			Log.d(TAG, " driver '" + d.getName() + "' requested to be notified of events of type '"
					+ e.getClass().getSimpleName() + "'.");
			ArrayList<EventWatcher> curList = eventWatchers.get(e.getClass());
			if (curList == null) {
				curList = new ArrayList<EventWatcher>();
			}
			curList.add((EventWatcher) d);
			eventWatchers.put(e.getClass(), curList);
		} else {
			Log.d(TAG, "driver '" + d.getName() + "' can not listen for events of type '"
					+ e.getClass().getSimpleName() + "', must implement EventWatcher.");
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Removes the given Driver from the watch list for Events of type e.
	 * 
	 * @param d
	 *            the Driver to remove from the watch list.
	 * @param e
	 *            the type of Event that this Driver no longer wants to listen for.
	 */
	public static void removeEventWatch(Driver d, Event e) throws IllegalArgumentException {
		if (d == null || e == null) {
			throw new IllegalArgumentException();
		}

		if (d instanceof EventWatcher) {
			ArrayList<EventWatcher> list = eventWatchers.get(e);
			if (list != null) {
				list.remove(d);
			} else {
				Log.d(TAG, "could not remove driver " + d.getName() + " from watch list for event "
						+ e.getClass().getSimpleName() + ", driver was not registered for watch.");
			}
		} else {
			Log.w(TAG, "could not remove driver " + d.getName() + " from watch list for event "
					+ e.getClass().getSimpleName() + ", driver is not an event watcher.");
		}
	}

	/**
	 * Accessor method to the the thread id number for a particular driver.
	 * 
	 * @param d
	 *            the driver whose thread number should be looked for
	 * @return the id of the drivers thread, or -1 if no such thread exists.
	 */
	public static long getDriverThreadId(Driver d) {
		long threadId = -1;
		if (driverThreads.containsKey(d)) {
			threadId = driverThreads.get(d).getId();
		}
		return threadId;
	}

	public static String getServerAddress() {
		StringBuilder address = new StringBuilder();
		address.append(webServer.getProtocol());
		address.append(webServer.getServerLocation());
		address.append(":");
		address.append(webServer.getPort());
		address.append("/index.html");
		Log.d(TAG, "server address: " + address.toString());
		return address.toString();
	}

	public static void main(String argv[]) throws InterruptedException, IOException {
		// disable dock icon in MacOS
		System.setProperty("apple.awt.UIElement", "true");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// start the system tray icon listener
		sysTray = new SysTray();

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Log.d(TAG, "thread: " + threadId + " current thread: " + Thread.currentThread().getId());
		// turn off debug messages
		// Log.setLogLevel(Log.LEVEL_WARN);
		Log.d(TAG, "SERVICE STARTING");
		Log.c(TAG, "Starting");
		parseCommandLineOptions(argv);

		// open the serial connection
		openSerialConnection();

		// start the message center
		messageCenter = MessageCenter.getInstance();
		messageCenter.setSerialConnection(serialConnection);

		// block until the local Arduino is ready
		System.out.print(TAG + ":" + "Waiting for local link to be ready.");
		if (!(serialConnection instanceof DummySerialConnection)) {
			byte[] sBytes = messageCenter.readBytesUntil((byte) 0x0A);
			String sString = new String(sBytes);
			if (sString.endsWith("READY")) {
				connectionReady = true;
			}

			if (!connectionReady) {
				Coordinator.exitWithReason("could not find a local Arduino connection, exiting");
			} else {
				Log.c(TAG, "Local link ready.");
			}
		} else {
			connectionReady = true;
			Log.c(TAG, "Local dummy link ready");
		}

		// query for remote modules. Since the modules may be slow in responding
		// + we will wait for a few seconds to make sure we get a complete list
		System.out.print(TAG + ":" + "Querying modules (6s wait).");
		messageCenter.beginReadingMessages();
		// begin processing incoming messages
		new Thread(messageCenter).start();

		// if we are using the dummy serial connection then there is no point in
		// waiting
		// + 6 seconds for a response from the modules
		if (!(serialConnection instanceof DummySerialConnection)) {
			queryRemoteModules();
			for (int i = 0; i < 6; i++) {
				if (messageCenter.isMessageAvailable()) {
					routeIncomingMessage(messageCenter.getMessage());
				}

				System.out.print(".");
				Thread.sleep(1000);
			}
		}
		System.out.println();

		if (remoteModules.size() > 0) {
			Log.c(TAG, "Found " + remoteModules.size() + " modules :" + remoteModules.toString());
		} else {
			Log.c(TAG, "Did not find any remote modules.");
		}

		// initialize and start all drivers
		startDrivers();

		// Add a shutdown hook so that the drivers can be notified when
		// + the system is going down.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				sysTray.setStatus(SysTray.Status.TERM);
				Thread.currentThread().setName("Shutdown Hook");
				Log.d(TAG, "system is going down. Notifying all drivers.");
				// cancel any pending driver restarts
				scheduledWakeUps.clear();
				for (String driverName : loadedDrivers.keySet()) {
					Log.d(TAG, "terminating driver '" + driverName + "'");
					loadedDrivers.get(driverName).terminate();
					Log.d(TAG, "notified driver '" + driverName + "'");
				}
				// give the drivers ~4s to finalize their termination
				long wakeTime = System.currentTimeMillis() + 4 * 1000;
				while (System.currentTimeMillis() < wakeTime) {
					// do a non-blocking sleep so that incoming message can still be routed
				}
			}
		});

		// start the web interface
		startWebServer();

		// enter main loop
		while (true) {
			// TODO keep track of the last time a message was sent to/received
			// from each remote module
			// + if the time period is too great, then re-query the remote
			// modules, possibly removing
			// + them from the known list if no response is detected

			// Check for incoming messages, only process the first byte before
			// breaking off
			// + to a more appropriate method
			if (messageCenter.isMessageAvailable()) {
				routeIncomingMessage(messageCenter.getMessage());
			}

			// Check for scheduled driver wake ups.
			for (Driver d : scheduledWakeUps.keySet()) {
				Long wakeTime = scheduledWakeUps.get(d);
				Long curTime = System.currentTimeMillis();
				if (wakeTime <= curTime && wakeTime != 0) {
					try {
						wakeDriver(d.getName());
					} catch (Exception e) {
						Log.e(TAG, "error restarting driver '" + d.getName() + "'");
						scheduledWakeUps.remove(d);
					}
				}
			}

			Thread.yield();
			Thread.sleep(100);
		}
	}
}
