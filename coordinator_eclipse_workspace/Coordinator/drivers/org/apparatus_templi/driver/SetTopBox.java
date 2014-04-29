package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.MotionEvent;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class SetTopBox extends Driver implements EventGenerator {
	private final int[] leds = { 5, 6, 7 };
	private final boolean[] ledsState = { false, false, false };
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Set Top Box");
	private final XmlFormatter fullFormatter = new XmlFormatter(this, "Set Top Box");
	private final Long lastMotion = null;

	private final TextArea rgbDescription = new TextArea("rgb desc",
			"Set the red, blue, and green values of the indicator LED");
	private final Button redLed = new Button("Red");
	private final Button greenLed = new Button("Green");
	private final Button blueLed = new Button("Blue");
	private final Button toggleButton = new Button("Toggle indicator LED");
	private final Pre description = new Pre(
			"desc",
			"<h3>Stateful LED controller</h3>"
					+ "<p>Controll an array of LED elements.  Input the number of the LED to toggle"
					+ ", then click the \"Toggle LED\" button</p>"
					+ "<img src=\"http://icons.iconarchive.com/icons/double-j-design/electronics/256/LED-icon.png\" />");

	public SetTopBox() {
		this.name = "LOCAL";
		toggleButton.setAction("toggle").setInputType("none");
		redLed.setAction("red$input").setInputType(InputType.NUM).setInputVal("50");
		greenLed.setAction("green$input").setInputType(InputType.NUM).setInputVal("200");
		blueLed.setAction("blue$input").setInputType(InputType.NUM).setInputVal("240");
		widgetXml.setRefresh(6000);
		widgetXml.addElement(rgbDescription);
		widgetXml.addElement(redLed);
		widgetXml.addElement(greenLed);
		widgetXml.addElement(blueLed);
		widgetXml.addElement(toggleButton);

		fullFormatter.setRefresh(6000);
		fullFormatter.addElement(description);
		fullFormatter.addElement(rgbDescription);
		fullFormatter.addElement(redLed);
		fullFormatter.addElement(greenLed);
		fullFormatter.addElement(blueLed);
		fullFormatter.addElement(toggleButton);
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

	// private void toggleLED(int ledNum) {
	// if (ledNum < 0 || ledNum > 2) {
	// Log.e(this.name, "toggleLED() given invalid led number");
	// } else {
	// boolean newState = !ledsState[ledNum];
	// String response = Coordinator.sendCommandAndWait(this, String.valueOf(leds[ledNum])
	// + (ledsState[ledNum] ? "0" : "1"), 6);
	// if (response != null && response.startsWith("OK")) {
	// ledsState[ledNum] = newState;
	//
	// switch (ledNum) {
	// case 0:
	// redLed.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
	// break;
	// case 1:
	// greenLed.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
	// break;
	// case 2:
	// blueLed.setStatus(String.valueOf(ledsState[ledNum] ? "on" : "off"));
	// break;
	// }
	// }
	// }
	// }

	@Override
	public boolean receiveCommand(String command) {
		boolean goodCommand = false;
		if (command != null) {
			if (command.equals("mot")) {
				if (lastMotion == null || System.currentTimeMillis() - lastMotion >= 3000) {
					// only send a motion event every 3 seconds
					MotionEvent e = new MotionEvent(System.currentTimeMillis(), this);
					Coordinator.receiveEvent(this, e);
				}
				goodCommand = true;
			} else if (command.equals("toggle")) {
				// toggle the indicator LED
				Log.d(this.name, "toggling indicator led");
				Coordinator.sendCommand(this, "toggle");
			} else if (command.startsWith("red")) {
				// toggle red led
				Log.d(this.name, "setting new blue value");
				try {
					Integer i = Integer.parseInt(command.substring(4));
					if (i >= 0 && i <= 255) {
						goodCommand = true;
						Coordinator.sendCommand(this, "r" + i);
					} else {
						throw new NumberFormatException(
								"integer value must be between 0 - 255 inclusive");
					}
				} catch (NumberFormatException e) {
					Log.w(this.name, "could not read valid int value from: " + command);
				}
			} else if (command.startsWith("green")) {
				// toggle green led
				Log.d(this.name, "setting new blue value");
				try {
					Integer i = Integer.parseInt(command.substring(4));
					if (i >= 0 && i <= 255) {
						goodCommand = true;
						Coordinator.sendCommand(this, "g" + i);
					} else {
						throw new NumberFormatException(
								"integer value must be between 0 - 255 inclusive");
					}
				} catch (NumberFormatException e) {
					Log.w(this.name, "could not read valid int value from: " + command);
				}
			} else if (command.startsWith("blue")) {
				// toggle blue led
				Log.d(this.name, "setting new blue value");
				try {
					Integer i = Integer.parseInt(command.substring(4));
					if (i >= 0 && i <= 255) {
						goodCommand = true;
						Coordinator.sendCommand(this, "b" + i);
					} else {
						throw new NumberFormatException(
								"integer value must be between 0 - 255 inclusive");
					}
				} catch (NumberFormatException e) {
					Log.w(this.name, "could not read valid int value from: " + command);
				}
			}
		}
		return goodCommand;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullFormatter.generateXml();
	}

	@Override
	@Deprecated
	public ArrayList<Event> getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
