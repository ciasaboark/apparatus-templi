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

import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles request to "/info.html"
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class InfoHandler implements HttpHandler {
	private static final String TAG = "InfoHandler";
	private final AbstractWebServer webserver;

	public InfoHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/html");

		byte[] response = getResponse();
		if (response != null) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		} else {
			response = PageGenerator.get404ErrorPage("info.html").getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}

	private byte[] getResponse() {
		byte[] returnBytes = null;

		byte[] templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "inc/template.html");
		byte[] indexBytes = HttpHelper
				.getFileBytes(webserver.getResourceFolder() + "inc/info.html");

		if (templateBytes != null && indexBytes != null) {
			String templateHtml = new String(templateBytes);
			String indexHtml = new String(indexBytes);
			templateHtml = templateHtml.replace("!PROTOCOL!", webserver.getProtocol());
			templateHtml = templateHtml.replace("!MAIN_CONTENT!", indexHtml.toString());
			templateHtml = templateHtml.replace("!JAVASCRIPT!",
					"<script type='text/javascript' src='/resource?file=js/info.js'></script>");
			returnBytes = templateHtml.getBytes();
		}

		return returnBytes;
	}
}
