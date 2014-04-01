package org.apparatus_templi.driver;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;

public class Echo extends Driver {
	public Echo() {
		this.name = "ECHO";
	}

	private static final String KEY_CORRECT_RESPONSE = "correct response";

	@Override
	public void run() {
		String command = "123456789012345678901234567890123456789012345678901234567890123456789";

		while (isRunning) {
			Log.d(this.name, "sending " + command.length() + " bytes to echo");
			int numTries = 1;
			String response = Coordinator.sendCommandAndWait(this, command, 3);
			while (!command.equals(response) && numTries < 5) {
				Log.d(this.name, "received malformed response. Send '" + command + "' received '"
						+ response + "'. Retrying...");
				response = Coordinator.sendCommandAndWait(this, command, 3);
				numTries++;
			}
			if (command.equals(response)) {
				Log.d(this.name, "Received correct response after " + numTries + " tries");
				Coordinator.storeTextData(this.name, KEY_CORRECT_RESPONSE,
						String.valueOf(System.currentTimeMillis()));
			} else {
				Log.d(this.name, "Gave up after " + numTries + " tries.");
			}

			this.sleep(1000 * 10); // wake every 10 seconds
		}

	}

	@Override
	public void receiveCommand(String command) {
		Log.w(this.name, "received command '" + command + "'");

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
