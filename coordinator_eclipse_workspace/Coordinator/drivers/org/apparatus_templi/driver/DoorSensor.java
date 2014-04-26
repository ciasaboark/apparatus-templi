package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class DoorSensor extends Driver implements EventGenerator{
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Door Sensor");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Door Sensor");
		private final Controller sensor = new Controller("Door");

	public DoorSensor() {
		this.name = "DoorSensor";
		widgetXml.addElement(sensor);
		fullXml.addElement(sensor);
		sensor.setStatus("");
	}

	@Override
	public void run() {

		while (!this.queuedCommands.isEmpty()) {
			this.receiveCommand(this.queuedCommands.pop());
		}

		while (this.isRunning) {
			this.sleep();
			Log.d(this.name, "waking");
		}

	}

	@Override
	public boolean receiveCommand(String command) {
		boolean goodCommand = false;
		if (command != null) {
			if (command.equals("toggle")) {
				Log.d(this.name, "toggle command received");
				Coordinator.sendCommand(this, "1");
				sensor.setStatus("opened");
				goodCommand = true;
			}
		}
		return goodCommand;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullXml.generateXml();
	}

	@Override
	public ArrayList<Event> getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
