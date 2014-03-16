package org.apparatus_templi.xml;

import java.util.ArrayList;

import org.apparatus_templi.driver.Driver;

/**
 * A convenience class to handle generating XML data that conforms to the schema for full page XML
 * and widget XML data. Contains a number of inner classes that can be used to represent sensors,
 * controllers, and buttons.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class XmlFormatter {
	private static final double VERSION = 1.0;
	private ArrayList<ElementInterface> elementInterfaces;
	private final String name;
	private final String shortName;

	/**
	 * Initialize a new XmlFormatter.
	 * 
	 * @param d
	 *            a reference to the driver the data will represent.
	 * @param longName
	 *            a free-form String representing the name of this Driver.
	 */
	public XmlFormatter(Driver d, String longName) {
		elementInterfaces = new ArrayList<ElementInterface>();
		this.name = longName;
		this.shortName = d.getName();
	}

	/**
	 * Removes all Elements.
	 */
	public void clearElements() {
		elementInterfaces = new ArrayList<ElementInterface>();
	}

	/**
	 * Adds an Element. Elements will be processed in the order in which they are added.
	 * 
	 * @param e
	 *            the Element to add.
	 */
	public void addElement(ElementInterface e) {
		elementInterfaces.add(e);
	}

	/**
	 * Removes the first occurence of the given Element if it exists.
	 * 
	 * @param e
	 *            the Element to remove.
	 */
	public void removeElement(ElementInterface e) {
		elementInterfaces.remove(e);
	}

	/**
	 * Generate an XML representation of this button. This XML data generated is incomplete on its
	 * own, and is expected to be wrapped into a container by the {@link XmlFormatter}.
	 */
	public String generateXml() {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		xml.append("<module version=\"" + XmlFormatter.VERSION + "\" name=\"" + this.name
				+ "\" driver=\"" + shortName + "\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"resource?file=xml/module-schema.xsd\" " + " >");
		for (ElementInterface e : elementInterfaces) {
			xml.append(e.getXml());
		}
		xml.append("</module>");
		return xml.toString();
	}

	// /**
	// * Generates an XML document that will validate against the full page XML schema. Elements are
	// * processed in the order in which they were added.
	// *
	// * @return the String representation of the full page XML data.
	// */
	// public String generateFullXml() {
	// StringBuilder xml = new StringBuilder();
	// xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	// xml.append("< version=\"" + XmlFormatter.VERSION + "\" name=\"" + this.name
	// + "\" driver=\"" + shortName + "\" "
	// + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	// + "xsi:noNamespaceSchemaLocation=\"resource?file=xml/widget-schema.xsd\" " + " >");
	// for (Element e : elements) {
	// xml.append(e.getXml());
	// }
	// xml.append("</widget>");
	// return xml.toString();
	// }

}
