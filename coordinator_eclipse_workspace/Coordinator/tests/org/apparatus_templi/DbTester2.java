package org.apparatus_templi;

import java.util.Arrays;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class DbTester2 extends Driver {
	private final XmlFormatter widgetXml;
	private final TextArea description = new TextArea("status", "");

	public DbTester2() {
		this.name = "DB_TESTER2";
		widgetXml = new XmlFormatter(this, "Database Tester2");
		widgetXml.setRefresh(9);
		widgetXml.addElement(description);

	}

	@Override
	public void run() {
		while (isRunning) {
			description.setText("Saving and reading 1000 values");
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
			description.setText("Sleeping...");
			this.sleep(1000 * 60 * 5);
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
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
