package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

/**
 * A driver that controls a remote array of LED pixels.
 * 
 * Remote side expects commands in the form of: "(int)" where (int) is the single digit value of the
 * pin number to power. Valid values are 4 - 9
 * 
 * Driver does not listen for any responses
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */

public class LedFlash extends Driver {

	public LedFlash() {
		this.name = "LED_FLASH";
	}

	/*
	 * I'm uncertain what the best way to implement this is. Either we can return back the strings
	 * "Controller","Sensor","Combo", or change the interfaces to require an isSensorModule() and
	 * isControllerModule(). I think the latter would be best, since additional types might be added
	 * later.
	 */

	/*
	 * Our starting point of execution for this driver. This simply runs a loop sending commands to
	 * flash LEDs attached to different pins. The driver does not care about responses from the
	 * remote module, so receiveMessage() is left unimplemented. If the Coordinator needs to shut
	 * down this thread it can do so through the terminate() method, which will make the while loop
	 * exit
	 * 
	 * The startup procedure is very simple, ask the Coordinator if the remote module is active. If
	 * it isn't, then terminate, else begin sending commands
	 */
	@Override
	public void run() {
		Log.d(name, "starting");
		// check for any queued messages
		while (queuedCommands.size() > 0) {
			receiveCommand(queuedCommands.poll());
		}

		while (queuedBinary.size() > 0) {
			receiveBinary(queuedBinary.poll());
		}

		if (Coordinator.isModulePresent(name)) {
			while (isRunning) {
				/*
				 * This is our main loop. All of the processing will happen here Our simple driver
				 * will repeatedly send three messages to the remote module, sleeping a few seconds
				 * between each message.
				 */
				for (int i = 5; i < 8; i++) {
					Log.d(name, "flashing LED on pin " + i);
					if (Coordinator.sendCommand(this, String.valueOf(i))) {
						// Log.d(name, "message sent");
					} else {
						// Log.d(name, "message could not be sent");
					}
					this.sleep(3000);
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
	public boolean receiveCommand(String command) {
		Log.d(name, "received command, ignoring");
		return true;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		Log.d(name, "received binary, ignoring");
		return true;
	}

	/*
	 * The XML format for the widget needs to be standardized
	 */
	@Override
	public String getWidgetXML() {
		Log.w(name, "getWidgetXML() unimplimented");
		return null;
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
