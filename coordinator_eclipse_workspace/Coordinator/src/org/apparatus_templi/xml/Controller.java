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
 * A representation of a controller located in a remote module. The controller is expected to have a
 * name and current status.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class Controller implements ElementInterface {
	private final String name;
	private String icon = null;
	private String status = null;
	private String description = "";

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
	public Controller setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	/**
	 * Set the current status of this controller to the given status.
	 * 
	 * @param status
	 */
	public Controller setStatus(String status) {
		this.status = status;
		return this;
	}

	/**
	 * Returns the current status of this Controller.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Generate an XML representation of this controller. This XML data generated is incomplete on
	 * its own, and is expected to be wrapped into a container by the {@link XmlFormatter}.
	 */
	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<controller name=\"" + this.name + "\" ");
		if (this.icon != null) {
			sb.append("icon=\"" + this.icon + "\" ");
		}
		sb.append("description='" + description + "' ");
		sb.append(">");

		sb.append("<status>" + status + "</status>");
		sb.append("</controller>");

		return sb.toString();
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Controller setDescription(String description) {
		this.description = description;
		return this;
	}
}