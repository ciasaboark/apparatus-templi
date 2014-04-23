package org.apparatus_templi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
	public final HashMap<String, String> DEF_PREFS = new HashMap<String, String>();
	// contains a long form description of the preference
	private final HashMap<String, String> PREF_DESC = new HashMap<String, String>();
	// contains a short descriptive name of the preferences
	private final HashMap<String, String> PREF_NAME = new HashMap<String, String>();

	public static final HashMap<String, String> preferences = new HashMap<String, String>();

	public Prefs() {
		DEF_PREFS.put(Keys.configFile, "coordinator.conf");
		PREF_NAME.put(Keys.configFile, "Configuration file");
		PREF_DESC.put(Keys.configFile, "The configuration file to read preferences from. "
				+ "If a preference is not specified in this file then the default value will "
				+ "be used");

		DEF_PREFS.put(Keys.emailList, "");
		PREF_NAME.put(Keys.emailList, "Notification List");
		PREF_DESC.put(Keys.emailList, "A comma separated list of email address that should "
				+ "be notified when the server starts, stops, or when the web server restarts");

		DEF_PREFS.put(Keys.driverList, "");
		PREF_NAME.put(Keys.driverList, "Drivers");
		PREF_DESC.put(Keys.driverList, "A comma seperated list of drivers to load. "
				+ "The drivers should be referenced by short class name.");

		DEF_PREFS.put(Keys.portNum, null);
		PREF_NAME.put(Keys.portNum, "Port number");
		PREF_DESC.put(Keys.portNum, "The port number that the web server will listen "
				+ "on.  If this value is empty then the server will attempt to automatically "
				+ "find an empty port");

		DEF_PREFS.put(Keys.encryptServer, "false");
		PREF_NAME.put(Keys.encryptServer, "Enable Encryption");
		PREF_DESC.put(Keys.encryptServer, "If set to true then traffic to the web server will "
				+ "be encrypted.  After enabling this option you may be prompted to accept "
				+ "unsigned credentials.");

		DEF_PREFS.put(Keys.serialPort, null);
		PREF_NAME.put(Keys.serialPort, "Serial port");
		PREF_DESC.put(Keys.serialPort, "The port name of the serial port the controller "
				+ "Arduino is connected to.  On a Windows system this will likely be COM3 or "
				+ "COM4.  For a Mac this should be something like /dev/tty.usbmodemXXXX, where "
				+ "XXXX is the specific serial device. On a Linux system this should be something "
				+ "like /dev/tty.ACMn, where n is the specific serial number.  If the serial port "
				+ "is null, or left blank, then the Coordinator will attempt to find the correct "
				+ "serial port.  A value of \"dummy\" indicates that the Coordinator should use "
				+ "the dummy serial connection, which does not require any hardware.");

		DEF_PREFS.put(Keys.serverBindLocalhost, "false");
		PREF_NAME.put(Keys.serverBindLocalhost, "Allow outside connections");
		PREF_DESC.put(Keys.serverBindLocalhost, "If this value is \"true\" then the web server "
				+ "will attempt to bind to the computers public IP address and the server will be "
				+ "accessable to any computer on the same network.  If this value is anything "
				+ "else then the web server will only bind to the loopback address, and will "
				+ "only be accessable from the computer running the service.");

		DEF_PREFS.put(Keys.webResourceFolder, "website/");
		PREF_NAME.put(Keys.webResourceFolder, "Resources folder");
		PREF_DESC.put(Keys.webResourceFolder, "The folder that contains the resources for the "
				+ "web frontend.  This folder should follow the same directory structure and file "
				+ "naming conventions as the website/ folder.");

		DEF_PREFS.put(Keys.twtrAccess, "");
		PREF_NAME.put(Keys.twtrAccess, "Twitter access");
		PREF_DESC.put(Keys.twtrAccess, "The public key of the Twitter user that the Twitter "
				+ "service will use");

		DEF_PREFS.put(Keys.twtrAccessKey, "");
		PREF_NAME.put(Keys.twtrAccessKey, "Twitter access key");
		PREF_DESC.put(Keys.twtrAccessKey, "The secret key of the Twitter user that the Twitter "
				+ "service will use");

		DEF_PREFS.put(Keys.emailServer, "");
		PREF_NAME.put(Keys.emailServer, "Server");
		PREF_DESC.put(Keys.emailServer, "The host/server name of the email account that the Email "
				+ "service will use");

		DEF_PREFS.put(Keys.emailPort, "");
		PREF_NAME.put(Keys.emailPort, "Port number");
		PREF_DESC.put(Keys.emailPort, "The port number of the email account that the Email "
				+ "service will use");

		DEF_PREFS.put(Keys.emailUsername, "");
		PREF_NAME.put(Keys.emailUsername, "Username");
		PREF_DESC.put(Keys.emailUsername, "The username of the email account that the Email "
				+ "service will use");

		DEF_PREFS.put(Keys.emailAddress, "");
		PREF_NAME.put(Keys.emailAddress, "Email address");
		PREF_DESC.put(Keys.emailAddress, "The address of the email account that the Email "
				+ "service will use");

		DEF_PREFS.put(Keys.emailPassword, "");
		PREF_NAME.put(Keys.emailPassword, "Password");
		PREF_DESC.put(Keys.emailPassword, "The password name of the email account that the Email "
				+ "service will use");

		DEF_PREFS.put(Keys.logFile, "coordinator.log");
		PREF_NAME.put(Keys.logFile, "Log file");
		PREF_DESC.put(Keys.logFile, "The log file that all debugging messages, warnings, errors, "
				+ "and terminal failures will be written to.");

		DEF_PREFS.put(Keys.autoIncPort, "true");
		PREF_NAME.put(Keys.autoIncPort, "Auto increment port");
		PREF_DESC.put(Keys.autoIncPort,
				"If true then the web server will attempt to bind to the first available "
						+ "port starting with the default port.  This setting has no effect if a "
						+ "port number is specified in the config file or on the command line."
						+ DEF_PREFS.get(Keys.portNum));

		DEF_PREFS.put(Keys.userName, "");
		PREF_NAME.put(Keys.userName, "User name");
		PREF_DESC
				.put(Keys.userName,
						"The username required to access the web site.  If left blank then no username/password will be required");

		DEF_PREFS.put(Keys.userPass, "");
		PREF_NAME.put(Keys.userPass, "Password");
		PREF_DESC
				.put(Keys.userPass,
						"The password required to access the web site.  If left blank then no username/password will be required");
	}

	/**
	 * Return a reference to the Prefs singleton.
	 */
	// public static Prefs getInstance() {
	// if (instance == null) {
	// instance = new Prefs();
	// }
	// return instance;
	// }

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
			preferences.putAll(DEF_PREFS);
			preferences.put(Keys.configFile, configFile);

			preferences.put(Keys.logFile,
					props.getProperty(Keys.logFile, DEF_PREFS.get(Keys.logFile)));

			// TODO remove from dist, this places passwords etc in the log file
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

			preferences.put(Keys.encryptServer,
					props.getProperty(Keys.encryptServer, DEF_PREFS.get(Keys.encryptServer)));
			Log.d(TAG, "read preferences '" + Keys.encryptServer + "' as '"
					+ getPreference(Keys.encryptServer) + "'");

			preferences.put(Keys.emailList,
					props.getProperty(Keys.emailList, DEF_PREFS.get(Keys.emailList)));
			Log.d(TAG, "read preferences '" + Keys.emailList + "' as '"
					+ getPreference(Keys.emailList) + "'");

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

			preferences.put(Keys.emailServer,
					props.getProperty(Keys.emailServer, DEF_PREFS.get(Keys.emailServer)));
			Log.d(TAG, "read preference '" + Keys.emailServer + "' as '"
					+ getPreference(Keys.emailServer) + "'");

			preferences.put(Keys.emailPort,
					props.getProperty(Keys.emailPort, DEF_PREFS.get(Keys.emailPort)));
			Log.d(TAG, "read preference '" + Keys.emailPort + "' as '"
					+ getPreference(Keys.emailPort) + "'");

			preferences.put(Keys.emailUsername,
					props.getProperty(Keys.emailUsername, DEF_PREFS.get(Keys.emailUsername)));
			Log.d(TAG, "read preference '" + Keys.emailUsername + "' as '"
					+ getPreference(Keys.emailUsername) + "'");

			preferences.put(Keys.emailAddress,
					props.getProperty(Keys.emailAddress, DEF_PREFS.get(Keys.emailAddress)));
			Log.d(TAG, "read preference '" + Keys.emailAddress + "' as '"
					+ getPreference(Keys.emailAddress) + "'");

			preferences.put(Keys.emailPassword,
					props.getProperty(Keys.emailPassword, DEF_PREFS.get(Keys.emailPassword)));
			Log.d(TAG, "read preference '" + Keys.emailPassword + "' as '"
					+ getPreference(Keys.emailPassword) + "'");

			// read username and password from the database (if one exists)
			String username = Coordinator.readTextData("SYSTEM", "USERNAME");
			String password = Coordinator.readTextData("SYSTEM", "PASSWORD");
			if (username != null) {
				preferences.put(Keys.userName, username);
			}
			// we don't really want to store the pass hash here
			preferences.put(Keys.userPass, "");

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
		if (PREF_DESC.containsKey(key)) {
			value = PREF_DESC.get(key);
		}
		return value;
	}

	public synchronized String getPreferenceName(String key) {
		String value = null;
		if (PREF_NAME.containsKey(key)) {
			value = PREF_NAME.get(key);
		}
		return value;
	}

	public synchronized String getDefPreferences(String key) {
		String value = null;
		if (DEF_PREFS.containsKey(key)) {
			value = DEF_PREFS.get(key);
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
		File newPrefsFileName = new File(newPrefsFile);
		File defPrefsFileName = new File(defPrefsFile);

		// TODO checking for file conflicts should be removed during dist (default config is saved
		// inside the jar and can not be overwritten)
		boolean sameFiles = true;
		try {
			sameFiles = Files.isSameFile(newPrefsFileName.toPath(), defPrefsFileName.toPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (!sameFiles) {
			HashMap<String, String> newPrefs = new HashMap<String, String>();
			newPrefs.putAll(DEF_PREFS); // store the default preferences
			// remove the default user/pass so they aren't stored to the DB
			newPrefs.remove(Keys.userPass);
			newPrefs.remove(Keys.userName);

			// insert the user preferences
			newPrefs.putAll(prefs); // store the updated preferences

			// store the user/pass to the database then remove it from the map so they are not
			// written to the config file
			if (newPrefs.containsKey(Keys.userName)) {
				Coordinator.storeTextData("SYSTEM", "USERNAME", newPrefs.get(Keys.userName));
				newPrefs.remove(Keys.userName);
			}

			if (newPrefs.containsKey(Keys.userPass)) {
				String password = newPrefs.get(Keys.userPass);
				newPrefs.remove(Keys.userPass);
				String hash;
				try {
					hash = org.apparatus_templi.web.PasswordHash.createHash(password);
					Coordinator.storeTextData("SYSTEM", "PASSWORD", hash);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					Log.e(TAG, "could not create has of password");
				}
			}

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
		// Main section properties
		public static final String configFile = "configFile";
		public static final String serialPort = "serialPort";
		public static final String driverList = "driverList";
		public static final String logFile = "logFile";
		public static final String emailList = "emailList";

		// Web server properties
		public static final String portNum = "web.portNum";
		public static final String webResourceFolder = "web.resourceFolder";
		public static final String encryptServer = "web.encryptServer";
		public static final String userName = "web.userName";
		public static final String userPass = "web.userPass";
		@Deprecated
		// this will be phased out in favor of specifying the host name to use
		public static final String serverBindLocalhost = "web.bindLocalhost";
		@Deprecated
		public static final String autoIncPort = "web.autoIncPort";

		// Twitter service properties
		public static final String twtrAccess = "twtr.access";
		public static final String twtrAccessKey = "twtr.access_key";

		// Email service properties
		public static final String emailServer = "email.server";
		public static final String emailPort = "email.port";
		public static final String emailUsername = "email.username";
		public static final String emailAddress = "email.address";
		public static final String emailPassword = "email.password";

	}
}
