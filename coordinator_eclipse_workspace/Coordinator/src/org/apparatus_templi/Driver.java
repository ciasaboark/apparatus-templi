package org.apparatus_templi;

public abstract class Driver implements Runnable {
	protected String name = null; //name of the device Empty String as default or could assign a random name if not assigned one
	
	protected volatile boolean running = true;

	abstract String getModuleType();
	abstract void receiveCommand(String command);
	abstract String getWidgetXML();
	abstract String getFullPageXML();
	

	final void terminate() {
		this.running = false; //call the subclass implementation
	}

	/*
	 * If the device is not given a name during creation, it will have an empty string as default
	 * The thread's begin_execution() function (make shift constructor) can handle this. It can assign it a random
         * name. 
 	 */
	
	final String getModuleName() {
		return this.name;
	}
	
	final Thread.State getState() {
		return Thread.currentThread().getState();
	}
	
	
}
