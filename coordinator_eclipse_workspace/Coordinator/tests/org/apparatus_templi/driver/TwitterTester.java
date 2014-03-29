package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventWatcher;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.TempChangedEvent;
import org.apparatus_templi.service.TwitterService;

public final class TwitterTester extends SensorModule implements EventWatcher {
	TwitterService twitterService = TwitterService.getInstance();

	public TwitterTester() {
		this.name = "TwtrTest";
	}

	@Override
	public void run() {
		Coordinator.registerEventWatch(this, new TempChangedEvent());

		while (isRunning) {
			// boolean updatePosted =
			// twitterService.updateTimeline(String.valueOf(System.currentTimeMillis()));
			// if (updatePosted) {
			// Log.d(this.name, "update posted");
			// } else {
			// Log.w(this.name, "update could not be posted");
			// }
			this.sleep();
		}

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

	@Override
	public void receiveEvent(Event e) {
		if (e instanceof TempChangedEvent) {
			boolean updatePosted = twitterService.updateTimeline("Temp changed from "
					+ ((TempChangedEvent) e).getPrevTemp() + " to "
					+ ((TempChangedEvent) e).getTemp());
			if (updatePosted) {
				Log.d(this.name, "update posted");
			} else {
				Log.w(this.name, "update not posted");
			}
		}

	}

}
