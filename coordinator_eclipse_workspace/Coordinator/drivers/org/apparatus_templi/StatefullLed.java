package org.apparatus_templi;

/**
 * StatefullLed
 * A driver for a remote module with a number of LEDs.  Each LED
 * 	can be turned on or off, and the driver keeps track of the state
 * 	of each 'pixel'
 * 
 * Remote side expects one of the following commands:
 * 	"RESET" - reset all attached LEDs to their off state
 * 	"(int):(int)" - set the state of the LED attached to 
 * the first (int) to the boolean value of the second int.
 * Valid values for the first int are 4,5,6.
 * The second int may be any valid signed int value, with 0
 * equating to false, all other values to true.
 * 
 * Driver listens for the following responses:
 * 	"RESETOK" - the reset command was received and all LEDs
 * were reset to off.
 * 	"OK(int)" - the state of the LED on pin number (int) was
 * toggled.
 * @author Jonathan Nelson <ciasaboark@gmail.com> 
 */

public class StatefullLed extends ControllerModule {
	private String moduleName = "LOCAL";
	
	//our remote module has three LEDs attached
	private int[] leds = {5, 6, 7};
	private boolean[] ledsState = {false, false, false};
	
	public StatefullLed() {
		this.name = moduleName;
	}
	
	@Override
	public String getModuleType() {
		return "Controller";
	}


	@Override
	public void receiveCommand(String message) {
		//throw away the message for now
//		Log.d(moduleName, "received message '"+ message + "', ignoring");

	}


	@Override
	void receiveBinary(byte[] data) {
		Log.d(moduleName, "received binary, ignoring");
		
	}

	@Override
	public String getWidgetXML() {
		Log.w(moduleName, "getWidgetXML() unimplimented");
		return null;
	}

	@Override
	public String getFullPageXML() {
		Log.w(moduleName, "getFullPageXML() unimplimented");
		return null;
	}

	@Override
	public void run() {
		Log.d(moduleName, "starting");
		
		//check for any queued messages
		while (queuedCommands.size() > 0) {
			receiveCommand(queuedCommands.pop());
		}
				
				
		//since we don't know the state of the remote module at the beginning we
		//+ tell it to reset to a default state (all LEDs off).  If the remote
		//+ module is not there then terminate
		if (Coordinator.isModulePresent(moduleName)) {
			Coordinator.sendCommand(moduleName, "RESET");
			for (boolean state: ledsState) {
				state = false;
			}
		} else {
			Log.e(moduleName, "remote module is not present, shutting down");
			terminate();
		}
		
		
		while (running) {
			//run through a hard-coded loop for now.
			int ledNum = 0 + (int)(Math.random() * ((2 - 0) + 1));
			toggleLED(ledNum);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//thread is terminating, do whatever cleanup is needed
		Coordinator.sendCommand(moduleName, "RESET");
		Log.d(moduleName, "terminating");

	}
	
	private void toggleLED(int ledNum) {
		if (ledNum > 2 || ledNum < 0) {
			Log.e(moduleName, "toggleLED() given invalid led number");
		} else {
			Log.d(moduleName, "toggling LED " + ledNum + " on pin " + leds[ledNum] + " to state: " +
					(ledsState[ledNum]? "OFF" : "ON"));
			Coordinator.sendCommand(moduleName, String.valueOf(leds[ledNum]) + (ledsState[ledNum]? "0" : "1"));
			ledsState[ledNum] = !ledsState[ledNum];
		}
	}

	@Override
	public String getControllerListXML() {
		Log.w(moduleName, "getControllerListXML() unimplimented for now");
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		Log.w(moduleName, "getControllerStatusXML() unimplimented for now");
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		Log.d(moduleName, "tellController() not validating command, passing without verification");
		Coordinator.sendCommand(moduleName, command);

	}

}
