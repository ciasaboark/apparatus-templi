package org.apparatus_templi.web.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler to return the contents of any arbitrary file within the web resources folder. The MIME
 * type of the file is guessed using the Apache tika libary.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class ResourceHandler implements HttpHandler {
	private static final String TAG = "ResourceHandler";
	private final AbstractWebServer webserver;

	public ResourceHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		String query = exchange.getRequestURI().getQuery();
		if (query != null) {
			HashMap<String, String> queryTags = HttpHelper.processQueryString(exchange
					.getRequestURI().getQuery());
			if (queryTags.containsKey("file")) {
				// Log.d(TAG, "value of 'file': '" + queryTags.get("file") + "'");
				String resourceName = queryTags.get("file");
				resourceName = resourceName.replaceAll("\\.\\./", "");
				// the file was found and read correctly
				byte[] response = getResponse(resourceName);
				// get the MIME type of the file
				String file = Coordinator.getPrefs().getPreference(Prefs.Keys.webResourceFolder)
						+ resourceName;
				String mime = null;
				try (InputStream is = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(is)) {
					AutoDetectParser parser = new AutoDetectParser();
					Detector detector = parser.getDetector();
					Metadata md = new Metadata();
					md.add(Metadata.RESOURCE_NAME_KEY, resourceName);
					MediaType mediaType = detector.detect(bis, md);
					mime = mediaType.toString();
				}
				if (mime != null) {
					com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
					headers.add("Content-Type", mime);
				}
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			} else {
				// If the query string did not contain a key/value pair for file then the
				// request
				// + is malformed
				byte[] response = PageGenerator.get400BadRequestPage(exchange.getRequestURI())
						.getBytes();
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length);
				exchange.getResponseBody().write(response);
			}
		}
		exchange.close();
	}

	private byte[] getResponse(String resourceName) throws IOException {
		return HttpHelper.getFileBytes(webserver.getResourceFolder() + resourceName);
	}
}
