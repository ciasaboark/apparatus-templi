package org.apparatus_templi;

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
	private ArrayList<Element> elements;
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
		elements = new ArrayList<Element>();
		this.name = longName;
		this.shortName = d.getName();
	}

	/**
	 * Removes all Elements.
	 */
	public void clearElements() {
		elements = new ArrayList<Element>();
	}

	/**
	 * Adds an Element. Elements will be processed in the order in which they are added.
	 * 
	 * @param e
	 *            the Element to add.
	 */
	public void addElement(Element e) {
		elements.add(e);
	}

	/**
	 * Removes the first occurence of the given Element if it exists.
	 * 
	 * @param e
	 *            the Element to remove.
	 */
	public void removeElement(Element e) {
		elements.remove(e);
	}

	/**
	 * Generates an XML document that will validate against the widget XML schema. Elements are
	 * processed in the order in which they were added.
	 * 
	 * @return the String representation of the widget XML data.
	 */
	public String generateWidgetXml() {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		xml.append("<widget version=\"" + XmlFormatter.VERSION + "\" name=\"" + this.name
				+ "\" driver=\"" + shortName + "\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"resource?file=xml/widget-schema.xsd\" " + " >");
		for (Element e : elements) {
			xml.append(e.getXml());
		}
		xml.append("</widget>");
		return xml.toString();
	}

	/**
	 * Generates an XML document that will validate against the full page XML schema. Elements are
	 * processed in the order in which they were added.
	 * 
	 * @return the Strin grepresentation of the full page XML data.
	 */
	public String generateFullXml() {
		// TODO
		return null;
	}

	/**
	 * An interface for all objects which can automatically generate their own XML representation
	 * data.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private interface Element {
		/**
		 * Returns the name of this Element.
		 * 
		 */
		public String getName();

		/**
		 * Generate an XML representation of this Element.
		 */
		public String getXml();
	}

	/**
	 * A representation of a controller located in a remote module. The controller is expected to
	 * have a name and current status.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	public static class Controller implements Element {
		private final String name;
		private String icon = null;
		private String status = null;

		/**
		 * Initialize a new Controller element with the given name
		 * 
		 * @param name
		 */
		public Controller(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		/**
		 * Returns the name of the icon associated with this Controller.
		 */
		public String getIcon() {
			return icon;
		}

		/**
		 * Set the icon associated with this Controller to the given icon name.
		 * 
		 * @param icon
		 */
		public void setIcon(String icon) {
			this.icon = icon;
		}

		/**
		 * Set the current status of this controller to the given status.
		 * 
		 * @param status
		 */
		public void setStatus(String status) {
			this.status = status;
		}

		/**
		 * Returns the current status of this Controller.
		 */
		public String getStatus() {
			return status;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<controller name=\"" + this.name + "\" ");
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(">");

			sb.append("<status>" + status + "</status>");
			sb.append("</controller>");

			return sb.toString();
		}
	}

	/**
	 * A representation of a sensor in a remote module. The sensor is expected to have a name and a
	 * value.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	public static class Sensor implements Element {
		private final String name;
		private String icon = null;
		private String value = null;

		/**
		 * Initialize the Sensor with the given name.
		 * 
		 * @param name
		 */
		public Sensor(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		/**
		 * Returns the name of the icon associated with this Sensor.
		 */
		public String getIcon() {
			return icon;
		}

		/**
		 * Set the icon associated with this Sensor to the given icon name.
		 * 
		 * @param icon
		 */
		public void setIcon(String icon) {
			this.icon = icon;
		}

		/**
		 * Set the current value of this Sensor to the given value.
		 * 
		 * @param value
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * Returns the current value of the Sensor.
		 */
		public String getValue() {
			return value;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<sensor name=\"" + this.name + "\" ");
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(">");

			sb.append("<value>" + this.value + "</value>");
			sb.append("</sensor>");

			return sb.toString();
		}
	}

	/**
	 * Represents a button to render in the widget or full page XML data. The button is expected to
	 * have a name.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	public static class Button implements Element {
		private final String name;
		private String action = null;
		private String inputType = null;
		private String inputVal = null;
		private String icon = null;

		/**
		 * Initialize the Button with the given name
		 * 
		 * @param name
		 */
		public Button(String name) {
			this.name = name;
		}

		/**
		 * Returns the current icon associated with this Button.
		 * 
		 */
		public String getIcon() {
			return icon;
		}

		/**
		 * Set the icon of this button to the given icon name.
		 * 
		 * @param icon
		 */
		public void setIcon(String icon) {
			this.icon = icon;
		}

		@Override
		public String getName() {
			return name;
		}

		/**
		 * Returns the String representation of the action this button should perform when clicked.
		 */
		public String getAction() {
			return action;
		}

		/**
		 * Sets the action of this Button to the given action. The action specified is driver
		 * specific and should correspond to commands that the driver can process within
		 * {@link Driver#receiveCommand(String)}. The action specified can be specified verbatim, or
		 * may include the special string "$input". The string "$input" will be replaced with the
		 * current value of the input field when the button is clicked. For example if the current
		 * value of the input field is "44" and the action string is "m$input" then the command
		 * generated when the button is clicked will be "m44". If the input type is
		 * {@link InputType#NONE} then the special string "$input" will be stripped and the
		 * corresponding command generated would be "m".
		 * 
		 * @param action
		 *            the command to send to this Driver when the button is clicked.
		 */
		public void setAction(String action) {
			this.action = action;
		}

		/**
		 * Return the input type of this Button.
		 */
		public String getInputType() {
			return inputType;
		}

		/**
		 * 
		 * 
		 * @param inputType
		 */
		/**
		 * Sets the input type of this button to the specified InputType.
		 * 
		 * @param inputType
		 *            the type of input field this button should contain. Valid values are
		 *            referenced from {@link InputType}.
		 * @throws IllegalArgumentException
		 *             if the given inputType does match known values from {@link InputType}
		 */
		public void setInputType(String inputType) throws IllegalArgumentException {
			if (InputType.isValidInputType(inputType)) {
				this.inputType = inputType;
			} else {
				throw new IllegalArgumentException("Unknown input type");
			}
		}

		public String getInputVal() {
			return inputVal;
		}

		public void setInputVal(String inputVal) {
			this.inputVal = inputVal;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<button title=\"" + this.name + "\" ");
			sb.append("action=\"" + this.action + "\" ");
			if (this.inputType == null) {
				sb.append("input=\"" + XmlFormatter.Button.InputType.NONE + "\" ");
			} else {
				sb.append("input=\"" + this.inputType + "\" ");
			}
			if (this.inputVal != null) {
				sb.append("inputVal=\"" + this.inputVal + "\" ");
			}
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(" />");

			return sb.toString();
		}

		/**
		 * A convenience class that contains a list of known valid Button input types.
		 * 
		 * @author Jonathan Nelson <ciasaboark@gmail.com>
		 * 
		 */
		public static class InputType {
			public static final String TEXT = "text";
			public static final String NUM = "numeric";
			public static final String NONE = "none";
		
			public static boolean isValidInputType(String inputType) {
				boolean isValid = false;
				if (inputType.equals(TEXT) || inputType.equals(NUM) || inputType.equals(NONE)) {
					isValid = true;
				}
				return isValid;
			}
		}
	}
}
