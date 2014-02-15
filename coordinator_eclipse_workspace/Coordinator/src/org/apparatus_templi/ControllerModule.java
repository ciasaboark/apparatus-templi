package org.apparatus_templi;

import java.util.ArrayList;

/**
 * An abstract class for a driver that controls a remote
 * module containing motors, switches, relays and the like.
 * A driver extending this class would likely be paired with
 * a remote module that expects to take commands, but will
 * not otherwise act on its own.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public abstract class ControllerModule extends Driver {	
	/**
	 * Used to determine which type of Driver each driver
	 * should be cast to. 
	 */
	static final String TYPE = "Controller";
	
	/**
	 * Return a list of all controllers on the remote module.
	 * The driver is expected to keep track of which controllers are
	 * available on its corresponding remote module.  The front
	 * ends will use this interface to get a list of what controllers
	 * are available.
	 * @return an ArrayList of Strings of all known controllers on the remote
	 * 	module.
	 */
	abstract ArrayList<String> getControllerList();
	
	/**
	 * Returns an XML description of the current status of the requested
	 * 	controller.
	 * @param controllerName The name of the controller on the remote module.
	 * @return a String of XML data describing the current status of the
	 * 	controller. If the specified controller does not exist, then returns null.
	 */
	abstract String getControllerStatusXML(String controllerName);
	
	/**
	 * Sends a command to a specific controller on the remote module.
	 * Called by the Coordinator on behalf of the front-ends.  It is up to
	 *  you do validate the incoming controllerName and command (if desired),
	 *  and to reformat the command (if needed) before the message is sent.
	 * @param controllerName the String name of the controller on the
	 * 	remote module.
	 * @param command the String command to send to the controller.
	 */
	abstract void tellController(String controllerName, String command);
	
	/**
	 * Returns the String type of Driver.  This type can be checked
	 * 	against SensorModule.TYPE and ControllerModule.TYPE to determine
	 * 	the type of driver.
	 */
	public String getDriverType() {
		return ControllerModule.TYPE;
	}
}