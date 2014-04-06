package org.apparatus_templi.xml;

/**
 * An element that holds pre-formatted HTML.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class Pre implements ElementInterface {
	private String name;
	private String html;
	private String description = "";

	// public Pre(String name) {
	// this(name, null);
	// }

	public Pre(String name, String html) {
		this.name = name;
		this.html = html;
	}

	@Override
	public String getName() {
		return name;
	}

	public Pre setName(String name) {
		this.name = name;
		return this;
	}

	public String getHtml() {
		return html;
	}

	public Pre setHtml(String html) {
		this.html = html;
		return this;
	}

	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<pre name=\"" + name + "\" ");
		sb.append("description='" + description + "' >");
		sb.append("<![CDATA[" + html + "]]>");
		sb.append("</pre>");
		return sb.toString();
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Pre setDescription(String description) {
		this.description = description;
		return this;
	}
}