package org.apparatus_templi;

public abstract class Driver implements Runnable {
	protected String name = null;
	
	protected volatile boolean running = true;

	abstract String getModuleType();
	abstract void receiveCommand(String command);
	abstract String getWidgetXML();
	abstract String getFullPageXML();
	

	final void terminate() {
		this.running = false;
	}
	
	final String getModuleName() {
		return this.name;
	}
	
	final Thread.State getState() {
		return Thread.currentThread().getState();
	}
	
	
}
