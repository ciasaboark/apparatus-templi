package org.apparatus_templi;

import org.apparatus_templi.Log;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.Sensor;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class XmlTest extends Driver {
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
			"<p>Some pre-formatted html.</p><img src='/resources?file=images/arduino_icon.png'>");
	private final String b64Image = "data:image/jpeg;base64,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";
	private final Pre b64Pre = new Pre(
			"b64",
			"<p>This is a base64 encoded embedded image:</p><img title='foobar' width='50px' style=\"width: 50px\" src=\""
					+ b64Image + "\" />").setDescription("Sassy bobcat");
	private final Pre longPre = new Pre(
			"long text",
			"<p>This is a really long paragraph<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>The End!</p>");
	// some additional Elements that are only displayed in the full page XML
	// Note that the pre-formatted area could link to resources served by the web server, or to
	// outside resources
	TextArea sensDesc = new TextArea("sensor description",
			"Temperature data is provided via a DHT11 "
					+ "sensor.  Readings are only accurate to +/- 1C.");
	Pre sensLink = new Pre(
			"sensor link",
			"<p>More information on the sensor can be found at the "
					+ "<a href='http://www.adafruit.com/products/386' title='Check out the Adafruit shop for more cool stuff' >AdaFruit</a> website.");

	public XmlTest() {
		this.name = "XmlTest";
		widgetXml = new XmlFormatter(this, "Xml Format Tester");
		widgetXml.setRefresh(600);
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
		controller1Button.setInputVal("40");
		controller1Button.setIcon("fa fa-refresh");

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
		fullXml.addElement(b64Pre);
		fullXml.addElement(longPre);
		// add an embedded infobox
		fullXml.addElement(new Pre("infobox",
				"<div class='info-box'><span class='title'>foo</span><span class='content'>bar</span></div>"));

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
	public boolean receiveCommand(String command) {
		Log.d(this.name, "received command '" + command + "'");
		return true;
	}

	@Override
	public boolean receiveBinary(byte[] data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getWidgetXML() {
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullXml.generateXml();
	}
}
