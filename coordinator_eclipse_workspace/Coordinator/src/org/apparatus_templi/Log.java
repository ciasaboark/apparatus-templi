package org.apparatus_templi;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging facilities for Coordinator and the drivers
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Log {
//	private final static Logger LOGGER = Logger.getLogger(Coordinator.class.getSimpleName()); 
	
	/**
	 * write a message to the console.
	 * Write a message to the console <STDOUT>. This message is not duplicated
	 * 	in the log.
	 * @param tag a String to identify the source of this message
	 * @param message the message to print to the console
	 */
	public static void c(String tag, String message) {
		System.out.println(tag + ":" + message);
	}
	/**
	 * write debugging information to the log
	 * @param tag a String to identify this source of this message
	 * @param message the debugging message to be logged
	 */
	public static void d(String tag, String message) {
		System.out.println(System.currentTimeMillis() + ": " + tag + ":" +  message);
//		LOGGER.setLevel(Level.ALL);
//		LOGGER.info(System.currentTimeMillis() + ": " + tag + ":" +  message);
	}
	
	/**
	 * write warning message to the log
	 * @param tag a String to identify the source of this message
	 * @param message the warning message to be logged
	 */
	public static void w(String tag, String message) {
		System.out.println(System.currentTimeMillis() + ": Warning: " + tag + ":" +  message);
//		LOGGER.setLevel(Level.ALL);
//		LOGGER.warning(System.currentTimeMillis() + ": " + tag + ":" +  message);
	}
	
	/**
	 * write error message to the log
	 * 	The error message will be written to both the log and <STDERR>
	 * @param tag a String to identify the source this message
	 * @param message the error message to be logged
	 */
	public static void e(String tag, String message) {
		System.err.println(System.currentTimeMillis() + ": Error: " + tag + ":" + message);
//		LOGGER.setLevel(Level.ALL);
//		LOGGER.severe(System.currentTimeMillis() + ": " + tag + ":" +  message);
	}
}