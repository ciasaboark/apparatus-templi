package org.apparatus_templi.web.handler;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles requests for "/about.html". Opens the contents of the template html file, then replaces
 * the main body content with that of the about.html template.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class UpdatePasswordHandler implements HttpHandler {
	private static final String TAG = "AboutHandler";
	private final AbstractWebServer webserver;

	public UpdatePasswordHandler(AbstractWebServer server) {
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
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	private byte[] getResponse() {
		byte[] returnBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "inc/updatepass.html");

		if (returnBytes != null) {
			String templateHtml = new String(returnBytes);
			String oldUser = Coordinator.readTextData("SYSTEM", "USERNAME");
			String oldPass = Coordinator.readTextData("SYSTEM", "PASSWORD");
			// add a form to input current user/pass if one has been set
			if ((oldUser != null || !"".equals(oldUser))
					&& (oldPass != null || !"".equals(oldPass))) {
				templateHtml = templateHtml
						.replace(
								"!OLDPASS!",
								"<div>Current Username <input id=\"olduser\" "
										+ "type=\"text\" name=\"olduser\"></div><div>Current Password <input id=\"oldpass\" "
										+ "type=\"password\" name=\"oldpass\"></div><hr />");
			}
			templateHtml = templateHtml.replace("!WARNING!", "");
			returnBytes = templateHtml.getBytes();
		}

		return returnBytes;
	}
}