package org.apparatus_templi.driver;

import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class FancyWidgetTest extends Driver {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Barrow Sea Ice Webcam");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Fancy Widget Test");
	private final Pre widgetVideo = new Pre("html", "");
	private final Pre fullVideo = new Pre("html", "");
	private final TextArea text = new TextArea("text", "Barrow Sea Ice Webcam");

	public FancyWidgetTest() {
		this.name = "FancyWidgt";
		widgetVideo
				.setHtml("<div style=\"text-align: center\">"
						+ "<video controls autoplay muted=\"muted\" poster=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.png\" preload=\"none\" height=\"200px\" width=\"400px\" >"
						// +
						+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.mp4\" type=\"video/mp4\">"
						+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.webm\" type=\"video/webm\"> +"
						+ "</video>" + "</div>");

		fullVideo
				.setHtml("<div style=\"text-align: center\">"
						+ "<video controls autoplay muted=\"muted\" poster=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.png\" preload=\"none\" width=\"500px\" >"
						// +
						+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.mp4\" type=\"video/mp4\">"
						+ "<source src=\"http://feeder.gina.alaska.edu/feeds/webcam-uaf-barrow-seaice-images/movies/current-1_day_animation.webm\" type=\"video/webm\"> +"
						+ "</video>" + "</div>");

		// widgetXml.addElement(text);
		widgetXml.addElement(widgetVideo);
		fullXml.addElement(text);
		fullXml.addElement(fullVideo);

	}

	@Override
	public void run() {
		// do nothing

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
