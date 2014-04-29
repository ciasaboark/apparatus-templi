package org.apparatus_templi;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.event.MotionEvent;
import org.apparatus_templi.service.EmailService;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.XmlFormatter;

public final class EmailTester extends Driver implements org.apparatus_templi.EventGenerator,
		org.apparatus_templi.EventWatcher {
	EmailService emailService = EmailService.getInstance();
	String recipients;

	public EmailTester() {
		this.name = "EmailTest";
		recipients = Coordinator.readTextData(this.name, "recipt");
		if (recipients == null) {
			recipients = "";
		}
	}

	@Override
	public void run() {
		Coordinator.registerEventWatch(this, new MotionEvent());

		while (isRunning) {

			boolean emailSent = emailService.sendEmailMessage(recipients,
					"Notification from Driver: " + this.name,
					"Time: <pre>" + System.currentTimeMillis() + "</pre>&deg;");
			if (emailSent) {
				Log.d(this.name, "email sent");
			} else {
				Log.w(this.name, "email not sent");
			}
			this.sleep(1000 * 60 * 5);
		}
	}

	@Override
	public boolean receiveCommand(String command) {
		if (command != null) {
			if (command.startsWith("recip")) {
				Log.d(this.name, "got new recipt list: " + command);
				Coordinator.storeTextData(this.name, "recipt", command.substring(5));
				recipients = command.substring(5);
			}
		}
		return true;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getWidgetXML() {
		XmlFormatter xml = new XmlFormatter(this, "Email Tester");
		xml.addElement(new Button("Save Recipient List").setAction("recip$input")
				.setInputType(InputType.TEXT).setIcon("fa fa-group")
				.setDescription("A comma separated list of recipients to email every 30 minutes")
				.setInputVal(recipients));
		return xml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receiveEvent(Event e) {
	}

	@Override
	public ArrayList<Event> getEventTypes() {
		return null;
	}

}
