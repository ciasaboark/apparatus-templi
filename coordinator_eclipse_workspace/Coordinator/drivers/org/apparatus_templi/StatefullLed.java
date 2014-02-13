package org.apparatus_templi;

import java.util.ArrayList;

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
	
	//our remote module has three LEDs attached
	private int[] leds = {5, 6, 7};
	private boolean[] ledsState = {false, false, false};
	
	public StatefullLed() {
		this.name = "LOCAL";
	}
	
	@Override
	public String getModuleType() {
		return "Controller";
	}


	@Override
	public void receiveCommand(String message) {
		//throw away the message for now
//		Log.d(this.name, "received message '"+ message + "', ignoring");

	}


	@Override
	void receiveBinary(byte[] data) {
		Log.d(this.name, "received binary, ignoring");
		
	}

	@Override
	public String getWidgetXML() {
		Log.w(this.name, "getWidgetXML() unimplimented");
		return null;
	}

	@Override
	public String getFullPageXML() {
		Log.w(this.name, "getFullPageXML() unimplimented");
		return null;
	}

	@Override
	public void run() {
		Log.d(this.name, "starting");
		
		//check for any queued messages
		while (queuedCommands.size() > 0) {
			receiveCommand(queuedCommands.pop());
		}
				
				
		//since we don't know the state of the remote module at the beginning we
		//+ tell it to reset to a default state (all LEDs off).  If the remote
		//+ module is not there then terminate
		if (Coordinator.isModulePresent(this.name)) {
			Coordinator.sendCommand(this.name, "RESET");
			for (boolean state: ledsState) {
				state = false;
			}
		} else {
			Log.e(this.name, "remote module is not present, shutting down");
			terminate();
		}
		
		
		while (running) {
			//run through a hard-coded loop for now.
			int ledNum = 0 + (int)(Math.random() * ((2 - 0) + 1));
			toggleLED(ledNum);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//thread is terminating, do whatever cleanup is needed
		Coordinator.sendCommand(this.name, "RESET");
		Log.d(this.name, "terminating");

	}
	
	private void toggleLED(int ledNum) {
		if (ledNum > 2 || ledNum < 0) {
			Log.e(this.name, "toggleLED() given invalid led number");
		} else {
//			Log.d(this.name, "toggling LED " + ledNum + " on pin " + leds[ledNum] + " to state: " +
//					(ledsState[ledNum]? "OFF" : "ON"));
			Coordinator.sendCommand(this.name, String.valueOf(leds[ledNum]) + (ledsState[ledNum]? "0" : "1"));
			ledsState[ledNum] = !ledsState[ledNum];
		}
	}

	@Override
	public ArrayList<String>  getControllerList() {
		Log.w(this.name, "getControllerListXML() unimplimented for now");
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		Log.w(this.name, "getControllerStatusXML() unimplimented for now");
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		Log.d(this.name, "tellController() not validating command, passing without verification");
		Coordinator.sendCommand(this.name, command);

	}

}
