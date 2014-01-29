package org.apparatus_templi;

/**
 * Logging facilities for Coordinator and the drivers
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Log {
	/**
	 * write debugging information to the log
	 * @param tag a String to identify this message
	 * @param message the debugging message to be logged
	 */
	public static void d(String tag, String message) {
		System.out.println(System.currentTimeMillis() + ": " + tag + ":" +  message);
	}
	
	/**
	 * write warning message to the log
	 * @param tag a String to identify this message
	 * @param message the warning message to be logged
	 */
	public static void w(String tag, String message) {
		System.out.println(System.currentTimeMillis() + ": Warning: " + tag + ":" +  message);
	}
	
	/**
	 * write error message to the log
	 * @param tag a String to identify this message
	 * @param message the error message to be logged
	 */
	public static void e(String tag, String message) {
		System.err.println(System.currentTimeMillis() + ": Error: " + tag + ":" + message);
	}
}