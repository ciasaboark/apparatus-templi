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

package org.apparatus_templi.web.generator;

import java.net.URI;

/**
 * Provides convenience methods to generate error pages.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class PageGenerator {
	/**
	 * Generates a 404 error page for the given resourceName.
	 * 
	 * @param resourceName
	 *            the name of the file, resource, or URI that could not be found.
	 * @return a String representation of the HTML page to be returned to the user agent.
	 */
	public static String get404ErrorPage(String resourceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" + "<body>"
				+ "<h1>404</h1>");
		sb.append("<p>Error locating resource: " + resourceName + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Generates a 400 error page for the given URI.
	 * 
	 * @param uri
	 *            the URI request that could not be completed.
	 * @return a String representation of the HTML page to be returned to the user agent.
	 */
	public static String get400BadRequestPage(URI uri) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Could not find request</title></head>" + "<body>"
				+ "<h1>400</h1>");
		sb.append("Malformed request: " + uri + "</p>");
		sb.append("</body></html>");
		return sb.toString();
	}
}
