package org.apparatus_templi;

import java.util.ArrayList;

/**
 * A sample driver that registers itself to watch for motion
 * events
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 *
 */
public class MotionWatcher extends SensorModule implements EventWatcher {
	public MotionWatcher() {
		this.name = "MOT_WATCH";
	}
	
	@Override
	public void run() {
		Coordinator.registerEventWatch(this, new MotionEvent());
		while (isRunning) {
			this.sleep();
		}

	}

	@Override
	public void receiveEvent(Event e) {
		if (e instanceof MotionEvent) {
			Log.d(this.name, "motion was detected by module " + e.getOrigin().getName());
			Coordinator.storeTextData(this.name, String.valueOf(e.getTimestamp()), "motion detected");
		}

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

}
