package org.apparatus_templi;

import java.util.ArrayList;

public class Echo extends ControllerModule {
	public Echo() {
		this.name = "ECHO";
	}
	
	
	@Override
	public void run() {
//		String command = "Testing ECHO";
		String command = "123456789012345678901234567890123456789012345678901234567890123456789";
		
		while (isRunning) {
			Log.d(this.name, "sending " + command.length() + " bytes to echo");
			try {
				int numTries = 1;
				String response = Coordinator.sendCommandAndWait(this, command, 3);
				while (!command.equals(response)) {
					Log.d(this.name, "received malformed response. Send '" + command + "' received '" + response + "'. Retrying...");
					response = Coordinator.sendCommandAndWait(this, command, 3);
					numTries++;
				}
				
				Log.d(this.name, "Received correct response after " + numTries + " tries\n");
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	ArrayList<String> getControllerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getControllerStatusXML(String controllerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void tellController(String controllerName, String command) {
		// TODO Auto-generated method stub

	}

	@Override
	void receiveCommand(String command) {
		Log.w(this.name, "received command '" + command + "'");

	}

	@Override
	void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	String getWidgetXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
