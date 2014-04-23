package org.apparatus_templi.web.generator;

import java.net.URI;

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
