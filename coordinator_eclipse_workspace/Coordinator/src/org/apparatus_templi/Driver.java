package org.apparatus_templi;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class Driver implements Runnable {
	protected String name = null;
	
	protected volatile boolean running = true;
	protected volatile Deque<String> queuedCommands = new ArrayDeque<String>();
	protected volatile Deque<byte[]> queuedBinary = new ArrayDeque<byte[]>();

	abstract String getModuleType();
	abstract void receiveCommand(String command);
	abstract void receiveBinary(byte[] data);
	abstract String getWidgetXML();
	abstract String getFullPageXML();
//	abstract void processMessage(byte[] message);
	

	final void terminate() {
		this.running = false;
	}
	
	final String getModuleName() {
		return this.name;
	}
	
	final Thread.State getState() {
		return Thread.currentThread().getState();
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
	
}
