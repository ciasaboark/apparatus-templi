package org.apparatus_templi.web.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * A handler to accept new preference values from POST data, then write that data back to the
 * configuration file specified.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class UpdateSettingsHandler implements HttpHandler {
	private static final String TAG = "UpdateSettingsHandler";
	private final AbstractWebServer webserver;

	public UpdateSettingsHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	private final Prefs prefs = Coordinator.getPrefs();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/html");
		boolean newServerLoopback = true;
		try {
			InputStream in = exchange.getRequestBody();
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			byte buf[] = new byte[4096];
			for (int n = in.read(buf); n > 0; n = in.read(buf)) {
				bao.write(buf, 0, n);
			}
			String query = new String(bao.toByteArray());
			query = java.net.URLDecoder.decode(query, "UTF-8");
			// Log.d(TAG, "update_settings query body: " + query);
			if (query != null && !query.equals("")) {
				HashMap<String, String> newPrefs = HttpHelper.processQueryString(query);
				prefs.savePreferences(newPrefs);
				if (newPrefs.get(Prefs.Keys.serverBindLocalhost).equals("true")) {
					newServerLoopback = false;
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "unable to update preferences");
		}

		byte[] response = getResponse();
		// headers.add("Location", getProtocol() + getServerLocation() + ":" + getPort()
		// + "/settings.html");
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	private byte[] getResponse() {
		byte[] returnBytes = null;

		byte[] templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "inc/redirect.html");

		if (templateBytes != null) {
			String templateHtml = new String(templateBytes);
			templateHtml = templateHtml.replace("!PROTOCOL!", webserver.getProtocol());
			templateHtml = templateHtml.replace("!TIMEOUT!", "4");
			templateHtml = templateHtml.replace(
					"!LOCATION!",
					webserver.getProtocol() + webserver.getServerLocation() + ":"
							+ webserver.getPort() + "/settings.html");
			returnBytes = templateHtml.getBytes();
		}

		return returnBytes;
	}
}
