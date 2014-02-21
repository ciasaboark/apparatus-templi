package org.apparatus_templi;

import java.util.ArrayList;


/**
 * A sample driver that generates simulated motion tracker events every
 * few seconds.
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class MotionGenerator extends SensorModule implements EventGenerator {

	public MotionGenerator() {
		this.name = "MOT_GEN";
	}
	
	@Override
	public void run() {
		Log.d(this.name, "starting");
		this.queuedBinary.clear();
		this.queuedCommands.clear();

		while (isRunning) {
			//simulate motion events every few seconds
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(this.name, "generating new motion event");
			MotionEvent e = new MotionEvent(System.currentTimeMillis(), this);
			Coordinator.receiveEvent(this, e);
		}
		Log.d(this.name, "terminating");
	}

	@Override
	void receiveCommand(String command) {
		// TODO Auto-generated method stub

	}

	@Override
	void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	String getWidgetXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event generateEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Event> getEventTypes() {
		ArrayList<Event> list = new ArrayList<Event>();
		list.add(new MotionEvent());
		return list;
	}

	@Override
	public ArrayList<String> getSensorList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSensorData(String sensorName) {
		// TODO Auto-generated method stub
		return null;
	}

}
