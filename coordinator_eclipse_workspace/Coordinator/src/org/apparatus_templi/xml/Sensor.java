package org.apparatus_templi.xml;

/**
 * A representation of a sensor in a remote module. The sensor is expected to have a name and a
 * value.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class Sensor implements ElementInterface {
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
	public Sensor setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	/**
	 * Set the current value of this Sensor to the given value.
	 * 
	 * @param value
	 */
	public Sensor setValue(String value) {
		this.value = value;
		return this;
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