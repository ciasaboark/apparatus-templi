package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class FancyWidgetTest extends ControllerModule {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Barrow Sea Ice Webcam");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Fancy Widget Test");
	private final Pre pre = new Pre("html", "");
	private final TextArea text = new TextArea("text", "Barrow Sea Ice Webcam");

	public FancyWidgetTest() {
		this.name = "FancyWidgt";
		pre.setHtml("<div style=\"text-align: center\">"
				// +
				// "<a href=\"http://seaice.alaska.edu/gi/observatories/barrow_webcam\" target=\"_blank\">"
				// +
				// "<img src=\"http://feeder.gina.alaska.edu/webcam-uaf-barrow-seaice-images/current/image\" height=\"120px\" />"
				// + "</a>"
				+ "<video controls autoplay muted=\"muted\" poster=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.png\" preload=\"none\" height=\"150px\" width=\"250px\" >"
				// +
				+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.mp4\" type=\"video/mp4\">"
				+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.webm\" type=\"video/webm\"> +"
				+ "</video>" + "</div>");

		// widgetXml.addElement(text);
		widgetXml.addElement(pre);
		fullXml.addElement(text);
		fullXml.addElement(pre);

	}

	@Override
	public void run() {
		// do nothing

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
		return widgetXml.generateXml();
	}

	@Override
	public String getFullPageXML() {
		return fullXml.generateXml();
	}

}
