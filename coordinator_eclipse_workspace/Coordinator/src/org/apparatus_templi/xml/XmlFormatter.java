/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	private String name;
	private Integer refresh = 60;
	private final Driver driver;

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
		this.driver = d;
	}

	/**
	 * Set the preferred refresh rate of this XML data. The frontends are free to ignore this value.
	 * 
	 * @param refreshRate
	 *            how often the frontend should refresh this data (in seconds).
	 */
	public XmlFormatter setRefresh(int refreshRate) {
		refresh = refreshRate;
		return this;
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
		if (e == null) {
			throw new IllegalArgumentException("can not add null element");
		}
		if (!(ElementInterface.class.isInstance(e))) {
			throw new IllegalArgumentException("object must implement ElementInterface");
		}
		elementInterfaces.add(e);
	}

	public void setName(String name) throws IllegalArgumentException {
		if (name != null) {
			this.name = name;
		} else {
			throw new IllegalArgumentException("Name can not be null");
		}
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
				+ "\" driver=\"" + driver.getName() + "\" "
				+ (refresh == null ? "" : ("refresh=\"" + refresh + "\" "))
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
