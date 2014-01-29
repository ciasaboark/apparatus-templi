package org.apparatus_templi;

public abstract class ControllerModule extends Driver {	
	/*
 	 * Abstract methods to be implemented in subclass
	 */
	abstract String getControllerListXML();
	abstract String getControllerStatusXML(String controllerName);
	abstract void tellController(String controllerName, String command);
	
	String getModuleType() {
		return "Controller";
	}
}