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
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class Log {
	// private final static Logger LOGGER = Logger.getLogger(Coordinator.class.getSimpleName());
	static final int LEVEL_DEBUG = 3;
	static final int LEVEL_WARN = 2;
	static final int LEVEL_ERR = 1;
	static final int LEVEL_TERM = 0;
	private static int logLevel = LEVEL_DEBUG;
	// a buffer to hold the last 30 log messages.
	private static EvictingQueue<String> prevLines = EvictingQueue.create(30);
	// hold lines that could not yet be written to the log file
	private static ArrayDeque<String> bufferedLines = new ArrayDeque<String>();

	private synchronized static void writeLogMessage(String logMessage) {
		prevLines.add(logMessage);
		PrintWriter out = null;
		String logFile = Coordinator.getPrefs().getPreference(Prefs.Keys.logFile);
		if (logFile == null) {
			bufferedLines.add(logMessage);
		} else {
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
				// write any buffered lines first
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
	 * 
	 * @param tag
	 *            a String to identify the source of this message.
	 * @param message
	 *            the terminal failure message to be logged.
	 */
	static void t(String tag, String message) {
		String logMessage = System.currentTimeMillis() + ": TERMINAL FAILURE: " + tag + ":"
				+ message;
		System.err.println(logMessage);
		writeLogMessage(logMessage);
	}

	/**
	 * Writes a message to the console as {@link System#out}. This message is not duplicated in the
	 * log.
	 * 
	 * @param tag
	 *            a String to identify the source of this message.
	 * @param message
	 *            the message to print to the console.
	 */
	public static void c(String tag, String message) {
		System.out.println(tag + ":" + message);
	}

	/**
	 * Writes debugging information to the log.
	 * 
	 * @param tag
	 *            a String to identify this source of this message.
	 * @param message
	 *            the debugging message to be logged.
	 */
	public static void d(String tag, String message) {
		String logMessage = System.currentTimeMillis() + ": " + tag + ":" + message;
		if (logLevel >= Log.LEVEL_DEBUG) {
			System.out.println(logMessage);
			writeLogMessage(logMessage);
		}

	}

	/**
	 * Writes a warning message to the log.
	 * 
	 * @param tag
	 *            a String to identify the source of this message.
	 * @param message
	 *            the warning message to be logged.
	 */
	public static void w(String tag, String message) {
		String logMessage = System.currentTimeMillis() + ": Warning: " + tag + ":" + message;
		if (logLevel >= Log.LEVEL_WARN) {
			System.out.println(logMessage);
			writeLogMessage(logMessage);
		}

	}

	/**
	 * Writes an error message to both the log and {@link System#err}.
	 * 
	 * @param tag
	 *            a String to identify the source of this message.
	 * @param message
	 *            the error message to be logged.
	 */
	public static void e(String tag, String message) {
		String logMessage = System.currentTimeMillis() + ": Error: " + tag + ":" + message;
		if (logLevel >= Log.LEVEL_ERR) {
			System.err.println(logMessage);
			writeLogMessage(logMessage);
		}

	}

	/**
	 * Returns a buffer of recent logging messages.
	 * 
	 * @return the last 30 lines of the log.
	 */
	public static ArrayList<String> getRecentLog() {
		return new ArrayList<String>(Arrays.asList(prevLines.toArray(new String[] {})));
	}

	/**
	 * Sets the logging level to the absolute value of newLogLevel. The logging level determines
	 * which log statements are printed to {@link System#out} or {@link System#err}. All log
	 * messages are written to the log file regardless of the logging level. The default value is
	 * {@link Log#LEVEL_DEBUG}, indicating that debug messages, warnings, errors, and terminal
	 * failures should all be printed.
	 * 
	 * @param newLogLevel
	 *            the new log level. A value greater/equal to {@link Log#LEVEL_DEBUG} will print all
	 *            messages. A value of {@link Log#LEVEL_WARN} will print only warnings, errors, and
	 *            terminal errors. A value of {@link Log#LEVEL_ERR} or less will only print errors
	 *            and terminal failures. A value of {@link Log#LEVEL_TERM} will only print terminal
	 *            failures.
	 */
	public static void setLogLevel(int newLogLevel) {
		logLevel = Math.abs(newLogLevel);
	}
}