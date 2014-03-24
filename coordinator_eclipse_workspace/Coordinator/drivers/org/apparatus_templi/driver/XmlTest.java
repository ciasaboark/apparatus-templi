package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.Log;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.Sensor;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class XmlTest extends ControllerModule {
	// we will use two instances of XmlFormatter, one to hold widget data, and one to hold the full
	// page XML data
	private final XmlFormatter widgetXml;
	private final XmlFormatter fullXml;

	// the widget XmlFormatter will only hold one sensor, one controller, and two buttons. The full
	// page generator will hold those same elements, plus some others.
	private final Sensor tempSensor;
	private final Controller controller1;
	private final Button tempSensButton;
	private final Button controller1Button;
	private final TextArea text1 = new TextArea("text area 1", "This is a long line of text that "
			+ "should be wrapped it it does not fit within a single physical line");
	private final Pre pre1 = new Pre("pre1",
			"<p>Some pre-formatted html.</p><img src='/resources?file=images/arduino_icon.png'");

	// some additional Elements that are only displayed in the full page XML
	// Note that the pre-formatted area could link to resources served by the web server, or to
	// outside resources
	TextArea sensDesc = new TextArea("sensor description",
			"Temperature data is provided via a DHT11 "
					+ "sensor.  Readings are only accurate to +/- 1C.");
	Pre sensLink = new Pre("sensor link", "<p>More information on the sensor can be found at the "
			+ "<a href='http://www.adafruit.com/products/386' >AdaFruit</a> website.");

	public XmlTest() {
		this.name = "XmlTest";
		widgetXml = new XmlFormatter(this, "Xml Format Tester");
		widgetXml.setRefresh(10);
		fullXml = new XmlFormatter(this, "Xml Format Tester");
		fullXml.setRefresh(5);

		tempSensor = new Sensor("Some Sensor");
		tempSensor.setValue("unknown");
		controller1 = new Controller("Some Controller");
		controller1.setStatus("waiting");
		tempSensButton = new Button("Refresh");
		tempSensButton.setAction("r");
		tempSensButton.setInputType(InputType.NONE);

		controller1Button = new Button("Turn");
		controller1Button.setAction("m$input");
		controller1Button.setInputType(InputType.NUM);

		widgetXml.addElement(tempSensor);
		widgetXml.addElement(tempSensButton);
		widgetXml.addElement(controller1);
		widgetXml.addElement(controller1Button);

		fullXml.addElement(tempSensor);
		fullXml.addElement(sensDesc);
		fullXml.addElement(sensLink);
		fullXml.addElement(tempSensButton);
		fullXml.addElement(controller1);
		fullXml.addElement(controller1Button);
		fullXml.addElement(text1);
		fullXml.addElement(pre1);

	}

	@Override
	public void run() {
		while (!this.queuedCommands.isEmpty()) {
			receiveCommand(queuedCommands.poll());
		}
		while (isRunning) {
			this.sleep(1000 * 60);
			Integer i;
			try {
				i = Integer.parseInt(tempSensor.getValue());
				i++;
				tempSensor.setValue(String.valueOf(i));
			} catch (NumberFormatException e) {
				tempSensor.setValue("1");
			}
		}

	}

	@Override
	public void receiveCommand(String command) {
		Log.d(this.name, "received command '" + command + "'");

	}

	@Override
	public void receiveBinary(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullXml.generateXml();
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

}
