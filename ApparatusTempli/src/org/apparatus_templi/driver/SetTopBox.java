/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

		while (isRunning) {
			// all interaction with this module is done through the widget,
			// just sleep until a message is received
			this.sleep();
		}

		// thread is terminating, do whatever cleanup is needed
		Coordinator.sendCommand(this, "RESET");
		Log.d(this.name, "terminating");
	}

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
