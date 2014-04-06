package org.apparatus_templi.xml;

/**
 * An Element to hold a line of text.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class TextArea implements ElementInterface {
	private String text;
	private String name;
	private String description = "";

	// public TextArea(String name) {
	// this(name, null);
	// }

	public TextArea(String name, String text) {
		this.name = name;
		this.text = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public TextArea setName(String name) {
		this.name = name;
		return this;
	}

	public TextArea setText(String text) {
		this.text = text;
		return this;
	}

	public String getText() {
		return text;
	}

	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<textarea name='" + name + "' ");
		sb.append("description='" + description + "' >");
		sb.append(text);
		sb.append("</textarea>");
		return sb.toString();
	}

	@Override
	public String getDescription() {
		return description;
	}

	public TextArea setDescription(String description) {
		this.description = description;
		return this;
	}
}