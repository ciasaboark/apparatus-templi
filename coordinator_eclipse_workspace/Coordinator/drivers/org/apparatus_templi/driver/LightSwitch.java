package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;
import org.apparatus_templi.event.MotionEvent;

public class LightSwitch extends Driver {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Light Switch");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Light Switch");
	private final TextArea description = new TextArea("descr", "Long description");
	private final Button button = new Button("switch");
	private final Controller light_switch = new Controller("switch");

	public LightSwitch() {
		this.name = "light switch";
		widgetXml.addElement(description);
		widgetXml.addElement(light_switch);
		widgetXml.addElement(button);

		fullXml.addElement(description);
		fullXml.addElement(button);

		button.setAction("toggle").setDescription("toggle the light switch")
				.setInputType(InputType.NONE);
		light_switch.setStatus("off");
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
				light_switch.setStatus("ON");
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
