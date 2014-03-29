package org.apparatus_templi.driver;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.TempChangedEvent;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.Sensor;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class TempMonitor extends SensorModule implements EventGenerator {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Temperature Monitor");
	private final XmlFormatter fullPageXml = new XmlFormatter(this, "Temperature Monitor");
	private final Sensor temp = new Sensor("Downstairs Temperature");
	private final Pre intro = new Pre("description", "");
	private final TextArea lastUpdated = new TextArea("lastUpdated", "");
	private final Button refreshButton = new Button("Refresh");
	private final String DB_KEY_LASTUPDATE = "last";

	private Integer lastKnownTemp = null;

	private Map<String, String> getLastReading() {
		Log.d(this.name, "getLastReading()");
		Map<String, String> m = new HashMap<String, String>();
		m.put("time", null);
		m.put("temp", null);
		String latestEntry = Coordinator.readTextData(this.name, DB_KEY_LASTUPDATE);
		if (latestEntry != null) {
			String reading = Coordinator.readTextData(this.name, latestEntry);
			if (reading != null) {
				m.put("time", latestEntry);
				m.put("temp", reading);
			}
		}
		return m;
	}

	private void updateTemp(long time, int temp) {
		Log.d(this.name, "updateTemp()");
		// store the values in memory and to the widget
		this.lastKnownTemp = temp;

		this.lastUpdated.setText("Last update: "
				+ DateFormat.getDateTimeInstance().format(new Date(time)));
		this.temp.setValue(String.valueOf(temp));

		// write the new temp data to the database
		Coordinator.storeTextData(this.name, String.valueOf(time), String.valueOf(temp));
		Coordinator.storeTextData(this.name, DB_KEY_LASTUPDATE, String.valueOf(time));
	}

	private boolean readRemoteTemp() {
		Log.d(this.name, "readRemoteTemp()");
		boolean isTempUpdated = false;
		String response = Coordinator.sendCommandAndWait(this, "t", 6);
		if (response != null) {
			// if the response was not formatted like a temperature reading then pass the data to
			// receiveCommand
			if (response.startsWith("t")) {
				try {
					Integer i = Integer.parseInt(response.substring(1));
					// if the temperature changed then will generate an event
					if (lastKnownTemp != null && lastKnownTemp != i) {
						TempChangedEvent e = new TempChangedEvent(System.currentTimeMillis(), this,
								lastKnownTemp, i);
						Coordinator.receiveEvent(this, e);
					}
					updateTemp(System.currentTimeMillis(), i);
					isTempUpdated = true;
				} catch (NumberFormatException e) {
					Log.w(this.name, "received a malformed temperature reading, discarding");
				}
			} else {
				// for some reason we received a response with a different format than what we were
				// expecting
				receiveCommand(response);
			}
		}
		return isTempUpdated;
	}

	public TempMonitor() {
		this.name = "TempMonitr";
		intro.setHtml("<p>Downstairs temperature reading</p>");
		temp.setValue("unknown");
		refreshButton.setAction("r");
		refreshButton.setInputType("none");
		// temp.setIcon(icon)
		lastUpdated.setText("Last updated: unknown");
		widgetXml.addElement(intro);
		widgetXml.addElement(temp);
		widgetXml.addElement(lastUpdated);
		widgetXml.addElement(refreshButton);

	}

	@Override
	public void run() {
		// try to get an updated temperature reading
		if (readRemoteTemp()) {
		} else {
			// if we got no response from the remote module then we can try looking at the database.
			// If the last temperature record is too old then we will leave the temp Sensor value as
			// "unknown"
			Log.d(this.name, "could not get current reading during setup, reading from database");
			Map<String, String> m = getLastReading();
			if (m.get("time") != null && m.get("temp") != null) {
				try {
					Long l = Long.parseLong(m.get("time"));
					Integer i = Integer.parseInt(m.get("temp"));
					// if the last reading was longer than 4 hours ago then it will not be used
					if (l >= (System.currentTimeMillis() - (1000 * 60 * 60 * 4))) {
						lastUpdated.setText("Last update: "
								+ DateFormat.getDateTimeInstance().format(new Date(l)));
						temp.setValue(m.get("temp"));
					} else {
						Log.w(this.name,
								"last known reading was longer than 4 hours ago, discarding");
					}
				} catch (NumberFormatException e) {
					Log.w(this.name, "Database has a bad value for last recorded reading"
							+ " timestamp, or last stored temperature was invalid");
				}
			} else {
				Log.w(this.name, "database had no recorded temperatures");
			}
		}

		while (isRunning) {
			// wake every 15 minutes to record temperature data
			this.sleep(1000 * 60 * 15);
			readRemoteTemp();
		}

		Log.d(this.name, "terminating");

	}

	@Override
	public ArrayList<Event> getEventTypes() {
		ArrayList<Event> a = new ArrayList<Event>();
		a.add(new TempChangedEvent());
		return a;
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
		if (command != null) {
			if (command.startsWith("t")) {
				try {
					Integer i = Integer.parseInt(command.substring(1));
					updateTemp(System.currentTimeMillis(), i);
				} catch (NumberFormatException e) {
					Log.w(this.name, "received malformed temperature reading, discarding");
				}
			} else if ("r".equals(command)) {
				// received request to update temperature reading
				readRemoteTemp();
			} else {
				Log.w(this.name, "received command in unknown format");
			}
		}

	}

	@Override
	public void receiveBinary(byte[] data) {
		Log.w(this.name, "received binary data, discarding");
	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullPageXml.generateXml();
	}

}
