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

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.AbstractWebServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler to return a virtual javascript document containing variables that will change during
 * runtime. Right now only returns the port number of server. May be removed at a later date.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class JsHandler implements HttpHandler {
	private static final String TAG = "JsHandler";
	private final AbstractWebServer webserver;

	public JsHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		byte[] response = getResponse();
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "application/javascript");
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	private byte[] getResponse() {
		String jsCode = "$portnum = " + Coordinator.getPrefs().getPreference(Prefs.Keys.portNum)
				+ ";";
		return jsCode.getBytes();
	}
}
