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
	private String moduleName = "StatefullLED";
	private boolean running = true;
	
	//our remote module has three LEDs attached
	private int[] leds = {4, 5, 6};
	private boolean[] ledsState = {false, false, false};
	
	@Override
	public String getModuleType() {
		return "Controller";
	}


	@Override
	public void receiveCommand(String message) {
		//throw away the message for now
		Log.d(moduleName, "received message, ignoring");

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
		//since we don't know the state of the remote module at the beginning we
		//+ tell it to reset to a default state (all LEDs off).  If the remote
		//+ side does not respond within 3 seconds then the driver will terminate
		if (Coordinator.isModulePresent(moduleName)) {
			if (Coordinator.sendCommandAndWait(moduleName, "RESET", 3).equals("OKRESET")) {
				for (boolean ledState: ledsState) {
					ledState = false;
				}
			} else {
				Log.e(moduleName, "did not get a response from the remote side, exiting");
				terminate();
			}
		} else {
			Log.e(moduleName, "remote module is not present, shutting down");
			terminate();
		}
		
		
		while (running) {
			//run through a hardcoded loop for now.
			for (int i = 0; i < 2; i++) {
				toggleLED(i);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		//thread is terminating, do whatever cleanup is needed
		Coordinator.sendCommand(moduleName, "RESET");
		Log.d(moduleName, "terminating");

	}
	
	private void toggleLED(int ledNum) {
		Log.d(moduleName, "toggling LED " + ledNum + " on pin " + leds[ledNum] + " to state: " +
				(ledsState[ledNum]? "OFF" : "ON"));
		Coordinator.sendCommand(moduleName, String.valueOf(leds[ledNum]) + ":" + (ledsState[ledNum]? "0" : "1"));
		ledsState[ledNum] = !ledsState[ledNum];
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
