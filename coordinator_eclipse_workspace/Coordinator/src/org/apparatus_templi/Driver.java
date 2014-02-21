package org.apparatus_templi;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An abstract class for a basic driver.  This driver should
 * not be sub-classed directly.  Each driver should subclass
 * either SensorModule or ControllerModule.  Drivers directly
 * sub-classing Driver might not be loaded. for execution.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public abstract class Driver implements Runnable {
	/**
	 * Used to determine which type of Driver each driver
	 * should be cast to. 
	 */
	static final String TYPE = "Driver";
	
	/**
	 * The unique name of this driver.  This name should match
	 * the name of the remote module it corresponds with, and
	 * must be 10 characters or less.
	 */
	protected String name = null;
	
	/**
	 * Indicates whether or not this driver should continue running,
	 * or begin its termination process. If the driver executes in
	 * a continuous loop after loading, then it should check the
	 * status of this flag after every loop.
	 */
	protected volatile boolean isRunning = true;
	
	/**
	 * A queue of commands that were sent to this driver while it was
	 * inactive.  The status of each driver is determined by its
	 * Thread.State.  If a driver is terminated then any incoming
	 * messages will be placed in this queue before the driver is
	 * restarted.  Each driver should check (or clear) this queue
	 * within its run() method.  If the driver runs continuously
	 * then this queue should be checked during each loop.
	 */
	protected volatile Deque<String> queuedCommands = new ArrayDeque<String>();
	
	/**
	 * A queue of data that was sent to this driver while it was
	 * inactive.  The status of each driver is determined by its
	 * Thread.State.  If a driver is terminated then any incoming
	 * messages will be placed in this queue before the driver is
	 * restarted.  Each driver should check (or clear) this queue
	 * within its run() method.  If the driver runs continuously
	 * then this queue should be checked during each loop.
	 */
	protected volatile Deque<byte[]> queuedBinary = new ArrayDeque<byte[]>();

	/**
	 * Sends a command to this drivers remote module.  This is the
	 * interface that the front-ends use to control each remote
	 * module.  It is up to each driver to check the incoming command
	 * for validity before calling
	 * {@link org.apparatus_templi.Coordinator#sendCommand(Driver, String)}
	 * @param command the command to send to the remote module.
	 */
	abstract void receiveCommand(String command);
	
	/**
	 * Sends binary data to this drivers remote module.  This is the
	 * interface that the front-ends use to control each remote
	 * module.  It is up to each driver to check the incoming data
	 * for validity before calling
	 * {@link org.apparatus_templi.Coordinator#sendBinary(Driver, byte[])}
	 * @param data the command to send to the remote module.
	 */
	abstract void receiveBinary(byte[] data);
	
	/**
	 * Returns a String of XML data representing a widget view
	 * of this driver.  The widget data should be a compact view of
	 * all sensor data or controller commands available, or full
	 * data from a limited set of sensors or controllers.
	 * @return a String of XML widget data.
	 */
	abstract String getWidgetXML();
	
	/**
	 * Returns a String of XML data representing a full view
	 * of this drivers data and capabilities.  This XML data
	 * should be a detailed view of all the sensors data or
	 * controller commands available. 
	 * @return a String of XML data.
	 */
	abstract String getFullPageXML();
	
	/**
	 * Notify the driver that it should begin terminating.  More
	 * specifically sets the boolean flag isRunning to false.
	 * Drivers should query this flag periodically to know when
	 * to begin their termination procedure (if any).
	 */
	final void terminate() {
		this.isRunning = false;
	}
	
	/**
	 * Returns the unique String name of this driver.
	 * @return the name of this driver.
	 */
	final String getModuleName() {
		return this.name;
	}
	
	final void queueCommand(String command) {
		queuedCommands.add(command);
	}
	
	final void queueBinary(byte[] data) {
		queuedBinary.add(data);
	}
	
	protected final String readQueuedCommand() {
		String message = null;
		if (queuedCommands.peek() != null) {
			message = queuedCommands.poll();
		}
		return message;
	}
	
	protected final byte[] readQueuedBinary() {
		byte[] data = null;
		if (queuedBinary.peek() != null) {
			data = queuedBinary.poll();
		}
		return data;
	}
	
	public String getDriverType() {
		return Driver.TYPE;
	}
	
	public String getName() {
		return name;
	}
	
	protected final synchronized void sleep()  {
		Coordinator.scheduleWake(this);
		try {
			this.wait();
		} catch (InterruptedException e) {
			Log.d(TYPE, "sleeping driver woken by interrupt");
			this.notify();
		}
	}
	
	protected final synchronized void sleep(long sleepTime)  {
		Coordinator.scheduleWake(this, System.currentTimeMillis() + sleepTime);
		try {
			this.wait();
		} catch (InterruptedException e) {
			Log.d(TYPE, "sleeping driver woken by interrupt");
			this.notify();
		}
	}
	
	public final synchronized void wake() {
		this.notify();
	}
	
}
