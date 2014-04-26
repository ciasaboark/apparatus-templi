package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class TestDriver extends Driver {

	public TestDriver() {
		this.name = "test";

	}

	@Override
	public void run() {
		while (isRunning) {
			Log.d(this.name, "Sending command");
			Coordinator.sendCommand(this, "123");
			this.sleep(5000);
			Log.d(this.name, "Sleeping");
		}

	}

	@Override
	public boolean receiveCommand(String command) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return false;
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
