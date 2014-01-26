package org.apparatus_templi;

/**
 * LedFlash
 * Controls a remote array of LED pixels.
 * 
 * Remote side expects commands in the form of:
 * 	"(int)"
 * where (int) is the single digit value of the pin number to power.
 * Valid values are 4 - 9
 * 
 * Driver does not listen for any responses
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */

public class LedFlash implements ControllerModule, Runnable {
	private String moduleName = "LED_FLASH";
	private volatile boolean running = true;
	
	public LedFlash() {
		
	}
	
	public LedFlash(String message) {
		
	}
	
	/*
	 * I'm uncertain what the best way to implement this is.  Either
	 * we can return back the strings "Controller","Sensor","Combo",
	 * or change the interfaces to require an isSensorModule() and
	 * isControllerModule().  I think the latter would be best, since
	 * additional types might be added later.
	 */
	@Override
	public String getModuleType() {
		return "Controller";
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}
	
	
	/*
	 * Tell the thread that execution should stop.
	 * This does not immediately terminate the thread, but sets a flag
	 * so that the infinite loop in run() will exit.
	 */
	@Override
	public void terminate() {
		running = false;
	}

	/*
	 * Since every driver is exited to have intimate knowledge of
	 * the remote module that it corresponds with this can be a hard
	 * coded XML response.
	 */
	@Override
	public String getControllerListXML() {
		Log.d(moduleName, "getControllerListXML() returning hard coded value for now");
		return new String(	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
							"<controlerList>" +
								"<controler>" +
									"<name>LED 1</name>" +
								"</controler>" +
								"<controler>" +
									"<name>LED 2</name>" +
								"</controler>" +
								"<controler>" +
									"<name>LED 1</name>" +
								"</controler>" +
							"</controlerList>");
						
	}
	
	/*
	 * Since our simple driver does not keep track of the status of the LEDs
	 * it can only respond with 'Unknown' for the status.  If this were a real
	 * driver you would want to check that the controlerName is valid, and
	 * return a response based of of the controller's last known status.
	 */
	@Override
	public String getControllerStatusXML(String controllerName) {
		Log.d(moduleName, "getControllerStatusXML() returning hard coded value for now");
		return new String(	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
							"<controller>" +
								"<name>" + controllerName + "</name>" +
								"<status>Unknown</status>" +
							"</controller>");
	}

	/*
	 * This method will be called by the Coordinator on behalf
	 * of the front-ends.  It is up to you do validate the incoming
	 * controllerName and command (if desired).  In this case we will
	 * switch based off the controllerName.  Since the only thing that
	 * this driver does is flash the LED we do not even have to check the
	 * command.
	 * 
	 * Note that the commands being send are very simple (a single digit
	 * number corresponding to a pin on the arduino). A more complex
	 * driver might send multiple pin numbers at a time, or even a length
	 * of time in which to flash.  The format of the command is free form
	 * text, but steer clear of using the line feed character for now.
	 */
	@Override
	public void tellController(String controllerName, String command) {
		switch (controllerName) {
			case "LED 1":
				Coordinator.sendCommand(moduleName, "4");
				break;
			case "LED 2":
				Coordinator.sendCommand(moduleName, "5");
				break;
			case "LED 3":
				Coordinator.sendCommand(moduleName, "6");
				break;
			default:
				Log.e(moduleName, "tellController() Given invalid LED name");
				break;
		}
	}

	/*
	 * Our starting point of execution for this driver.
	 * This simply runs a loop sending commands to flash LEDs attached to
	 * different pins.  The driver does not care about responses from the
	 * remote module, so receiveMessage() is left unimplemented.  If the
	 * Coordinator needs to shut down this thread it can do so through the
	 * terminate() method, which will make the while loop exit
	 * 
	 * The startup procedure is very simple, ask the Coordinator if the
	 * remote module is active.  If it isn't, then terminate, else begin sending
	 * commands 
	 */
	@Override
	public void run() {
		if (Coordinator.isModulePresent(moduleName)) {
			while (running) {
				/*
				 * This is our main loop.  All of the processing will happen here
				 * Our simple driver will repeatedly send three messages to the
				 * remote module, sleeping 5 seconds between each message.
				 */
				for (int i = 3; i < 6; i++) {
					Log.d(moduleName, "flashing LED on pin " + i);
					Coordinator.sendCommand(moduleName, String.valueOf(i));
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * We don't care about any response messages.
	 */
	@Override
	public void receiveMessage(String message) {
		Log.d(moduleName, "received message, ignoring");
	}

	/*
	 * The XML format for the widget needs to be standardized
	 */
	@Override
	public String getWidgetXML() {
		Log.w(moduleName, "getWidgetXML() unimplimented");
		return null;
	}

	/*
	 * The XML format for the full page control needs to be standardized
	 */
	@Override
	public String getFullPageXML() {
		Log.w(moduleName, "getFullPageXML() unimplimented");
		return null;
	}

}