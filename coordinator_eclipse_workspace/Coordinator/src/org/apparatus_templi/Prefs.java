package org.apparatus_templi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Maintains default preferences and reads in preferences
 * from configuration file.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Prefs {
	private static final String TAG = "Preferences";
	public static final HashMap<String, String> DEF_PREFS = new HashMap<String, String>();
	public static final HashMap<String, String> preferences = new HashMap<String, String>();
	private static Prefs instance = null;
	
	private Prefs() {
		//Singleton pattern
		DEF_PREFS.put(Prefs.Keys.configFile, "coordinator.conf");
		DEF_PREFS.put(Prefs.Keys.driverList, "");
		DEF_PREFS.put(Prefs.Keys.portNum, "8000");
		DEF_PREFS.put(Prefs.Keys.serialPort, null);
		DEF_PREFS.put(Prefs.Keys.serverBindLocalhost, "false");
		DEF_PREFS.put(Prefs.Keys.webResourceFolder, "website/");
		DEF_PREFS.put(Prefs.Keys.twtrAccess, "");
		DEF_PREFS.put(Prefs.Keys.twtrAccessKey, "");
		DEF_PREFS.put(Prefs.Keys.logFile, "coordinator.log");
	}
	
	public static Prefs getInstance() {
		if (instance == null) {
			instance = new Prefs();
		}
		return instance;
	}
	
	public synchronized void readPreferences(String configFile) {
		//Read the values from the configuration file.  If any command line
		//+ parameters were passed, they should overwrite values read, so we
		//+ will continue processing them later.	
		Log.d(TAG, "reading from config file: '" + configFile + "'");
		try {
			Properties props = new Properties();
			FileInputStream fin = new FileInputStream(configFile);
			props.load(fin);
			fin.close();
			
			preferences.put(Prefs.Keys.logFile, props.getProperty(Keys.logFile,
					DEF_PREFS.get(Keys.logFile)));
			Log.d(TAG, "read preference '" + Keys.logFile + "' as '" +
					getPreference(Keys.logFile) + "'");
			
			preferences.put(Prefs.Keys.portNum, props.getProperty(Keys.portNum,
					DEF_PREFS.get(Keys.portNum)));
			Log.d(TAG, "read preference '" + Keys.portNum + "' as '" +
					getPreference(Keys.portNum) + "'");
			
			preferences.put(Prefs.Keys.serialPort, props.getProperty(Keys.serialPort,
					DEF_PREFS.get(Keys.serialPort)));
			Log.d(TAG, "read preference '" + Keys.serialPort + "' as '" +
					getPreference(Keys.serialPort) + "'");
			
			preferences.put(Prefs.Keys.webResourceFolder, props.getProperty(Keys.webResourceFolder,
					DEF_PREFS.get(Keys.webResourceFolder)));
			Log.d(TAG, "read preference '" + Keys.webResourceFolder + "' as '" +
					getPreference(Keys.webResourceFolder) + "'");
			
			preferences.put(Prefs.Keys.driverList, props.getProperty(Keys.driverList,
					DEF_PREFS.get(Keys.driverList)));
			Log.d(TAG, "read preference '" + Keys.driverList + "' as '" +
					getPreference(Keys.driverList) + "'");
			
			preferences.put(Prefs.Keys.serverBindLocalhost, props.getProperty(Keys.serverBindLocalhost,
					DEF_PREFS.get(Keys.serverBindLocalhost)));
			Log.d(TAG, "read preference '" + Keys.serverBindLocalhost + "' as '" +
					getPreference(Keys.serverBindLocalhost) + "'");
			
			preferences.put(Keys.twtrAccess, props.getProperty(Keys.twtrAccess,
					DEF_PREFS.get(Keys.twtrAccess)));
			Log.d(TAG, "read preference '" + Keys.twtrAccess + "' as '" +
					getPreference(Keys.twtrAccess) + "'");
			
			preferences.put(Keys.twtrAccessKey, props.getProperty(Keys.twtrAccessKey,
					DEF_PREFS.get(Keys.twtrAccessKey)));
			Log.d(TAG, "read preference '" + Keys.twtrAccessKey + "' as '" +
					getPreference(Keys.twtrAccessKey) + "'");
		} catch (IOException | NullPointerException e) {
			Log.w(TAG, "unable to read configuration file '" + configFile + "'");
			
		}
	}
	
	public synchronized String getPreference(String key) {
		String value = null;
		if (preferences.containsKey(key)) {
			value = preferences.get(key);
		}
		return value;
	}
	
	public synchronized void putPreference(String key, String value) {
		preferences.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized HashMap<String, String> getPreferencesMap() {
		//make sure that the caller can not modify the preferences we hold
		return (HashMap<String, String>)preferences.clone();
	}
	
	/**
	 * Writes the given preferences back to the config file.
	 * @param prefs
	 */
	public synchronized boolean savePreferences(HashMap<String, String> prefs) {
		//only save the preferences if the config file is not the default
		boolean fileUpdated = false;
		String newPrefsFile = prefs.get(Keys.configFile);
		String defPrefsFile = DEF_PREFS.get(Keys.configFile);
		
		if (!defPrefsFile.equals(newPrefsFile)) {
			HashMap<String, String> newPrefs = new HashMap<String, String>();
			newPrefs.putAll(DEF_PREFS);		//store the default preferences
			newPrefs.putAll(prefs);			//store the updated preferences
			String configFile = newPrefs.remove(Keys.configFile);
			for (String key: newPrefs.keySet()) {
				Log.d(TAG, "savePreferences key " + key + " = " + newPrefs.get(key));
			}
			
			Properties props = new Properties();
			props.putAll(newPrefs);
			try {
				FileOutputStream fout = new FileOutputStream(configFile);
				props.store(fout, "");
				fout.close();
				Log.d(TAG, "wrote preferences to file: '" + configFile);
				readPreferences(configFile);
				fileUpdated = true;
			} catch (IOException e) {
				Log.e(TAG, "could not save preferences to file: '" + newPrefs.get(Keys.configFile) + "'");
			}
		} else {
			Log.e(TAG, "can not overwrite default config file");
		}
		return fileUpdated;		
	}
	
	public class Keys {
		public static final String portNum = "portNum";
		public static final String configFile = "configFile";
		public static final String serialPort = "serialPort";
		public static final String webResourceFolder = "webResourceFolder";
		public static final String driverList = "driverList";
		public static final String serverBindLocalhost = "serverBindLocalhost";
		public static final String twtrAccess = "twtr_access";
		public static final String twtrAccessKey = "twtr_access_key";
		public static final String logFile = "logFile";
	}
}
