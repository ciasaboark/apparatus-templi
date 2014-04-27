package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.XmlFormatter;

public class DoorSensor extends Driver implements EventGenerator {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Door Sensor");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Door Sensor");
	private final Controller sensor = new Controller("Door");

	public DoorSensor() {
		this.name = "DoorSensor";
		widgetXml.setRefresh(2);
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
			if (command.equals("O")) {
				Log.d(this.name, "door is opened");
				sensor.setStatus("opened");
				goodCommand = true;
			} else if (command.equals("C")) {
				Log.d(this.name, "door is closed");
				sensor.setStatus("closed");
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
