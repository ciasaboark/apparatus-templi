package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class SleepyDriver extends Driver {
	public SleepyDriver() {
		this.name = "SLEEPY";
	}

	@Override
	public void run() {
		this.queuedBinary.clear();
		this.queuedCommands.clear();
		Log.d(this.name, "starting");

		while (isRunning) {
			// simulate doing some stuff, then schedule a wake up
			// + in one minute
			String reading = Coordinator.sendCommandAndWait(this, "read temp", 6);

			// all instance data will be lost when the driver is restarted,
			// + so we ask the coordinator to log the reading (if we got a response)
			if (reading != null) {
				Coordinator.storeTextData(this.name, String.valueOf(System.currentTimeMillis()),
						reading);
			}

			Log.d(this.name, "scheduling a restart in 5 minutes");
			this.sleep(1000 * 60 * 5);
			Log.d(this.name, "waking");
		}

		Log.d(this.name, "run() exiting");

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
