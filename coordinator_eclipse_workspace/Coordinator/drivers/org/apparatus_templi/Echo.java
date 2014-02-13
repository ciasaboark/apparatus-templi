package org.apparatus_templi;

public class Echo extends ControllerModule {
	public Echo() {
		this.name = "ECHO";
	}
	
	
	@Override
	public void run() {
//		String command = "Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO ";
//		String command = "Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO Testing ECHO ";
		String command = "Testing ECHO";
		
		while (running) {
			Log.d(this.name, "sending " + command.length() + " bytes to echo");
			try {
				Coordinator.sendCommand(this.name, command);
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	String getControllerListXML() {
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
