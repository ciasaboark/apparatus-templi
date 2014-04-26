package org.apparatus_templi.web.handler;

import java.io.IOException;
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
 * Handles request to restart service modules. Expects a query string in the form of
 * ?module=some_module. Does not check that 'some_model' is a valid module name, only passes the
 * given module name to {@link Coordinator#restartModule(String)} for further processing. Returns a
 * HTML page with a timed javascript redirect to send the user to the correct hostname and port
 * after a few seconds.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class RestartModuleHandler implements HttpHandler {
	private static final String TAG = "RestartModuleHandler";
	private final AbstractWebServer webserver;

	public RestartModuleHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		com.sun.net.httpserver.Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/html");

		String query = exchange.getRequestURI().getQuery();
		HashMap<String, String> queryTags = null;
		if (query != null) {
			queryTags = HttpHelper.processQueryString(exchange.getRequestURI().getQuery());
		}

		byte[] response = getResponse(queryTags.get("module"));
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();

		if (queryTags != null) {
			if (queryTags.containsKey("module")) {
				try {
					// sleep for a bit so the user agent can load resources references in the
					// response html before the server restarts
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Coordinator.restartModule(queryTags.get("module"));
			}
		}
	}

	private byte[] getResponse(String module) {
		assert module != null : "module to restart can not be null";
		byte[] returnBytes = null;

		byte[] templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
				+ "redirect.html");

		if (templateBytes != null) {
			String templateHtml = new String(templateBytes);
			if (module.equals("all")) {
				templateHtml = templateHtml.replace("!TIMEOUT!", "15");
			} else {
				templateHtml = templateHtml.replace("!TIMEOUT!", "8");
			}
			if ((module != null) && (module.equals("web") || module.equals("all"))) {
				// The address the new server will be listening on may have changed
				String newAddress = null;
				if (Coordinator.getPrefs().getPreference(Prefs.Keys.serverBindLocalhost)
						.equals("true")) {
					newAddress = HttpHelper.bestAddress();
				} else {
					newAddress = "localhost";
				}

				templateHtml = templateHtml.replace("!LOCATION!",
						webserver.getProtocol() + newAddress + ":"
								+ Coordinator.getPrefs().getPreference(Prefs.Keys.portNum)
								+ "/settings.html");
			} else {
				templateHtml = templateHtml.replace("!LOCATION!", webserver.getProtocol()
						+ webserver.getServerLocation() + ":" + webserver.getPort()
						+ "/settings.html");
			}
			returnBytes = templateHtml.getBytes();
		}

		return returnBytes;
	}
}
