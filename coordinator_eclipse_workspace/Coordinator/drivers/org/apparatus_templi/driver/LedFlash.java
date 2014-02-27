package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

/**
 * A driver that controls a remote array of LED pixels.
 * 
 * Remote side expects commands in the form of:
 * 	"(int)"
 * where (int) is the single digit value of the pin number to power.
 * Valid values are 4 - 9
 * 
 * Driver does not listen for any responses
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */

public class LedFlash extends ControllerModule {
	
	public LedFlash() {
		this.name = "LED_FLASH";
	}
	
	/*
	 * I'm uncertain what the best way to implement this is.  Either
	 * we can return back the strings "Controller","Sensor","Combo",
	 * or change the interfaces to require an isSensorModule() and
	 * isControllerModule().  I think the latter would be best, since
	 * additional types might be added later.
	 */
	

	/*
	 * Since every driver is exited to have intimate knowledge of
	 * the remote module that it corresponds with this can be a hard
	 * coded XML response.
	 */
	@Override
	public ArrayList<String>  getControllerList() {
		Log.d(name, "getControllerListXML() returning hard coded value for now");
//		return new String(	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//							"<controlerList>" +
//								"<controler>" +
//									"<name>LED 1</name>" +
//								"</controler>" +
//								"<controler>" +
//									"<name>LED 2</name>" +
//								"</controler>" +
//								"<controler>" +
//									"<name>LED 1</name>" +
//								"</controler>" +
//							"</controlerList>");
		return null;
						
	}
	
	/*
	 * Since our simple driver does not keep track of the status of the LEDs
	 * it can only respond with 'Unknown' for the status.  If this were a real
	 * driver you would want to check that the controlerName is valid, and
	 * return a response based of of the controller's last known status.
	 */
	@Override
	public String getControllerStatusXML(String controllerName) {
		Log.d(name, "getControllerStatusXML() returning hard coded value for now");
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
				Coordinator.sendCommand(this, "4");
				break;
			case "LED 2":
				Coordinator.sendCommand(this, "5");
				break;
			case "LED 3":
				Coordinator.sendCommand(this, "6");
				break;
			default:
				Log.e(name, "tellController() Given invalid LED name");
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
		Log.d(name, "starting");
		//check for any queued messages
		while (queuedCommands.size() > 0) {
			receiveCommand(queuedCommands.poll());
		}
		
		while (queuedBinary.size() > 0) {
			receiveBinary(queuedBinary.poll());
		}
		
		if (Coordinator.isModulePresent(name)) {
			while (isRunning) {
				/*
				 * This is our main loop.  All of the processing will happen here
				 * Our simple driver will repeatedly send three messages to the
				 * remote module, sleeping a few seconds between each message.
				 */
				for (int i = 5; i < 10; i++) {
					Log.d(name, "flashing LED on pin " + i);
					if (Coordinator.sendCommand(this, String.valueOf(i))) {
//						Log.d(name, "message sent");
					} else {
//						Log.d(name, "message could not be sent");
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			Log.e(name, "remote module not present, shutting down");
		}
		
		Log.d(name, "terminating");
	}

	/*
	 * We don't care about any response messages.
	 */
	@Override
	public void receiveCommand(String command) {
		Log.d(name, "received command, ignoring");
	}

	@Override
	public void receiveBinary(byte[] data) {
		Log.d(name, "received binary, ignoring");
		
	}

	/*
	 * The XML format for the widget needs to be standardized
	 */
	@Override
	public String getWidgetXML() {
		Log.w(name, "getWidgetXML() unimplimented");
		return "<widget><element name='led1'><state>on</state></element></widget>";
	}

	/*
	 * The XML format for the full page control needs to be standardized
	 */
	@Override
	public String getFullPageXML() {
		Log.w(name, "getFullPageXML() unimplimented");
		return null;
	}

}
