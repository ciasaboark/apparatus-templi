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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handle request for a driver's widget XML. Calls {@link Coordinator#requestWidgetXML(String)} to
 * retrieve the Driver's widget XML data.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class WidgetXmlHandler implements HttpHandler {
	private static final String TAG = "WidgetXmlHandler";
	private final AbstractWebServer webserver;

	public WidgetXmlHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// TODO
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		HashMap<String, String> queryMap = HttpHelper.processQueryString(exchange.getRequestURI()
				.getQuery());
		byte[] response;
		if (queryMap.containsKey("driver")) {
			String driverName = queryMap.get("driver");
			response = getResponse(driverName);
			if (response != null) {
				com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
				headers.add("Content-Type", "application/xml");
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				response = PageGenerator.get404ErrorPage(
						"Driver " + driverName + " did not respond").getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
				exchange.getResponseBody().write(response);
			}
		} else {
			response = PageGenerator.get404ErrorPage("No driver specified").getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}

	private byte[] getResponse(String driverName) {
		byte[] response = null;
		String widgetXml = Coordinator.requestWidgetXML(driverName);
		if (widgetXml != null) {
			response = widgetXml.getBytes();
		}
		return response;
	}
}