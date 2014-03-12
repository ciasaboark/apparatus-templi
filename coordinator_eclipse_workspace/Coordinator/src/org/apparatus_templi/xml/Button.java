package org.apparatus_templi.xml;

import org.apparatus_templi.driver.Driver;

/**
 * Represents a button to render in the widget or full page XML data. The button is expected to have
 * a name.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class Button implements Element {
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
	 *            the type of input field this button should contain. Valid values are referenced
	 *            from {@link InputType}.
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
			sb.append("input=\"" + InputType.NONE + "\" ");
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
}