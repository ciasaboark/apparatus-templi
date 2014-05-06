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