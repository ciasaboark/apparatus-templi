package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Log;
import org.apparatus_templi.service.TwitterService;

public final class TwitterTester extends SensorModule {
	TwitterService twitterService = TwitterService.getInstance();
	
	public TwitterTester() {
		this.name = "TwtrTest";
	}

	@Override
	public void run() {
		while (isRunning) {
			boolean updatePosted = twitterService.updateTimeline(String.valueOf(System.currentTimeMillis()));
			if (updatePosted) {
				Log.d(this.name, "update posted");
			} else {
				Log.w(this.name, "update could not be posted");
			}
			this.sleep(1000 * 60 * 5);
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

}
