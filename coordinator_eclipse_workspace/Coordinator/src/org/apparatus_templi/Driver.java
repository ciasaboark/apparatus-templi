package org.apparatus_templi;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class Driver implements Runnable {
	protected String name = null;
	
	protected volatile boolean running = true;
	protected volatile Deque<byte[]> queuedMessages = new ArrayDeque<byte[]>();

	abstract String getModuleType();
	abstract void receiveCommand(String command);
	abstract String getWidgetXML();
	abstract String getFullPageXML();
	abstract void processMessage(byte[] message);
	

	final void terminate() {
		this.running = false;
	}
	
	final String getModuleName() {
		return this.name;
	}
	
	final Thread.State getState() {
		return Thread.currentThread().getState();
	}
	
	final void queueMessage(byte[] message) {
		queuedMessages.add(message);
	}
	
	final byte[] readQueuedMessage() {
		byte[] message = null;
		if (queuedMessages.peek() != null) {
			message = queuedMessages.pop();
		}
		return message;
	}
	
}
