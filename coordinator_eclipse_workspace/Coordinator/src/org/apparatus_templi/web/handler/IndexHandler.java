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
 * Handles requests for "/index.html". Opens the contents of the template html file, then replaces
 * the main body content with that of the index.html template.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class IndexHandler implements HttpHandler {
	private static final String TAG = "IndexHandler";
	private final AbstractWebServer webserver;

	public IndexHandler(AbstractWebServer server) {
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
			response = PageGenerator.get404ErrorPage("index.html").getBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
			exchange.getResponseBody().write(response);
		}
		exchange.close();
	}

	private byte[] getResponse() {
		byte[] returnBytes = null;

		byte[] templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "inc/template.html");
		byte[] indexBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "inc/index.html");

		if (templateBytes != null && indexBytes != null) {
			String templateHtml = new String(templateBytes);
			String indexHtml = new String(indexBytes);
			templateHtml = templateHtml.replace("!PROTOCOL!", webserver.getProtocol());
			templateHtml = templateHtml.replace("!MAIN_CONTENT!", indexHtml.toString());
			returnBytes = templateHtml.getBytes();
		}

		return returnBytes;
	}
}
