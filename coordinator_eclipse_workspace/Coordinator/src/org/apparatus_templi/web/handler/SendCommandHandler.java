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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * A handler to send commands from the web frontend to a driver. Expects a query string in the form
 * of ?driver=<some driver>&command=<some command>. Responds with "OK" and an HTTP_OK header if the
 * {@link Driver#receiveCommand(String command)} returns true, otherwise responds with "FAIL" and an
 * HTTP_BAD_REQUEST header.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SendCommandHandler implements HttpHandler {
	private static final String TAG = "SendCommandHandler";
	private final AbstractWebServer webserver;

	public SendCommandHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/plain");

		String query = exchange.getRequestURI().getQuery();
		HashMap<String, String> queryTags = null;
		if (query != null) {
			queryTags = HttpHelper.processQueryString(exchange.getRequestURI().getQuery());
		}

		boolean commandSent = Coordinator.passCommand(TAG, queryTags.get("driver"),
				queryTags.get("command"));
		if (commandSent) {
			byte[] response = "OK".getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		} else {
			byte[] response = "FAIL".getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}
}
