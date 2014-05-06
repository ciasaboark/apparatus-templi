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

import org.apparatus_templi.Log;

/**
 * A blank driver to be used as a template for a sensor module.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */

public class BlankSensorDriver extends Driver {

	/*
	 * Make sure to give each driver a unique name. The length should be 10 characters or less. If
	 * it is more then the Coordinator will not load the driver. For the same of simplicity, this
	 * should also be the name assigned to the remote module that pairs with this driver.
	 */
	public BlankSensorDriver() {
		this.name = "blank";
	}

	/*
	 * This is the starting point of execution for your driver. Do not call run() from within your
	 * constructor, it will be called by the Coordinator after querying for remote modules. If your
	 * driver should be active all the time then place all of your code within the while loop. When
	 * its time for the server to go down Coordinator will set running to be false, so any final
	 * cleanup should be done after the while loop. Note that each driver can have commands and
	 * binary data queued for processing. If you do not want to process the data, then you should at
	 * least clear the queue
	 */
	@Override
	public void run() {
		while (isRunning) {
			/*
			 * First check for any commands or binary data that has been queued for processing. For
			 * simplicities sake we just pass along the data as if it came from a front end.
			 */
			if (!this.queuedCommands.isEmpty()) {
				this.receiveCommand(this.queuedCommands.poll());
			}
			if (!this.queuedBinary.isEmpty()) {
				this.receiveBinary(this.queuedBinary.poll());
			}

			// Do cool stuff
		}

		Log.d(this.name, "terminating");
	}

	/*
	 * receiveCommand() and receiveBinary() are the interfaces that the front ends use to
	 * communicate with the drivers. It is up to the driver to check that the command and data are
	 * valid before operating on them.
	 */
	@Override
	public boolean receiveCommand(String command) {
		// If the command was valid
		// Coordinator.sendCommand(this, command);
		return true;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// If the data was valid
		// Coordinator.sendBinary(this, data);
		return true;
	}

	/*
	 * The front ends receive data about each running driver as XML data. The format for the widget
	 * XML data has not yet been finalized.
	 */
	@Override
	public String getWidgetXML() {
		Log.w(name, "getWidgetXML() unimplimented");
		return null;
	}

	/*
	 * The full page XML is intended to provide a detailed interface for the front tends to interact
	 * with the drivers. The format for the full page XML has not yet been finalized.
	 */
	@Override
	public String getFullPageXML() {
		Log.w(name, "getFullPageXML() unimplimented");
		return null;
	}
}
