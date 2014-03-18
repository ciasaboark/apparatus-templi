package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.XmlFormatter;

/**
 * A driver for a remote module with a number of LEDs. Each LED can be turned on or off, and the
 * driver keeps track of the state of each 'pixel'
 * 
 * Remote side expects one of the following commands: "RESET" - reset all attached LEDs to their off
 * state "(int):(int)" - set the state of the LED attached to the first (int) to the boolean value
 * of the second int. Valid values for the first int are 4,5,6. The second int may be any valid
 * signed int value, with 0 equating to false, all other values to true.
 * 
 * Driver listens for the following responses: "RESETOK" - the reset command was received and all
 * LEDs were reset to off. "OK(int)" - the state of the LED on pin number (int) was toggled.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public class StatefullLed extends ControllerModule {

	// our remote module has three LEDs attached
	private final int[] leds = { 5, 6, 7 };
	private final boolean[] ledsState = { false, false, false };
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Stateful LED");
	private final Controller led1 = new Controller("Led1");
	private final Controller led2 = new Controller("Led2");
	private final Controller led3 = new Controller("Led3");

	public StatefullLed() {
		this.name = "State_LED";
		widgetXml.addElement(led1);
		widgetXml.addElement(led2);
		widgetXml.addElement(led3);
		led1.setStatus("off");
		led2.setStatus("off");
		led3.setStatus("off");
	}

	@Override
	public void receiveCommand(String message) {
		// throw away the message for now
		// Log.d(this.name, "received message '"+ message + "', ignoring");

	}

	@Override
	public void receiveBinary(byte[] data) {
		Log.d(this.name, "received binary, ignoring");

	}

	@Override
	public String getWidgetXML() {
		Log.w(this.name, "getWidgetXML() unimplimented");
		String xml = widgetXml.generateXml();
		return xml;
	}

	@Override
	public String getFullPageXML() {
		Log.w(this.name, "getFullPageXML() unimplimented");
		return null;
	}

	@Override
	public void run() {
		Log.d(this.name, "starting");

		// check for any queued messages
		while (queuedCommands.size() > 0) {
			receiveCommand(queuedCommands.pop());
		}

		// // If the remote module is not there then terminate
		// if (!Coordinator.isModulePresent(this.name)) {
		// Log.e(this.name, "remote module is not present, shutting down");
		// terminatee();
		// }

		while (isRunning) {
			// run through a hard-coded loop for now.
			int ledNum = 0 + (int) (Math.random() * ((2 - 0) + 1));
			Log.d(this.name, "toggling LED " + ledNum);
			toggleLED(ledNum);
			this.sleep(3000);
		}

		// thread is terminating, do whatever cleanup is needed
		Coordinator.sendCommand(this, "RESET");
		Log.d(this.name, "terminating");

	}

	private void toggleLED(int ledNum) {
		if (ledNum > 2 || ledNum < 0) {
			Log.e(this.name, "toggleLED() given invalid led number");
		} else {
			boolean newState = !ledsState[ledNum];
			Coordinator.sendCommand(this, String.valueOf(leds[ledNum])
					+ (ledsState[ledNum] ? "0" : "1"));
			ledsState[ledNum] = newState;

			switch (ledNum) {
			case 0:
				led1.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
				break;
			case 1:
				led2.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
				break;
			case 2:
				led3.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
				break;
			}
		}
	}

	@Override
	public ArrayList<String> getControllerList() {
		Log.w(this.name, "getControllerListXML() unimplimented for now");
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		Log.w(this.name, "getControllerStatusXML() unimplimented for now");
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		Log.d(this.name, "tellController() not validating command, passing without verification");
		Coordinator.sendCommand(this, command);

	}

}
