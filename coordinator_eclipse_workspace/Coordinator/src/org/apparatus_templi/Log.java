package org.apparatus_templi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.EvictingQueue;


/**
 * Logging facilities for Coordinator and the drivers
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class Log {
//	private final static Logger LOGGER = Logger.getLogger(Coordinator.class.getSimpleName()); 
	static final int LEVEL_DEBUG = 2;
	static final int LEVEL_WARN  = 1;
	static final int LEVEL_ERR   = 0;
	private static int logLevel = LEVEL_DEBUG;
	//a buffer to hold the last 30 log messages.
	private static EvictingQueue<String> prevLines = EvictingQueue.create(30);
	//hold lines that could not yet be written to the log file
	private static ArrayDeque<String> bufferedLines = new ArrayDeque<String>();
	
	private static void writeLogMessage(String logMessage) {
		prevLines.add(logMessage);
		PrintWriter out = null;
		String logFile = Prefs.getInstance().getPreference(Prefs.Keys.logFile);
		if (logFile == null) {
			bufferedLines.add(logMessage);
		} else {
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
				//write any buffered lines first
				while (!bufferedLines.isEmpty()) {
					out.println(bufferedLines.poll());
				}
				out.println(logMessage);
				out.close();
			} catch (IOException e) {
				System.err.println("Log : could not write to log file: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Writes a terminal error message to both the log and to {@link System#err}.
	 * @param tag a String to identify the source of this message.
	 * @param message the terminal failure message to be logged.
	 */
	static void t(String tag, String message) {
		String logMessage = System.currentTimeMillis() + ": TERMINAL FAILURE: " + tag + ":" + message;
		System.out.println(logMessage);
		System.err.println(logMessage);
		writeLogMessage(logMessage);
	}

	/**
	 * Sets the logging level to the absolute value of newLogLevel.
	 * 	The logging level determines which log statements are recorded.
	 * 	The default value is {@link Log#LEVEL_DEBUG}, indicating that
	 *  debug messages, warnings, and errors should all be logged.
	 * @param newLogLevel the new log level. A value greater/equal to 
	 * {@link Log#LEVEL_DEBUG} will	log all messages.  A value of
	 * {@link Log#LEVEL_WARN} will log only warnings and errors.  A
	 * 	value of {@link Log#LEVEL_ERR} or less will only log errors.
	 */
	public static void setLogLevel(int newLogLevel) {
		logLevel = Math.abs(newLogLevel);
	}
	
	/**
	 * Writes a message to the console as {@link System#out}. This message
	 * is not duplicated in the log.
	 * @param tag a String to identify the source of this message.
	 * @param message the message to print to the console.
	 */
	public static void c(String tag, String message) {
		System.out.println(tag + ":" + message);
	}
	/**
	 * Writes debugging information to the log.
	 * @param tag a String to identify this source of this message.
	 * @param message the debugging message to be logged.
	 */
	public static void d(String tag, String message) {
		if (logLevel >= Log.LEVEL_DEBUG) {
			String logMessage = System.currentTimeMillis() + ": " + tag + ":" +  message;
			System.out.println(logMessage);
			writeLogMessage(logMessage);
		}
	}
	
	/**
	 * Writes a warning message to the log.
	 * @param tag a String to identify the source of this message.
	 * @param message the warning message to be logged.
	 */
	public static void w(String tag, String message) {
		if (logLevel >= Log.LEVEL_WARN) {
			String logMessage = System.currentTimeMillis() + ": Warning: " + tag + ":" +  message;
			System.out.println(logMessage);
			writeLogMessage(logMessage);
		}
	}
	
	/**
	 * Writes an error message to both the log and {@link System#err}.
	 * @param tag a String to identify the source of this message.
	 * @param message the error message to be logged.
	 */
	public static void e(String tag, String message) {
		if (logLevel >= Log.LEVEL_ERR) {
			String logMessage = System.currentTimeMillis() + ": Error: " + tag + ":" + message;
			System.err.println(logMessage);
			writeLogMessage(logMessage);
		}
	}
	
	public static ArrayList<String> getRecentLog() {
		return new ArrayList<String>(Arrays.asList(prevLines.toArray(new String[]{})));
	}
}