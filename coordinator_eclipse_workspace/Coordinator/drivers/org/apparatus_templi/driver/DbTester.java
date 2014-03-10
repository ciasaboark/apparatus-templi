package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class DbTester extends ControllerModule {

	public DbTester() {
		this.name = "DB_TESTER";
	}

	@Override
	public void run() {
		while (isRunning) {
			String data = "1234567890";
			Coordinator.storeTextData(this.name, "test", data);
			this.sleep(3000);
			String storedData = Coordinator.readTextData(this.name, "test");
			if (data.equals(storedData)) {
				Log.d(this.name, "data stored and retrieved correctly");
			} else {
				Log.d(this.name, "data was not stored correctly");
			}
			this.sleep(1000 * 10);
		}

	}

	@Override
	public ArrayList<String> getControllerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveCommand(String command) {
		// TODO Auto-generated method stub

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
