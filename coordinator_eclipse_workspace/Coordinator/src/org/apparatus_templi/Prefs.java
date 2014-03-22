package org.apparatus_templi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * A singleton pattern that maintains a map of default preferences. Contains methods to read
 * preferences from a {@link java.util.Properties}, to set preferences in memory, and to write
 * preferences back to disk. from configuration file.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class Prefs {
	private static final String TAG = "Preferences";
	// contains the default preferences
	public static final HashMap<String, String> DEF_PREFS = new HashMap<String, String>();
	// contains a long form description of the preference
	private static final HashMap<String, String> PREF_DESC = new HashMap<String, String>();
	public static final HashMap<String, String> preferences = new HashMap<String, String>();
	private static Prefs instance = null;

	private Prefs() {
		// Singleton pattern
		DEF_PREFS.put(Keys.configFile, "coordinator.conf");
		PREF_DESC.put(Keys.configFile, "The configuration file to read preferences from. "
				+ "If a preference is not specified in this file then the default value will"
				+ " be used");
		DEF_PREFS.put(Keys.driverList, "");
		PREF_DESC.put(Keys.driverList, "A comma seperated list of drivers to load."
				+ "The drivers should be referenced by short class name.");
		DEF_PREFS.put(Keys.portNum, "8000");
		PREF_DESC.put(Keys.portNum, "The port number that the web server will listen "
				+ "on.  If this value is empty then the server will attempt to automatically "
				+ "find an empty port");
		DEF_PREFS.put(Keys.serialPort, null);
		PREF_DESC.put(Keys.serialPort, "The port name of the serial port the controller "
				+ "Arduino is connected to.  On a Windows system this will likely be COM3 or "
				+ "COM4.  For a Mac this should be something like /dev/tty.usbmodemXXXX, where "
				+ "XXXX is the specific serial device. On a Linux system this should be something "
				+ "like /dev/tty.ACMn, where n is the specific serial number.  If the serial port "
				+ "is null, or left blank, then the Coordinator will attempt to find the correct "
				+ "serial port.  A value of \"dummy\" indicates that the Coordinator should use "
				+ "the dummy serial connection, which does not require any hardware.");
		DEF_PREFS.put(Keys.serverBindLocalhost, "false");
		PREF_DESC.put(Keys.serverBindLocalhost, "If this value is \"true\" then the web server"
				+ "will attempt to bind to the computers public IP address and the server will be"
				+ "accessable to any computer on the same network.  If this value is anything "
				+ "else then the web server will only bind to the loopback address, and will "
				+ "only be accessable from the computer running the service.");
		DEF_PREFS.put(Keys.webResourceFolder, "website/");
		PREF_DESC.put(Keys.webResourceFolder, "The folder that contains the resources for the "
				+ "web frontend.  This folder should follow the same directory structure and file "
				+ "naming conventions as the website/ folder.");
		DEF_PREFS.put(Keys.twtrAccess, "");
		PREF_DESC.put(Keys.twtrAccess, "The public key of the Twitter user that the Twitter "
				+ "service will use");
		DEF_PREFS.put(Keys.twtrAccessKey, "");
		PREF_DESC.put(Keys.twtrAccessKey, "The secret key of the Twitter user that the Twitter "
				+ "service will use");
		DEF_PREFS.put(Keys.logFile, "coordinator.log");
		PREF_DESC.put(Keys.logFile, "The log file that all debugging messages, warnings, errors, "
				+ "and terminal failures will be written to.");
		DEF_PREFS.put(Keys.autoIncPort, "true");
		PREF_DESC.put(Keys.autoIncPort,
				"If true then the web server will attempt to bind to the first available "
						+ "port starting with the default port.  This setting has no effect if a "
						+ "port number is specified in the config file or on the command line."
						+ DEF_PREFS.get(Keys.portNum));
	}

	/**
	 * Return a reference to the Prefs singleton.
	 */
	public static Prefs getInstance() {
		if (instance == null) {
			instance = new Prefs();
		}
		return instance;
	}

	/**
	 * Reads all preferences from the given configFile. If the given file can not be read then the
	 * default preferences are used.
	 * 
	 * @param configFile
	 */
	public synchronized void readPreferences(String configFile) {
		// TODO use the default preferences if an IOException is thrown

		// Read the values from the configuration file. If any command line
		// + parameters were passed, they should overwrite values read, so we
		// + will continue processing them later.
		Log.d(TAG, "reading from config file: '" + configFile + "'");
		try {
			Properties props = new Properties();
			FileInputStream fin = new FileInputStream(configFile);
			props.load(fin);
			fin.close();

			preferences.put(Keys.logFile,
					props.getProperty(Keys.logFile, DEF_PREFS.get(Keys.logFile)));
			Log.d(TAG, "read preference '" + Keys.logFile + "' as '" + getPreference(Keys.logFile)
					+ "'");

			// if no port number was specified then we will set the port number to the default port
			// and auto increment if needed when creating the server socket.
			if (props.getProperty(Keys.portNum) == null) {
				preferences.put(Keys.autoIncPort, "true");
			} else {
				preferences.put(Keys.autoIncPort, "false");
			}

			preferences.put(Keys.portNum,
					props.getProperty(Keys.portNum, DEF_PREFS.get(Keys.portNum)));
			Log.d(TAG, "read preference '" + Keys.portNum + "' as '" + getPreference(Keys.portNum)
					+ "'");

			// setting the serial port to null from the web interface is not possible, the best we
			// can do is set it to a blank value or the word 'null'. These values should be
			// transcribed back to a null value.
			String sp = props.getProperty(Keys.serialPort, DEF_PREFS.get(Keys.serialPort));
			if (sp == null || sp.equals("") || sp.equals("null")) {
				sp = null;
			}
			preferences.put(Keys.serialPort, sp);
			Log.d(TAG, "read preference '" + Keys.serialPort + "' as '"
					+ getPreference(Keys.serialPort) + "'");

			preferences.put(Keys.webResourceFolder, props.getProperty(Keys.webResourceFolder,
					DEF_PREFS.get(Keys.webResourceFolder)));
			Log.d(TAG, "read preference '" + Keys.webResourceFolder + "' as '"
					+ getPreference(Keys.webResourceFolder) + "'");

			preferences.put(Keys.driverList,
					props.getProperty(Keys.driverList, DEF_PREFS.get(Keys.driverList)));
			Log.d(TAG, "read preference '" + Keys.driverList + "' as '"
					+ getPreference(Keys.driverList) + "'");

			preferences.put(
					Keys.serverBindLocalhost,
					props.getProperty(Keys.serverBindLocalhost,
							DEF_PREFS.get(Keys.serverBindLocalhost)));
			Log.d(TAG, "read preference '" + Keys.serverBindLocalhost + "' as '"
					+ getPreference(Keys.serverBindLocalhost) + "'");

			preferences.put(Keys.twtrAccess,
					props.getProperty(Keys.twtrAccess, DEF_PREFS.get(Keys.twtrAccess)));
			Log.d(TAG, "read preference '" + Keys.twtrAccess + "' as '"
					+ getPreference(Keys.twtrAccess) + "'");

			preferences.put(Keys.twtrAccessKey,
					props.getProperty(Keys.twtrAccessKey, DEF_PREFS.get(Keys.twtrAccessKey)));
			Log.d(TAG, "read preference '" + Keys.twtrAccessKey + "' as '"
					+ getPreference(Keys.twtrAccessKey) + "'");
		} catch (IOException | NullPointerException e) {
			Log.w(TAG, "unable to read configuration file '" + configFile + "'");

		}
	}

	/**
	 * Retuns the preferences associated with the given key.
	 * 
	 * @param key
	 *            the name of the preference to return. A set of keys is available for use in the
	 *            {@link Keys} class.
	 */
	public synchronized String getPreference(String key) {
		String value = null;
		if (preferences.containsKey(key)) {
			value = preferences.get(key);
		}
		return value;
	}

	public synchronized String getPreferenceDesc(String key) {
		String value = null;
		if (preferences.containsKey(key)) {
			value = PREF_DESC.get(key);
		}
		return value;
	}

	/**
	 * Update the value of a preference. Updating the preference value will only update the
	 * preference resident in memory. To store the preference permanently
	 * {@link Prefs#savePreferences(HashMap)} must be used.
	 * 
	 * @param key
	 *            the name of the preference to update. A set of keys is available for use in the
	 *            {@link Keys} class.
	 * @param value
	 *            the value of the preference.
	 */
	public synchronized void putPreference(String key, String value) {
		preferences.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public synchronized HashMap<String, String> getPreferencesMap() {
		// make sure that the caller can not modify the preferences we hold
		return (HashMap<String, String>) preferences.clone();
	}

	/**
	 * Writes the given preferences back to the config file. If any preference is undefined in the
	 * given map then the default preference will be written. Keys that do not map to a known value
	 * in {@link Keys} will also be written to the file.
	 * 
	 * @param prefs
	 *            a {@link java.util.HashMap} of key/value pairs to write back to the preferences
	 *            file.
	 */
	public synchronized boolean savePreferences(HashMap<String, String> prefs) {
		// only save the preferences if the config file is not the default
		boolean fileUpdated = false;
		String newPrefsFile = prefs.get(Keys.configFile);
		String defPrefsFile = DEF_PREFS.get(Keys.configFile);

		if (!defPrefsFile.equals(newPrefsFile)) {
			HashMap<String, String> newPrefs = new HashMap<String, String>();
			newPrefs.putAll(DEF_PREFS); // store the default preferences
			newPrefs.putAll(prefs); // store the updated preferences
			String configFile = newPrefs.remove(Keys.configFile);
			// for (String key : newPrefs.keySet()) {
			// Log.d(TAG, "savePreferences key " + key + " = " + newPrefs.get(key));
			// }

			Properties props = new Properties();
			// if the value of a key is null then it should not appear in the saved config file
			for (String key : newPrefs.keySet()) {
				if (newPrefs.get(key) != null) {
					props.setProperty(key, newPrefs.get(key));
				}
			}
			try {
				FileOutputStream fout = new FileOutputStream(configFile);
				props.store(fout, "");
				fout.close();
				Log.d(TAG, "wrote preferences to file: '" + configFile);
				readPreferences(configFile);
				fileUpdated = true;
			} catch (IOException e) {
				Log.e(TAG, "could not save preferences to file: '" + newPrefs.get(Keys.configFile)
						+ "'");
			}
		} else {
			Log.e(TAG, "can not overwrite default config file");
		}
		return fileUpdated;
	}

	/**
	 * A convenience class containing known keys that correspond to preference values.
	 */
	public static class Keys {
		public static final String portNum = "portNum";
		public static final String configFile = "configFile";
		public static final String serialPort = "serialPort";
		public static final String webResourceFolder = "webResourceFolder";
		public static final String driverList = "driverList";
		public static final String serverBindLocalhost = "serverBindLocalhost";
		public static final String twtrAccess = "twtr_access";
		public static final String twtrAccessKey = "twtr_access_key";
		public static final String logFile = "logFile";
		public static final String autoIncPort = "autoIncPort";
	}
}
