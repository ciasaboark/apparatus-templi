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

package org.apparatus_templi.web.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Properties;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles requests for "/about.html". Opens the contents of the template html file, then replaces
 * the main body content with that of the about.html template.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SettingsXmlHandler implements HttpHandler {
	private static final String TAG = "AboutHandler";
	private final AbstractWebServer webserver;

	public SettingsXmlHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "application/xml");

		byte[] response = getResponseAlt();
		if (response != null) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		} else {
			response = PageGenerator.get404ErrorPage("settings.xml").getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}

	private byte[] getResponse() {
		HashMap<String, String> prefMap = Coordinator.getPrefs().getPreferencesMap();
		Properties props = new Properties();
		props.putAll(prefMap);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			props.storeToXML(os, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os.toByteArray();
	}

	private byte[] getResponseAlt() {
		StringBuilder xml = new StringBuilder();
		Prefs prefs = Coordinator.getPrefs();
		HashMap<String, String> curPrefs = prefs.getPreferencesMap();
		xml.append("<preferences>\n");
		for (String key : Prefs.Keys.getKeyset()) {
			xml.append("<entry>\n");
			// add the key name
			xml.append("\t<key>");
			xml.append(key);
			xml.append("</key>\n");

			// add the current value
			xml.append("\t<value>");
			xml.append(curPrefs.get(key));
			xml.append("</value>\n");

			// add the short name
			xml.append("\t<name>");
			xml.append(prefs.getPreferenceName(key));
			xml.append("</name>\n");

			// add the long description
			xml.append("\t<descr>");
			xml.append(prefs.getPreferenceDesc(key));
			xml.append("</descr>\n");

			xml.append("</entry>\n");
		}
		xml.append("</preferences>");

		return xml.toString().getBytes();
	}
}