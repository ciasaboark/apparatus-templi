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
	private String description = "";

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
		sb.append("description='" + description + "' ");
		sb.append(">");

		sb.append("<value>" + this.value + "</value>");
		sb.append("</sensor>");

		return sb.toString();
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Sensor setDescription(String description) {
		this.description = description;
		return this;
	}
}