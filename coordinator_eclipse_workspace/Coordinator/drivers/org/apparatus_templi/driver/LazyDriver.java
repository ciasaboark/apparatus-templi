package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class LazyDriver extends SensorModule {
	public LazyDriver() {
		this.name = "LAZY";
	}
	
	@Override
	public void run() {
		Log.d(this.name, "waking");
		//we don't care about incoming binary data, just discard any queued
		this.queuedBinary.clear();
		
		while (isRunning) {
			//check for queued messages
			while (!this.queuedCommands.isEmpty()) {
				String cmd = this.queuedCommands.pop();
				Log.d(this.name, "working on command: " + cmd);
				this.receiveCommand(cmd);
				Log.d(this.name, "received incoming data: '" + cmd + "'");
			}
			
			//Sleep until an incoming message wakes us up.
			this.sleep();
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
	public void receiveCommand(String command) {
		Coordinator.storeTextData(this.name, ((Long)System.currentTimeMillis()).toString(), command);

	}

	@Override
	public void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

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