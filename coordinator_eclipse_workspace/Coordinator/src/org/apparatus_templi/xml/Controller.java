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
		sb.append(">");

		sb.append("<status>" + status + "</status>");
		sb.append("</controller>");

		return sb.toString();
	}
}