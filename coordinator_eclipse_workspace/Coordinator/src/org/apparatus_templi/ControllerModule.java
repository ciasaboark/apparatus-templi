package org.apparatus_templi;

import java.util.ArrayList;

public abstract class ControllerModule extends Driver {	
	/*
 	 * Abstract methods to be implemented in subclass
	 */
	static final String TYPE = "Controller";
	abstract ArrayList<String> getControllerList();
	abstract String getControllerStatusXML(String controllerName);
	abstract void tellController(String controllerName, String command);
	
	String getModuleType() {
		return ControllerModule.TYPE;
	}
}