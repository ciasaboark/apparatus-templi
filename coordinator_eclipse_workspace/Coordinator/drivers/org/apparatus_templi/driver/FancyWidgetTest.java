package org.apparatus_templi.driver;

import java.util.ArrayList;

import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;

public class FancyWidgetTest extends ControllerModule {
	private final XmlFormatter widgetXml = new XmlFormatter(this, "Fancy Widget Test");
	private final XmlFormatter fullXml = new XmlFormatter(this, "Fancy Widget Test");
	private final Pre pre = new Pre("html", "");
	private final TextArea text = new TextArea("text", "");

	public FancyWidgetTest() {
		this.name = "FancyWidgt";
		pre.setHtml("<table  cellspacing=\"3\" style=\"border-spacing:3px;width:22em;\">"
				+ "<tbody><tr><th colspan=\"2\" style=\"text-align:center;font-size:125%;"
				+ "font-weight:bold;\"><span class=\"fn\">Edgar Allan Poe</span></th></tr><tr>"
				+ "<td colspan=\"2\" style=\"text-align:center;\"><a href=\"/wiki/File:Edgar_Allan_Poe"
				+ "_daguerreotype_crop.png\" class=\"image\"><img alt=\"Edgar Allan Poe daguerreotype crop.png\""
				+ " src=\"//upload.wikimedia.org/wikipedia/commons/thumb/8/84/Edgar_Allan_Poe_daguerreotype_crop.png"
				+ "/220px-Edgar_Allan_Poe_daguerreotype_crop.png\" width=\"220\" height=\"308\" srcset=\"//upload.wik"
				+ "imedia.org/wikipedia/commons/ thumb/8/84/Edgar_Allan_Poe_daguerreotype_crop.png/330px-Edgar_Allan_"
				+ "Poe_daguerreotype_crop.png 1.5x, //upload.wikimedia.org/wikipedia/commons/thumb/8/84/Edgar_Allan_Po"
				+ "e_daguerreotype_crop.png/440px-Edgar_Allan_Poe_daguerreotype_crop.png 2x\"></a><br><div>1849 \"Annie"
				+ "\" <a href=\"/wiki/Daguerreotype\" title=\"Daguerreotype\">daguerreotype</a> of Poe</div></td></tr><tr"
				+ "><th scope=\"row\" style=\"text-align:left;\">Born</th><td>Edgar Poe<br>");

		widgetXml.addElement(text);
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
