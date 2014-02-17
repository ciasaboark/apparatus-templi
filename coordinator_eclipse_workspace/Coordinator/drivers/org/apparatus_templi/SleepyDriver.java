package org.apparatus_templi;

import java.util.ArrayList;

public class SleepyDriver extends SensorModule {
	public SleepyDriver() {
		this.name = "SLEEPY";
	}

	@Override
	public void run() {
		this.queuedBinary.clear();
		this.queuedCommands.clear();
		Log.d(this.name, "waking");
		//simulate doing some stuff, then schedule a wake up
		//+ in one minute
		String reading = Coordinator.sendCommandAndWait(this, "read temp", 6);
		
		//all instance data will be lost when the driver is restarted,
		//+ so we ask the coordinator to log the reading (if we got a response)
		if (reading != null) {
			Coordinator.storeTextData(this.name, String.valueOf(System.currentTimeMillis()), reading);
		}
		
		Log.d(this.name, "scheduling a restart in 5 minutes");
		Coordinator.scheduleRestart(this, System.currentTimeMillis() + 1000 * 60 * 5);

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
