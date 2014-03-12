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

	public Pre(String name) {
		this(name, null);
	}

	public Pre(String name, String html) {
		this.name = name;
		this.html = html;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	@Override
	public String getXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<pre name=\"" + name + "\">");
		sb.append("<![CDATA[" + html + "]]>");
		sb.append("</pre>");
		return sb.toString();
	}
}