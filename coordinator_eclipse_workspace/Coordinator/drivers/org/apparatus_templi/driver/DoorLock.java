package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class DoorLock extends Driver {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Door lock");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Door Lock");
	private final TextArea description = new TextArea("descr", "Long description");
	private final Button button = new Button("control");
	private final Controller lock = new Controller("lock");

	public DoorLock() {
		setName("LOCAL");
		widgetXml.addElement(description);
		widgetXml.addElement(lock);
		widgetXml.addElement(button);

		fullXml.addElement(description);
		fullXml.addElement(button);

		button.setAction("toggle").setDescription("toggle the bolt state")
				.setInputType(InputType.NONE);
		lock.setStatus("locked");

	}

	@Override
	public void run() {

		while (!this.queuedCommands.isEmpty()) {
			this.receiveCommand(this.queuedCommands.pop());
		}

		while (this.isRunning) {
			this.sleep();
			Log.d(this.getName(), "waking");
		}

	}

	@Override
	public boolean receiveCommand(String command) {
		boolean goodCommand = false;
		if (command != null) {
			if (command.equals("toggle")) {
				Log.d(this.getName(), "toggle command received");
				Coordinator.sendCommand(this, "1");
				lock.setStatus("unlocked");
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
