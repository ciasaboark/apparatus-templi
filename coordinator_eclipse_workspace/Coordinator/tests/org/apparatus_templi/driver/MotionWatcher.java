package org.apparatus_templi.driver;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Event;
import org.apparatus_templi.EventWatcher;
import org.apparatus_templi.Log;
import org.apparatus_templi.event.MotionEvent;
import org.apparatus_templi.xml.Sensor;
import org.apparatus_templi.xml.XmlFormatter;

/**
 * A sample driver that registers itself to watch for motion events
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class MotionWatcher extends SensorModule implements EventWatcher {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Motion Watcher");
	private final Sensor motion = new Sensor("Last motion event");

	public MotionWatcher() {
		this.name = "MOT_WATCH";
		widgetXml.addElement(motion);
		widgetXml.setRefresh(5);
		motion.setValue("unknown");
	}

	@Override
	public void run() {
		Coordinator.registerEventWatch(this, new MotionEvent());
		while (isRunning) {
			this.sleep();
		}

	}

	@Override
	public void receiveEvent(Event e) {
		if (e instanceof MotionEvent) {
			Log.d(this.name, "motion was detected by module " + e.getOrigin().getName());
			Date date = new Date(e.getTimestamp());
			DateFormat df = DateFormat.getDateTimeInstance();
			motion.setValue(df.format(date));
			Coordinator.storeTextData(this.name, String.valueOf(e.getTimestamp()),
					"motion detected");
			Coordinator.storeTextData(this.name, "last_motion", String.valueOf(e.getTimestamp()));
		} else {
			Log.d(this.name, "received unknown event type");
		}

	}

	@Override
	public ArrayList<String> getSensorList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSensorData(String sensorName) {
		// TODO Auto-generated method stub
		return null;
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
		if (motion.getValue().equals("unknown")) {
			String lastMotion = Coordinator.readTextData(this.name, "last_motion");
			if (lastMotion != null) {
				Date date = new Date(Long.valueOf(lastMotion));
				DateFormat df = DateFormat.getDateTimeInstance();
				motion.setValue(df.format(date));
			}
		}
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

}
