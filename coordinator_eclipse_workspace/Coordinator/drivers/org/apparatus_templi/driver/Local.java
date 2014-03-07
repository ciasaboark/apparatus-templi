package org.apparatus_templi.driver;

import java.util.ArrayList;
import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.XmlFormatter;

public class Local extends ControllerModule {
	private int[] leds = {5, 6, 7};
	private boolean[] ledsState = {false, false, false};
	private XmlFormatter widgetFormatter = new XmlFormatter(this, "Local Controller");
	private XmlFormatter fullFormatter = new XmlFormatter(this, "Local Controller");
	
	public Local() {
		this.name = "LOCAL";
	}
	
	@Override
	public void run() {
		Log.d(this.name, "starting");

        //check for any queued messages
        while (queuedCommands.size() > 0) {
            receiveCommand(queuedCommands.pop());
        }


        //If the remote module is not there then terminate
        if (!Coordinator.isModulePresent(this.name)) {
            Log.e(this.name, "remote module is not present, shutting down");
            terminate();
        }


        while (isRunning) {
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
        Log.d(this.name, "terminating");
	}
	
	private void toggleLED(int ledNum) {
        if (ledNum > 2 || ledNum < 0) {
            Log.e(this.name, "toggleLED() given invalid led number");
        } else {
//        	Log.d(this.name, "toggling LED " + leds[ledNum]);
            Coordinator.sendCommand(this, String.valueOf(leds[ledNum]) + (ledsState[ledNum]? "0" : "1"));
            ledsState[ledNum] = !ledsState[ledNum];
        }
	}

	@Override
	public ArrayList<String> getControllerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		// TODO Auto-generated method stub

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
		return widgetFormatter.generateWidgetXml();
	}

	@Override
	public String getFullPageXML() {
		return fullFormatter.generateFullXml();
	}

}
