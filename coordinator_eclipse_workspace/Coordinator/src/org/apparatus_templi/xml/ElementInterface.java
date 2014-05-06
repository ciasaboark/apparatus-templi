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
 * An interface for all objects which can automatically generate their own XML representation data.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface ElementInterface {
	/**
	 * Returns the name of this Element.
	 * 
	 */
	public String getName();

	/**
	 * Generate an XML representation of this Element. The XML data generated is expected to be
	 * wrapped into a container by an {@link XmlFormatter} instance;
	 */
	public String getXml();

	public String getDescription();
}