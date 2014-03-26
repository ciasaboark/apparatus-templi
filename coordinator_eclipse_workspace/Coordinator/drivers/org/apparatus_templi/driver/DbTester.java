package org.apparatus_templi.driver;

import java.util.ArrayList;
import java.util.Arrays;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.XmlFormatter;

public class DbTester extends ControllerModule {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Database Tester");
	private final Pre status = new Pre("test pre", "");

	public DbTester() {
		this.name = "DB_TESTER";
		widgetXml.setRefresh(15);
		widgetXml.addElement(status);

	}

	@Override
	public void run() {
		while (isRunning) {
			status.setHtml("<p>Saving and reading back 1000 values to the database</p><p style='font-family: \"fantasy\"'>Sit back, relax and have a &nbsp;<i class=\"fa fa-beer\"></i>, this could take a while.</p>");
			for (int i = 1; i <= 1000; i++) {
				String data = String.valueOf(System.currentTimeMillis());
				byte[] binData = data.getBytes();
				Log.d(this.name, "Test " + i + " of 1000");
				int code = Coordinator.storeTextData(this.name, "txt", data);
				if (code == -1) {
					Log.d(this.name, "data overwritten");
				} else if (code == 0) {
					Log.d(this.name, "data could not be written");
				} else if (code == 1) {
					Log.d(this.name, "new data stored");
				} else {
					Log.e(this.name, "unknown code returned");
				}
				String storedData = Coordinator.readTextData(this.name, "txt");
				if (data.equals(storedData)) {
					Log.d(this.name, "text data stored and retrieved correctly");
				} else {
					Log.e(this.name, "text data was not stored correctly");
				}

				code = Coordinator.storeBinData(this.name, "bin", binData);
				if (code == -1) {
					Log.d(this.name, "data overwritten");
				} else if (code == 0) {
					Log.d(this.name, "data could not be written");
				} else if (code == 1) {
					Log.d(this.name, "new data stored");
				} else {
					Log.e(this.name, "unknown code returned");
				}
				byte[] storedBin = Coordinator.readBinData(this.name, "bin");
				if (Arrays.equals(binData, storedBin)) {
					Log.d(this.name, "binary data stored and retrieved correctly");
				} else {
					Log.e(this.name, "binary data was not stored correctly");
				}

				if (!isRunning) {
					break;
				}
			}
			status.setHtml("<p>Sleeping...<p>");
			this.sleep(1000 * 60 * 5);
		}
		Log.d(this.name, "terminating");

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
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
