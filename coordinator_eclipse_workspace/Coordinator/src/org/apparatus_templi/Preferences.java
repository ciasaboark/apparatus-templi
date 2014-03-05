package org.apparatus_templi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Maintains default preferences and reads in preferences
 * from configuration file.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Preferences {
	private static final String TAG = "Preferences";
	public static final String SERIAL_PORT = null;
	public static final String WEB_RESOURCES = "./website/";
	public static final int SERVER_PORT = 8000;
	public static final String CONFIG_FILE = "./coordinator.conf";
	public static final String DRIVER_LIST = "";
	public static final String SERVER_BIND_LOCAL = "false";
	
	public static final HashMap<String, String> preferences = new HashMap<String, String>();
	private static Preferences instance = null;
	
	private Preferences() {
		//Singleton pattern
	}
	
	public static Preferences getInstance() {
		if (instance == null) {
			instance = new Preferences();
		}
		return instance;
	}
	
	public  void readPreferences(String configFile) {
		//Read the values from the configuration file.  If any command line
		//+ parameters were passed, they should overwrite values read, so we
		//+ will continue processing them later.
//		String portNum = null;
//		String serialPort;
//		String webResourceFolder;
//		String driverList;
//		String serverBindLocalhost;
		
		try {
			Properties props = new Properties();
			FileInputStream fin = new FileInputStream(configFile);
			props.load(fin);
			fin.close();
			
			//read the port number and serial name from the configuration file
			try {
				Log.d(TAG, "read config file property '" + values.portNum + "' as '" + props.getProperty("server_port", String.valueOf(SERVER_PORT)) + "'");
				String portNum = props.getProperty("server_port", String.valueOf(SERVER_PORT));
				preferences.put(Preferences.values.portNum, String.valueOf(portNum));
				//We will be saving the port number as a string, but we can go
				//+ ahead and check it for validity now, using the default
				//+ if it isn't a valid integer.
				Integer.parseInt(portNum);
			} catch (Exception e) {
				Log.w(TAG,  "error reading port number from configuration file, setting to default");
				preferences.put(Preferences.values.portNum, String.valueOf(SERVER_PORT));
			}
			
			preferences.put(Preferences.values.serialPort, props.getProperty("serial", SERIAL_PORT));
			Log.d(TAG, "read preferences '" + values.serialPort + "' as '" + getPreference(values.serialPort) + "'");
			
			preferences.put(Preferences.values.webResourceFolder, props.getProperty("web_resources", WEB_RESOURCES));
			Log.d(TAG, "read preferences '" + values.webResourceFolder + "' as '" + getPreference(values.webResourceFolder) + "'");
			
			preferences.put(Preferences.values.driverList, props.getProperty("drivers", DRIVER_LIST));
			Log.d(TAG, "read preferences '" + values.driverList + "' as '" + getPreference(values.driverList) + "'");
			
			preferences.put(Preferences.values.serverBindLocalhost, props.getProperty("server_bind_local", SERVER_BIND_LOCAL));
			Log.d(TAG, "read preferences '" + values.webResourceFolder + "' as '" + getPreference(values.serverBindLocalhost) + "'");
		} catch (IOException | NullPointerException e) {
			Log.w(TAG, "unable to read configuration file '" + configFile + "'");
			
		}
	}
	
	public String getPreference(String key) {
		String value = null;
		if (preferences.containsKey(key)) {
			value = preferences.get(key);
		}
		return value;
	}
	
	public void putPreference(String key, String value) {
		preferences.put(key, value);
	}
	
	public class values {
		public static final String portNum = "portNum";
		public static final String configFile = "configFile";
		public static final String serialPort = "serialPort";
		public static final String webResourceFolder = "webResourceFolder";
		public static final String driverList = "driverList";
		public static final String serverBindLocalhost = "serverBindLocalhost";
	}
}
