package org.apparatus_templi;


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
			System.out.println(System.currentTimeMillis() + ": " + tag + ":" +  message);
//			LOGGER.setLevel(Level.ALL);
//			LOGGER.info(System.currentTimeMillis() + ": " + tag + ":" +  message);
		}
	}
	
	/**
	 * Writes a warning message to the log.
	 * @param tag a String to identify the source of this message.
	 * @param message the warning message to be logged.
	 */
	public static void w(String tag, String message) {
		if (logLevel >= Log.LEVEL_WARN) {	
			System.out.println(System.currentTimeMillis() + ": Warning: " + tag + ":" +  message);
	//		LOGGER.setLevel(Level.ALL);
	//		LOGGER.warning(System.currentTimeMillis() + ": " + tag + ":" +  message);
		}
	}
	
	/**
	 * Writes an error message to both the log and {@link System#err}.
	 * @param tag a String to identify the source this message.
	 * @param message the error message to be logged.
	 */
	public static void e(String tag, String message) {
		if (logLevel >= Log.LEVEL_ERR) {
			System.err.println(System.currentTimeMillis() + ": Error: " + tag + ":" + message);
//			LOGGER.setLevel(Level.ALL);
//			LOGGER.severe(System.currentTimeMillis() + ": " + tag + ":" +  message);
		}
	}
}