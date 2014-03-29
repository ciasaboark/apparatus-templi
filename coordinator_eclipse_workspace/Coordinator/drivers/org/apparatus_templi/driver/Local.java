package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.XmlFormatter;

public class Local extends ControllerModule {
	private final int[] leds = { 5, 6, 7 };
	private final boolean[] ledsState = { false, false, false };
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Local Controller");
	private final XmlFormatter fullFormatter = new XmlFormatter(this, "Local Controller");
	private final Controller led1 = new Controller("0: Purple");
	private final Controller led2 = new Controller("1: Green");
	private final Controller led3 = new Controller("2: White");
	private final Button toggleButton = new Button("ToggleLED");

	public Local() {
		this.name = "LOCAL";
		toggleButton.setAction("$input");
		toggleButton.setInputType("numeric");
		widgetXml.setRefresh(6000);
		widgetXml.addElement(led1);
		widgetXml.addElement(led2);
		widgetXml.addElement(led3);
		widgetXml.addElement(toggleButton);
		led1.setStatus("off");
		led2.setStatus("off");
		led3.setStatus("off");
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
		// terminate();
		// }

		while (isRunning) {
			// all interaction with this module is done through the widget,
			// just sleep until a message is received
			this.sleep();
		}

		Coordinator.sendCommand(this, "RESET");
		// thread is terminating, do whatever cleanup is needed
		Log.d(this.name, "terminating");
	}

	private void toggleLED(int ledNum) {
		if (ledNum < 0 || ledNum > 2) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveCommand(String command) {
		if (command != null) {
			if (command.startsWith("OK")) {
				Log.d(this.name, "received confirmation from remote module: " + command);
			} else {
				Integer i = null;
				try {
					i = Integer.parseInt(command);
					if (i < 0 || i > 2) {
						throw new NumberFormatException("Led number out of range");
					}
					// Coordinator.wakeSelf(this);
					toggleLED(i);
				} catch (NumberFormatException e) {
					Log.w(this.name, "not a valid number");
				}
			}
		}
	}

	@Override
	public void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullFormatter.generateXml();
	}

}
