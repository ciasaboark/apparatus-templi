package org.apparatus_templi;

import java.util.ArrayList;

public class LazyDriver extends SensorModule {
	public LazyDriver() {
		this.name = "LAZY";
	}
	
	@Override
	public void run() {
		Log.d(this.name, "waking");
		//we don't care about incoming binary data, just discard any queued
		this.queuedBinary.clear();
		
		//check for queued messages
		while (!this.queuedCommands.isEmpty()) {
			String cmd = this.queuedCommands.pop();
			Log.d(this.name, "working on command: " + cmd);
			this.receiveCommand(cmd);
			Log.d(this.name, "received incoming data: '" + cmd + "'");
		}
		
		Log.d(this.name, "terminating");
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
