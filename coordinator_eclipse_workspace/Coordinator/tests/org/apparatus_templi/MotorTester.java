package org.apparatus_templi;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class MotorTester extends Driver {
	private final Controller cont;
	private final TextArea ta;
	private final XmlFormatter widgetXml;
	private final XmlFormatter fullXml;
	private final Button contButton;

	public MotorTester() {
		widgetXml = new XmlFormatter(this, "Motor Driver").setRefresh(10);
		fullXml = new XmlFormatter(this, "Motor Driver").setRefresh(10);
		this.name = "MotGen";
		ta = new TextArea("descr", "This is the long text area");
		cont = new Controller("cont1").setIcon("fa fa-user").setStatus("off")
				.setDescription("a tooltip");
		contButton = new Button("contButton").setAction("$input").setInputVal("0")
				.setInputType(InputType.NUM);
		widgetXml.addElement(ta);
		widgetXml.addElement(cont);

		fullXml.addElement(ta);
		fullXml.addElement(cont);
		fullXml.addElement(contButton);

	}

	@Override
	public void run() {
		// Coordinator.sendCommand(this, "0");
		// cont.setStatus("off");
		//
		// while (isRunning) {
		// Coordinator.sendCommand(this, "1");
		// cont.setStatus("on");
		// this.sleep(1000 * 30);
		// Coordinator.sendCommand(this, "0");
		// cont.setStatus("off");
		// }
		//
		// Coordinator.sendCommand(this, "0");
		// cont.setStatus("off");
		// while (isRunning) {
		// this.sleep();
		//
		// }
		while (isRunning) {
			this.sleep();
			Log.d(this.name, "Woken");
		}
	}

	@Override
	public boolean receiveCommand(String command) {
		boolean goodCommand = false;

		if (command != null) {
			if (command.equals("1")) {
				Log.d(this.name, "Received request to turn on motor");
				Coordinator.sendCommand(this, "1");
				cont.setStatus("on");
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

}
