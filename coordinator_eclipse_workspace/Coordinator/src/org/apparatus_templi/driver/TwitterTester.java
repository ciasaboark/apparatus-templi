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

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventWatcher;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.TempChangedEvent;
import org.apparatus_templi.service.TwitterService;

public final class TwitterTester extends Driver implements EventWatcher {
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
