package org.apparatus_templi;

import java.util.Arrays;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class DbTester extends Driver {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Database Tester");
	private final Pre intro = new Pre("test pre",
			"<p>Saving and reading back 1000 values to the db</p>");
	private final Pre status = new Pre("test pre", "");
	private final TextArea curCycle = new TextArea("cycle", "");
	private final Button restartTestButton = new Button("Restart Test");
	private final Button pauseTestButton = new Button("Pause Test");
	private final Button resumeTestButton = new Button("Resume Test");
	private boolean pauseTest = false;

	public DbTester() {
		this.name = "DB_TESTER";
		restartTestButton.setAction("s");
		pauseTestButton.setAction("p");
		resumeTestButton.setAction("r");
		widgetXml.setRefresh(5);
		widgetXml.addElement(intro);
		widgetXml.addElement(curCycle);
		widgetXml.addElement(status);

	}

	@Override
	public void run() {
		while (isRunning) {
			widgetXml.removeElement(restartTestButton);
			widgetXml.removeElement(resumeTestButton);
			widgetXml.addElement(pauseTestButton);
			status.setHtml("<p style='font-size: 9pt; font-family: \"Herculanum\"'>Sit back, relax and have a &nbsp;<i class=\"fa fa-beer\"></i>, this could take a while.</p>");
			for (int i = 1; i <= 1000; i++) {
				curCycle.setText("Current cycle: " + String.valueOf(i));
				String data = String.valueOf(System.currentTimeMillis());
				byte[] binData = data.getBytes();
				// Log.d(this.name, "Test " + i + " of 1000");
				int code = Coordinator.storeTextData(this.name, "txt", data);
				if (code == -1) {
					// Log.d(this.name, "data overwritten");
				} else if (code == 0) {
					// Log.d(this.name, "data could not be written");
				} else if (code == 1) {
					// Log.d(this.name, "new data stored");
				} else {
					// Log.e(this.name, "unknown code returned");
				}
				String storedData = Coordinator.readTextData(this.name, "txt");
				if (data.equals(storedData)) {
					// Log.d(this.name, "text data stored and retrieved correctly");
				} else {
					// Log.e(this.name, "text data was not stored correctly");
				}

				code = Coordinator.storeBinData(this.name, "bin", binData);
				if (code == -1) {
					// Log.d(this.name, "data overwritten");
				} else if (code == 0) {
					// Log.d(this.name, "data could not be written");
				} else if (code == 1) {
					// Log.d(this.name, "new data stored");
				} else {
					// Log.e(this.name, "unknown code returned");
				}
				byte[] storedBin = Coordinator.readBinData(this.name, "bin");
				if (Arrays.equals(binData, storedBin)) {
					// Log.d(this.name, "binary data stored and retrieved correctly");
				} else {
					// Log.e(this.name, "binary data was not stored correctly");
				}

				if (pauseTest) {
					this.sleep();
				}

				if (!isRunning) {
					break;
				}
			}
			status.setHtml("<p>Sleeping...<p>");
			widgetXml.removeElement(pauseTestButton);
			widgetXml.removeElement(resumeTestButton);
			widgetXml.addElement(restartTestButton);
			widgetXml.setName("Database Tester (sleeping)");
			this.sleep(1000 * 60 * 5);
		}
		Log.d(this.name, "terminating");

	}

	@Override
	public boolean receiveCommand(String command) {
		if ("s".equals(command)) {
			Log.d(this.name, "received restart command, waking self");
			widgetXml.setName("Database Tester");
			Coordinator.wakeSelf(this);
		} else if ("p".endsWith(command)) {
			Log.d(this.name, "received pause command");
			widgetXml.removeElement(pauseTestButton);
			widgetXml.addElement(resumeTestButton);
			widgetXml.setName("Database Tester (paused)");
			this.pauseTest = true;
		} else if ("r".endsWith(command)) {
			Log.d(this.name, "received resume command");
			widgetXml.removeElement(resumeTestButton);
			widgetXml.addElement(pauseTestButton);
			widgetXml.setName("Database Tester");
			this.pauseTest = false;
			Coordinator.wakeSelf(this);
		}
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
