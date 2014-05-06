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

import org.apparatus_templi.driver.Driver;

/**
 * Represents a button to render in the widget or full page XML data. The button is expected to have
 * a name.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class Button implements ElementInterface {
	private final String name;
	private String action = null;
	private String inputType = null;
	private String inputVal = null;
	private String icon = null;
	private String description = "";

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
	public Button setIcon(String icon) {
		this.icon = icon;
		return this;
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
	 * Sets the action of this Button to the given action. The action specified is driver specific
	 * and should correspond to commands that the driver can process within
	 * {@link Driver#receiveCommand(String)}. The action specified can be specified verbatim, or may
	 * include the special string "$input". The string "$input" will be replaced with the current
	 * value of the input field when the button is clicked. For example if the current value of the
	 * input field is "44" and the action string is "m$input" then the command generated when the
	 * button is clicked will be "m44". If the input type is {@link InputType#NONE} then the special
	 * string "$input" will be stripped and the corresponding command generated would be "m".
	 * 
	 * @param action
	 *            the command to send to this Driver when the button is clicked.
	 */
	public Button setAction(String action) {
		this.action = action;
		return this;
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
	 *            the type of input field this button should contain. Valid values are referenced
	 *            from {@link InputType}.
	 * @throws IllegalArgumentException
	 *             if the given inputType does match known values from {@link InputType}
	 */
	public Button setInputType(String inputType) throws IllegalArgumentException {
		if (InputType.isValidInputType(inputType)) {
			this.inputType = inputType;
		} else {
			throw new IllegalArgumentException("Unknown input type");
		}
		return this;
	}

	/**
	 * Returns the default value that should be inserted into this Buttons input area.
	 */
	public String getInputVal() {
		return inputVal;
	}

	/**
	 * Set the default value of this Button's input area. Care should be taken to insure that this
	 * data can conform to this Button's input type.
	 * 
	 * @param inputVal
	 *            the default value that should be placed into this buttons input area.
	 */
	public Button setInputVal(String inputVal) {
		this.inputVal = inputVal;
		return this;
	}

	/**
	 * Generate an XML representation of this button. This XML data generated is incomplete on its
	 * own, and is expected to be wrapped into a container by the {@link XmlFormatter}.
	 */
	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<button title=\"" + this.name + "\" ");
		sb.append("action=\"" + this.action + "\" ");
		if (this.inputType == null) {
			sb.append("input=\"" + InputType.NONE + "\" ");
		} else {
			sb.append("input=\"" + this.inputType + "\" ");
		}
		if (this.inputVal != null) {
			sb.append("inputVal=\"" + this.inputVal + "\" ");
		}
		sb.append("description='" + description + "' ");
		if (this.icon != null) {
			sb.append("icon=\"" + this.icon + "\" ");
		}
		sb.append(" />");

		return sb.toString();
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Button setDescription(String description) {
		this.description = description;
		return this;
	}
}