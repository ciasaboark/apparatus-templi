package org.apparatus_templi.driver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventGenerator;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.TempChangedEvent;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class TempMonitor extends Driver implements EventGenerator {
	private XmlFormatter widgetXml = new XmlFormatter(this, "Temperature Monitor");
	private XmlFormatter fullPageXml = new XmlFormatter(this, "Temperature Monitor");
	// private final Sensor temp = new Sensor("Downstairs Temperature");
	// private final Pre intro = new Pre("description", "");
	// private final TextArea lastUpdated = new TextArea("lastUpdated", "");
	private final String DB_KEY_LASTUPDATE = "last";
	private final int DEFAULT_REFRESH_RATE = 15 * 60;
	private final String DEFAULT_LOCATION = "";
	private Integer refreshRate = null;
	private String location = null;
	// private final Pre widgetPre = new Pre("fancy", "");
	private Integer lastKnownTemp = null;

	// private final String noContact =
	// "<div style='text-align: right;'><span style=' font-size: 100px'>"
	// + "?"
	// + "&deg;</span></div>"
	// +
	// "<div style='text-align: center;'><span style='float: right;padding-right: 50px; font-size: 15px'>"
	// + "could not contact module</span></div>";

	private void buildWidgetXml(String temp, String time) {
		String loc = (location == null) ? DEFAULT_LOCATION : location;
		int ref = ((refreshRate == null) ? DEFAULT_REFRESH_RATE : refreshRate);
		widgetXml = new XmlFormatter(this, loc + " Temperature");

		if (temp == null || time == null) {
			// set a default unknown state with a short refresh interval
			widgetXml.setRefresh(3);
			widgetXml
					.addElement(new Pre(
							"status",
							"<div style='text-align: right;'><span style=' font-size: 100px'>"
									+ "?"
									+ "&deg;</span></div>"
									+ "<div style='text-align: center;'><span style='float: right;padding-right: 50px; font-size: 15px'>"
									+ "no reading</span></div>"));
		} else {
			widgetXml.setRefresh(ref);
			String date = "";
			try {
				// Date d = new java.util.Date(Long.parseLong(time));
				// SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
				date = new SimpleDateFormat().format(Long.parseLong(time));
			} catch (IllegalArgumentException e) {
				date = "unknown";
			}
			widgetXml
					.addElement(new Pre(
							"status",
							"<div style='text-align: right;'><span style=' font-size: 100px'>"
									+ temp
									+ "&deg;</span></div>"
									+ "<div style='text-align: center;'><span style='float: right;padding-right: 50px; font-size: 15px'>"
									+ "Last Updated: " + date + "</span></div>"));
		}
	}

	// rebuild the full page XmlFormatter
	private void buildFullPageXml() {
		int refRate = (((refreshRate == null) ? DEFAULT_REFRESH_RATE : refreshRate) / 60);
		String loc = ((location == null) ? DEFAULT_LOCATION : location);

		fullPageXml = new XmlFormatter(this, loc + " Temperature");
		fullPageXml.addElement(new TextArea("intro", loc + " Temperature"));
		// add button to set refresh rate
		fullPageXml.addElement(new TextArea("refresh descr",
				"How often the temperature should be checked (in minutes)"));
		fullPageXml.addElement(new Button("Set Refresh Rate").setAction("rr$input")
				.setInputType(InputType.NUM).setInputVal(String.valueOf(refRate))
				.setIcon("fa fa-clock-o")
				.setDescription("How often the temperature should be checked (in minutes)"));
		// spacer
		fullPageXml.addElement(new Pre("spacer", "&nbsp;"));

		// add button to set location name
		fullPageXml.addElement(new TextArea("loc descr",
				"What area of the house is the reading coming from"));
		fullPageXml.addElement(new Button("Set Location").setAction("name$input")
				.setInputType(InputType.TEXT).setInputVal(loc).setIcon("fa fa-location-arrow"));

	}

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
		// if the temperature changed then will generate an event
		if (lastKnownTemp != null && lastKnownTemp != temp) {
			TempChangedEvent e = new TempChangedEvent(System.currentTimeMillis(), this,
					lastKnownTemp, temp);
			Coordinator.receiveEvent(this, e);
		}

		// store the values in memory and to the widget
		this.lastKnownTemp = temp;

		// this.lastUpdated.setText("Last update: "
		// + DateFormat.getDateTimeInstance().format(new Date(time)));
		// this.temp.setValue(String.valueOf(temp));
		// this.widgetPre
		// .setHtml("<div style='text-align: right;'><span style=' font-size: 100px'>"
		// + this.temp.getValue()
		// + "&deg;</span></div>"
		// +
		// "<div style='text-align: center;'><span style='float: right;padding-right: 50px; font-size: 15px'>"
		// + lastUpdated.getText() + "</span></div>");

		// write the new temp data to the database
		Coordinator.storeTextData(this.name, String.valueOf(time), String.valueOf(temp));
		Coordinator.storeTextData(this.name, DB_KEY_LASTUPDATE, String.valueOf(time));
	}

	private Integer readRemoteTemp() {
		Log.d(this.name, "readRemoteTemp()");
		Integer temp = null;
		String response = Coordinator.sendCommandAndWait(this, "t", 6);
		if (response != null) {
			// if the response was not formatted like a temperature reading then pass the data to
			// receiveCommand
			if (response.startsWith("t")) {
				try {
					temp = Integer.parseInt(response.substring(1));

					updateTemp(System.currentTimeMillis(), temp);
				} catch (NumberFormatException e) {
					Log.w(this.name, "received a malformed temperature reading, discarding");
				}
			} else {
				// for some reason we received a response with a different format than what we were
				// expecting
				receiveCommand(response);
			}
		}
		return temp;
	}

	private void getBestReading() {
		// try to get an updated temperature reading
		Integer curTemp = readRemoteTemp();
		if (curTemp != null) {
			updateTemp(System.currentTimeMillis(), curTemp);
			buildWidgetXml(String.valueOf(curTemp), String.valueOf(System.currentTimeMillis()));
		} else {
			Map<String, String> lastTemp = getLastReading();
			if (lastTemp.get("temp") == null || lastTemp.get("time") == null) {
				// no current reading, and no historical reading
				buildWidgetXml(null, null);
			} else {
				try {
					lastKnownTemp = Integer.valueOf(lastTemp.get("temp"));
				} catch (NumberFormatException e) {
					// just leave the current temp as null (or whatever it currently is)
				}

				// display the temperature
				buildWidgetXml(lastTemp.get("temp"), lastTemp.get("time"));
			}
		}
	}

	public TempMonitor() {
		this.name = "TempMonitr";
		// intro.setHtml("<p>Downstairs temperature reading</p>");
		// temp.setValue("?");
		// lastUpdated.setText("unknown");

		location = Coordinator.readTextData(this.name, "location");
		String r = Coordinator.readTextData(this.name, "refresh");
		try {
			refreshRate = Integer.parseInt(r);
		} catch (NumberFormatException e) {
			// if the stored value was bad we just use the default
		}

		buildWidgetXml(null, null);
		buildFullPageXml();

	}

	@Override
	public void run() {

		while (isRunning) {
			getBestReading();

			// wake every 15 minutes to record temperature data
			this.sleep(((refreshRate == null) ? DEFAULT_REFRESH_RATE : refreshRate) * 1000);
			Log.d(this.name, "waking...");
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
	public boolean receiveCommand(String command) {
		boolean goodCommand = false;
		if (command != null) {
			if (command.startsWith("t")) {
				try {
					Integer i = Integer.parseInt(command.substring(1));
					updateTemp(System.currentTimeMillis(), i);
					goodCommand = true;
				} catch (NumberFormatException e) {
					Log.w(this.name, "received malformed temperature reading, discarding");
				}
			} else if ("r".equals(command)) {
				// received request to update temperature reading
				goodCommand = true;
				getBestReading();
			} else if (command.startsWith("rr")) {
				// received request to set new refresh rate
				String rate = "";
				try {
					Integer i = Integer.parseInt(command.substring(2)) * 60;
					if (i <= 0) {
						throw new NumberFormatException("negative values not allowed");
					}
					rate = String.valueOf(i);
					Log.d(this.name, "received request to set new refresh rate");
					try {
						refreshRate = Integer.parseInt(rate);
						goodCommand = true;
					} catch (NumberFormatException e) {
						// use the default value
					}
					Coordinator.storeTextData(this.name, "refresh", rate);
					Coordinator.wakeSelf(this);
					buildFullPageXml();
					// getBestReading();
				} catch (NumberFormatException e) {

				}
			} else if (command.startsWith("name")) {
				Log.d(this.name, "received request to set new location name");
				location = command.substring(4);

				Coordinator.storeTextData(this.name, "location", command.substring(4));
				buildFullPageXml();
				// getBestReading();
				goodCommand = true;
			} else {
				Log.w(this.name, "received command in unknown format");
			}
		}
		return goodCommand;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		Log.w(this.name, "received binary data, discarding");
		return true;
	}

	@Override
	public String getWidgetXML() {
		// getBestReading();
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullPageXml.generateXml();
	}

}
