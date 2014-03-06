package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Log;
import org.apparatus_templi.XmlFormatter;

public class XmlTest extends ControllerModule {
	private XmlFormatter widgetXml;
	private XmlFormatter.Sensor sensor1;
	private XmlFormatter.Controller controller1;
	private XmlFormatter.Button sensor1Button;
	private XmlFormatter.Button controller1Button;

	public XmlTest() {
		this.name = "XmlTest";
		widgetXml = new XmlFormatter(this, "Xml Formatter");
		
		sensor1 = new XmlFormatter.Sensor("Temperature");
		sensor1.setValue("unknown");
		controller1 = new XmlFormatter.Controller("Some Controller");
		controller1.setStatus("waiting");
		sensor1Button = new XmlFormatter.Button("Refresh");
		sensor1Button.setAction("r");
		sensor1Button.setInputType(XmlFormatter.InputType.NONE);
		
		controller1Button = new XmlFormatter.Button("Turn");
		controller1Button.setAction("m$input");
		controller1Button.setInputType(XmlFormatter.InputType.NUM);
		
		widgetXml.addElement(sensor1);
		widgetXml.addElement(sensor1Button);
		widgetXml.addElement(controller1);
		widgetXml.addElement(controller1Button);
		
		
	}

	@Override
	public void run() {
		while (!this.queuedCommands.isEmpty()) {
			receiveCommand(queuedCommands.poll());
		}
		while (isRunning) {
			this.sleep(10000);
			Integer i;
			try {
				i = Integer.parseInt(sensor1.getValue());
				i++;
				sensor1.setValue(String.valueOf(i));
			} catch (NumberFormatException e) {
				sensor1.setValue("1");
			}
		}

	}

	@Override
	public void receiveCommand(String command) {
		Log.d(this.name, "received command '" + command + "'");

	}

	@Override
	public void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWidgetXML() {
		String xml = widgetXml.generateWidgetXml();
		return xml;
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
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

}
