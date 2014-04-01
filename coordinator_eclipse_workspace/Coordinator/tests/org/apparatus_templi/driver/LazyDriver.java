package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class LazyDriver extends Driver {
	public LazyDriver() {
		this.name = "LAZY";
	}

	@Override
	public void run() {
		Log.d(this.name, "waking");
		// we don't care about incoming binary data, just discard any queued
		this.queuedBinary.clear();

		while (isRunning) {
			// check for queued messages
			while (!this.queuedCommands.isEmpty()) {
				String cmd = this.queuedCommands.pop();
				Log.d(this.name, "working on command: " + cmd);
				this.receiveCommand(cmd);
				Log.d(this.name, "received incoming data: '" + cmd + "'");
			}

			// Sleep until an incoming message wakes us up.
			this.sleep();
		}

		Log.d(this.name, "terminating");
	}

	@Override
	public boolean receiveCommand(String command) {
		Coordinator.storeTextData(this.name, ((Long) System.currentTimeMillis()).toString(),
				command);
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

}
