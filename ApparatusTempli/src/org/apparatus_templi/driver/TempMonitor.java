/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.apparatus_templi.service.EmailService;
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
	private Long lastKnownTime = null;
	private Integer percentChangeRequired = null;
	private Integer maxTemp = null;
	private Integer minTemp = null;

	private String emailList;

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
			// Gradually decrease the opacity of the temperature and increase the red of the
			// timestamp as the temperature reading ages

			String opacity = "1.0";
			String dateColor = "";
			float age = System.currentTimeMillis() - Float.parseFloat(time);
			if (age >= 1000 * 60 * 30) { // last reading was more than 30 min ago
				opacity = "0.7";
				dateColor = "rgb(251, 216, 216)";
			}
			if (age >= 1000 * 60 * 60 * 2) { // last reading was more than 2 hrs ago
				opacity = "0.5";
				dateColor = "rgb(247, 191, 191)";
			}
			if (age >= 1000 * 60 * 60 * 6) { // last reading was more than 6 hrs ago
				opacity = "0.3";
				dateColor = "rgb(247, 152, 152)";
			}
			if (age >= 1000 * 60 * 60 * 12) { // last reading was more than 12 hrs ago
				opacity = "0.2";
				dateColor = "rgb(247, 122, 122)";
			}
			widgetXml.setRefresh(ref);
			String date = "";
			try {
				// Date d = new java.util.Date(Long.parseLong(time));
				// SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
				date = new SimpleDateFormat().format(Long.parseLong(time));
			} catch (IllegalArgumentException e) {
				date = "unknown";
			}
			widgetXml.addElement(new Pre("status",
					"<div style='text-align: right;'><span style='opacity: " + opacity
							+ "; font-size: 100px'>" + temp + "&deg;</span></div>"
							+ "<div style='text-align: center;'><span style='color: " + dateColor
							+ ";float: right;padding-right: 50px; font-size: 15px'>"
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
		fullPageXml.addElement(new Pre("spacer", "<p>&nbsp;</p>"));

		// add button to set location name
		fullPageXml.addElement(new TextArea("loc descr",
				"What area of the house is the reading coming from"));
		fullPageXml.addElement(new Button("Set Location").setAction("name$input")
				.setInputType(InputType.TEXT).setInputVal(loc).setIcon("fa fa-location-arrow"));

		// spacer
		fullPageXml.addElement(new Pre("spacer", "<p>&nbsp;</p>"));
		fullPageXml
				.addElement(new Pre(
						"notifications",
						"<h3>Notifications</h3><p>If you wish to be notified of temperature change "
								+ "events add your email to the list below, and choose which criteria should "
								+ "be matched before a notification is sent.</p>"));

		fullPageXml.addElement(new Pre("email list descr",
				"<p>A comma separated list of email addresses to send notifications to.</p>"));
		fullPageXml
				.addElement(new Button("Update Email List")
						.setAction("email$input")
						.setIcon("fa fa-envelope")
						.setDescription(
								"A comma separated list of email addresses to notify when the temperature changes")
						.setInputType(InputType.TEXT).setInputVal(emailList));
		// percent change
		fullPageXml
				.addElement(new Pre(
						"Percent change descr",
						"<p>Send a notification if the temperature change exceeds this percent (i.e. a change of temperature "
								+ "from 72  to 75 is a 4% change)</p>"));
		fullPageXml.addElement(new Button("Set required percent change").setAction("pc$input")
				.setIcon("fa fa-tachometer")
				.setDescription("The percent change required before an email is sent.")
				.setInputType(InputType.NUM).setInputVal(String.valueOf(percentChangeRequired)));

		// max temperature exceeded
		fullPageXml.addElement(new Pre("max temp descr",
				"<p>Send a notification if the temperature exceeds this value (in &deg;F).</p>"));
		fullPageXml.addElement(new Button("Set maximum temperature").setAction("max$input")
				.setIcon("fa fa-toggle-up").setDescription("The maximum temperature allowed.")
				.setInputType(InputType.NUM)
				.setInputVal((maxTemp == 9999) ? "" : String.valueOf(maxTemp)));

		// min temperature exceeded
		fullPageXml.addElement(new TextArea("min temp descr",
				"Send a notification if the temperature falls below this value (in F)."));
		fullPageXml.addElement(new Button("Set minimum temperature").setAction("min$input")
				.setIcon("fa fa-toggle-down").setDescription("The minimum temperature allowed.")
				.setInputType(InputType.NUM)
				.setInputVal((minTemp == -9999) ? "" : String.valueOf(minTemp)));
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
		// if the temperature changed then will generate an event, and optionally send an email if
		// the percent change is great enough
		if (lastKnownTemp != null && lastKnownTemp != temp) {
			TempChangedEvent e = new TempChangedEvent(System.currentTimeMillis(), this,
					lastKnownTemp, temp);
			Coordinator.receiveEvent(this, e);
			double pc = ((((double) temp / (double) lastKnownTemp) * 100) - 100);
			if (emailList != null && !emailList.equals("")) {
				if (percentChangeRequired != null && Math.abs(pc) >= percentChangeRequired) {
					// TODO generate a unique link for every address in the email list for auto
					// unsubscribe
					sendEmail(temp, time);
				}
			}
		}
		if (temp > maxTemp || temp < minTemp) {
			sendEmail(temp, time);
		}

		// store the values in memory and to the widget
		this.lastKnownTemp = temp;
		this.lastKnownTime = time;

		// write the new temp data to the database
		Coordinator.storeTextData(this.name, String.valueOf(time), String.valueOf(temp));
		Coordinator.storeTextData(this.name, DB_KEY_LASTUPDATE, String.valueOf(time));
	}

	private void sendEmail(int temp, long time) {
		if (lastKnownTemp != null) {
			StringBuilder reason = new StringBuilder();
			reason.append("<p>This is an automated message from the Apparatus Templi home "
					+ "automation system.</p>");
			reason.append("<p>This email was sent because a user at this email address requested "
					+ "to be notified of a temperature change event.</p>");
			reason.append("<p>Current temperature is " + temp
					+ " &deg;F, the last known temperature was " + lastKnownTemp + " &deg;F.</p>");
			reason.append("<p>This email was generated because of one of the following reasons:</p><ul>");
			double pc = ((((double) temp / (double) lastKnownTemp) * 100) - 100);
			if (Math.abs(pc) >= percentChangeRequired) {
				reason.append("<li>Percent change exceeded:  a change of " + pc + "%.</li>");
			}
			if (temp > maxTemp) {
				reason.append("<li>Maximum temperature of (" + maxTemp + ") exceeded.</li>");
			}
			if (temp < minTemp) {
				reason.append("<li>Minimum temperature of (" + minTemp + ") exceeded.</li>");
			}
			reason.append("</ul>");
			reason.append("<p>If you no longer want to be notified of these events please change your "
					+ "settings by visiting: <a href=\""
					+ Coordinator.getServerAddress()
					+ "\">"
					+ Coordinator.getServerAddress() + "</a>.");

			EmailService.getInstance().sendEmailMessage(emailList, "Temperature Change",
					reason.toString());
		}
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

					// updateTemp(System.currentTimeMillis(), temp);
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

		try {
			percentChangeRequired = Integer.parseInt(Coordinator.readTextData(this.name,
					"changeRequired"));
		} catch (NumberFormatException e) {
			Log.w(this.name, "error reading percent change required from database");
			percentChangeRequired = 1001;
		}

		try {
			maxTemp = Integer.parseInt(Coordinator.readTextData(this.name, "maxTemp"));
		} catch (NumberFormatException e) {
			Log.w(this.name, "error reading max temp from database");
			maxTemp = 9999; // a default value that will (hopefully) never be reached
		}

		try {
			minTemp = Integer.parseInt(Coordinator.readTextData(this.name, "minTemp"));
		} catch (NumberFormatException e) {
			Log.w(this.name, "error reading min temp from database");
			minTemp = -9999; // a default value that will (hopefully) never be reached
		}

		emailList = Coordinator.readTextData(this.name, "emailList");
		if (emailList == null) {
			emailList = "";
		}

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

			// sleep until its time to refresh the temperature data again
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
				} catch (NumberFormatException e) {

				}
			} else if (command.startsWith("name")) {
				Log.d(this.name, "received request to set new location name");
				location = command.substring(4);

				Coordinator.storeTextData(this.name, "location", command.substring(4));
				goodCommand = true;
			} else if (command.startsWith("email")) {
				emailList = command.substring(5);
				Log.d(this.name, "received new email list: " + emailList);
				Coordinator.storeTextData(this.name, "emailList", emailList);
				goodCommand = true;
			} else if (command.startsWith("pc")) {
				try {
					percentChangeRequired = Integer.parseInt(command.substring(2));
					Coordinator.storeTextData(this.name, "changeRequired",
							String.valueOf(percentChangeRequired));
					goodCommand = true;
				} catch (NumberFormatException e) {
					Log.w(this.name, "received malformatted percent change request, ignoring");
				}
			} else if (command.startsWith("max")) {
				try {
					maxTemp = Integer.parseInt(command.substring(3));
					Coordinator.storeTextData(this.name, "maxTemp", String.valueOf(maxTemp));
					// buildFullPageXml();
					goodCommand = true;
				} catch (NumberFormatException e) {
					Log.w(this.name, "received malformatted max temp request, ignoring");
				}
			} else if (command.startsWith("min")) {
				try {
					minTemp = Integer.parseInt(command.substring(3));
					Coordinator.storeTextData(this.name, "minTemp", String.valueOf(maxTemp));
					// buildFullPageXml();
					goodCommand = true;
				} catch (NumberFormatException e) {
					Log.w(this.name, "received malformatted min temp request, ignoring");
				}
			} else {
				Log.w(this.name, "received command in unknown format");
			}
		}

		if (goodCommand) {
			buildWidgetXml(lastKnownTemp == null ? null : String.valueOf(lastKnownTemp),
					lastKnownTime == null ? null : String.valueOf(lastKnownTime));
			buildFullPageXml();
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