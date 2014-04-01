package org.apparatus_templi.driver;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

/**
 * An abstract class for a basic driver. This driver should not be sub-classed directly. Each driver
 * should subclass either SensorModule or ControllerModule. Drivers directly sub-classing Driver
 * might not be loaded. for execution.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public abstract class Driver implements Runnable {
	/**
	 * The unique name of this driver. This name should match the name of the remote module it
	 * corresponds with, and must be 10 characters or less.
	 */
	protected String name = null;

	/**
	 * Indicates whether or not this driver should continue running, or begin its termination
	 * process. If the driver executes in a continuous loop after loading, then it should check the
	 * status of this flag after every loop.
	 */
	protected volatile boolean isRunning = true;

	/**
	 * A queue of commands that were sent to this driver while it was inactive. The status of each
	 * driver is determined by its Thread.State. If a driver is terminated then any incoming
	 * messages will be placed in this queue before the driver is restarted. Each driver should
	 * check (or clear) this queue within its run() method. If the driver runs continuously then
	 * this queue should be checked during each loop.
	 */
	protected volatile Deque<String> queuedCommands = new ArrayDeque<String>();

	/**
	 * A queue of data that was sent to this driver while it was inactive. The status of each driver
	 * is determined by its Thread.State. If a driver is terminated then any incoming messages will
	 * be placed in this queue before the driver is restarted. Each driver should check (or clear)
	 * this queue within its run() method. If the driver runs continuously then this queue should be
	 * checked during each loop.
	 */
	protected volatile Deque<byte[]> queuedBinary = new ArrayDeque<byte[]>();

	/**
	 * Sends a command to this drivers remote module. This is the interface that the front-ends use
	 * to control each remote module. It is up to each driver to check the incoming command for
	 * validity before calling {@link org.apparatus_templi.Coordinator#sendCommand(Driver, String)}
	 * 
	 * @param command
	 *            the command to send to the remote module.
	 * @return TODO
	 */
	public abstract boolean receiveCommand(String command);

	/**
	 * Sends binary data to this drivers remote module. This is the interface that the front-ends
	 * use to control each remote module. It is up to each driver to check the incoming data for
	 * validity before calling {@link org.apparatus_templi.Coordinator#sendBinary(Driver, byte[])}
	 * 
	 * @param data
	 *            the command to send to the remote module.
	 * @return TODO
	 */
	public abstract boolean receiveBinary(byte[] data);

	/**
	 * Returns a String of XML data representing a widget view of this driver. The widget data
	 * should be a compact view of all sensor data or controller commands available, or full data
	 * from a limited set of sensors or controllers.
	 * 
	 * @return a String of XML widget data.
	 */
	public abstract String getWidgetXML();

	/**
	 * Returns a String of XML data representing a full view of this drivers data and capabilities.
	 * This XML data should be a detailed view of all the sensors data or controller commands
	 * available.
	 * 
	 * @return a String of XML data.
	 */
	public abstract String getFullPageXML();

	/**
	 * Notify the driver that it should begin terminating. More specifically sets the boolean flag
	 * isRunning to false. Drivers should query this flag periodically to know when to begin their
	 * termination procedure (if any).
	 */
	public final void terminate() {
		this.isRunning = false;
	}

	/**
	 * Returns the unique String name of this driver.
	 * 
	 * @return the name of this driver.
	 */
	public final String getModuleName() {
		return this.name;
	}

	/**
	 * Places the String command into the Driver's command queue
	 * 
	 * @param command
	 *            the command to enqueue
	 */
	public final void queueCommand(String command) {
		queuedCommands.add(command);
	}

	/**
	 * Places a block of binary data into the Driver's data queue
	 * 
	 * @param data
	 *            the data to enqueue
	 */
	public final void queueBinary(byte[] data) {
		queuedBinary.add(data);
	}

	/**
	 * Pull the front-most element from the command queue
	 * 
	 * @return the head of the command queue, or null if there are no more commands to process
	 */
	protected final String readQueuedCommand() {
		String message = null;
		if (queuedCommands.peek() != null) {
			message = queuedCommands.poll();
		}
		return message;
	}

	/**
	 * Pull the front-most element from the data queue
	 * 
	 * @return the head of the data queue, or null if there are no more data blocks to process
	 */
	protected final byte[] readQueuedBinary() {
		byte[] data = null;
		if (queuedBinary.peek() != null) {
			data = queuedBinary.poll();
		}
		return data;
	}

	/**
	 * Return the short (10 character or less) name of the driver.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Places this driver into a blocking state until it is woken by the Coordinator. After entering
	 * this state the Driver will remain BLOCKED until an incoming message address to this driver is
	 * received, or until the service is stopping, whichever comes first. The blocking will only
	 * affect the execution of the {@link Driver#run()} method.
	 */
	protected final synchronized void sleep() {
		// this method should only be called on the driver's thread, indefinite blocking could
		// result otherwise
		long thisThread = Thread.currentThread().getId();
		long driverThread = Coordinator.getDriverThreadId(this);

		if (Thread.currentThread().getId() == Coordinator.getDriverThreadId(this)) {
			Coordinator.scheduleSleep(this, Thread.currentThread().getId());
			try {
				this.wait();
			} catch (InterruptedException e) {
				Log.d(this.getClass().getName(), "sleeping driver woken by interrupt");
				this.notify();
			}
		} else {
			Log.e(this.name, "driver can only sleep on its own thread");
		}
	}

	/**
	 * Places this driver into a blocking state until it is woken by the Coordinator or until its
	 * sleepPeriod has expired. After entering this state the Driver will remain BLOCKED until an
	 * incoming message address to this driver is received, or until the service is stopping, or
	 * until its sleep period has expired, whichever comes first. The blocking will only affect the
	 * execution of the {@link Driver#run()} method.
	 * 
	 * @param sleepPeriod
	 *            the length of time in milliseconds that this driver should block.
	 */
	protected final synchronized void sleep(long sleepPeriod) {
		// this method should only be called on the driver's thread, indefinite blocking could
		// result otherwise

		if (Thread.currentThread().getId() == Coordinator.getDriverThreadId(this)) {
			Coordinator.scheduleSleep(this, System.currentTimeMillis() + sleepPeriod, Thread
					.currentThread().getId());
			try {
				this.wait();
			} catch (InterruptedException e) {
				Log.d(this.getClass().getName(), "sleeping driver woken by interrupt");
				this.notify();
			}
		} else {
			Log.e(this.name, "driver can only sleep on its own thread");
		}
	}

	/**
	 * Wakes this driver if it is in a sleep period, does nothing otherwise.
	 */
	public final synchronized void wake() {
		this.notify();
	}

}
