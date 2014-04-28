package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.event.MotionEvent;

public class MotionWatcher extends Driver implements EventGenerator {

	public MotionWatcher() {
		this.name = "LOCAL";
	}

	@Override
	public void run() {
		while (isRunning) {
			this.sleep();
		}

	}

	@Override
	public ArrayList<Event> getEventTypes() {
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(new MotionEvent());
		return events;
	}

	@Override
	public boolean receiveCommand(String command) {
		if (command != null && command.equals("mot")) {
			MotionEvent e = new MotionEvent(System.currentTimeMillis(), this);
			Coordinator.receiveEvent(this, e);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
