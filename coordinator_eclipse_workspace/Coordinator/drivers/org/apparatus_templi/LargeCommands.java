package org.apparatus_templi;

public class LargeCommands extends SensorModule {

	private static final String command1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,<.>/?;:\'\"[{]}\\|-_=+`~";
	public LargeCommands() {
		this.name = "LargeComm";
	}
	
	@Override
	public void run() {
		Log.d(name, "starting");
		Log.d(name, "command size: " + command1.length());
		while (this.running) {
			Coordinator.sendCommand(this.name,  command1);
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public String getSensorList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSensorData(String sensorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void receiveCommand(String command) {
		// TODO Auto-generated method stub

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
