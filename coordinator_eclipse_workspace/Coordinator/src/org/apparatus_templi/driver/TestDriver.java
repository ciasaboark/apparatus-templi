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
import org.apparatus_templi.Log;

public class TestDriver extends Driver {

	public TestDriver() {
		this.name = "test";

	}

	@Override
	public void run() {
		while (isRunning) {
			Log.d(this.name, "Sending command");
			Coordinator.sendCommand(this, "123");
			this.sleep(5000);
			Log.d(this.name, "Sleeping");
		}

	}

	@Override
	public boolean receiveCommand(String command) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return false;
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
