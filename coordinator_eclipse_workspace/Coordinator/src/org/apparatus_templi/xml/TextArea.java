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

	public void setName(String name) {
		this.name = name;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<textarea name=\"" + name + "\">");
		sb.append(text);
		sb.append("</textarea>");
		return sb.toString();
	}
}