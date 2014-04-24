package org.apparatus_templi.web.handler;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LogHandler implements HttpHandler {
	private static final String TAG = "LogHandler";
	private final AbstractWebServer webserver;

	public LogHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// Log.d(TAG,
		// "received request from " + exchange.getRemoteAddress() + " "
		// + exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/plain");

		byte[] response = getResponse();
		if (response != null) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		} else {
			response = PageGenerator.get404ErrorPage("logfile").getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}

	private byte[] getResponse() {
		StringBuilder sb = new StringBuilder("");
		for (String logLine : Log.getRecentLog()) {
			sb.append(logLine + "\n");
		}

		return sb.toString().getBytes();
	}
}
