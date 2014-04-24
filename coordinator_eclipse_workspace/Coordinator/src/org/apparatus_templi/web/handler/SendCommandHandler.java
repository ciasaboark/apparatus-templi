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
