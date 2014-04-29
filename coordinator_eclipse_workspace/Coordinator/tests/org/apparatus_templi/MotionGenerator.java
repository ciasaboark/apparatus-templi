package org.apparatus_templi;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.event.MotionEvent;

/**
 * A sample driver that generates simulated motion tracker events every few seconds.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class MotionGenerator extends Driver implements EventGenerator {

	public MotionGenerator() {
		this.name = "MOT_GEN";
	}

	@Override
	public void run() {
		Thread.currentThread().setName("Message Center");
		Log.d(this.name, "starting");
		this.queuedBinary.clear();
		this.queuedCommands.clear();

		while (isRunning) {
			// simulate motion events every few seconds
			this.sleep(5000);
			Log.d(this.name, "generating new motion event");
			MotionEvent e = new MotionEvent(System.currentTimeMillis(), this);
			Coordinator.receiveEvent(this, e);
		}
		Log.d(this.name, "terminating");
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
	public ArrayList<Event> getEventTypes() {
		ArrayList<Event> list = new ArrayList<Event>();
		list.add(new MotionEvent());
		return list;
	}
}
