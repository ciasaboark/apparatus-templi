package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventWatcher;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.MotionEvent;
import org.apparatus_templi.service.EmailService;

public final class EmailTester extends Driver implements EventWatcher {
	EmailService emailService = EmailService.getInstance();

	public EmailTester() {
		this.name = "EmailTest";
	}

	@Override
	public void run() {
		Coordinator.registerEventWatch(this, new MotionEvent());
		
		while (isRunning) {
			MotionEvent e = new MotionEvent(System.currentTimeMillis(), this);
			Coordinator.receiveEvent(this, e);
			this.sleep(30000);
		}
	}

	@Override
	public boolean receiveCommand(String command) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getWidgetXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receiveEvent(Event e) {
		if (e instanceof MotionEvent) {
			boolean emailSent = emailService.sendEmailMessage("Notification from Driver: " + this.name,"Motion Event: "
					+ ((MotionEvent) e).getTimestamp());
			if (emailSent) {
				Log.d(this.name, "email sent");
			} else {
				Log.w(this.name, "email not sent");
			}
		}

	}

}
