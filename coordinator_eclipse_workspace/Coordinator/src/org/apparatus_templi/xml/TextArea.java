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