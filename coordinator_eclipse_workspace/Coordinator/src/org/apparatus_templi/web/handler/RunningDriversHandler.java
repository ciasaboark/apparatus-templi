package org.apparatus_templi.web.handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handle requests for a list of running drivers. Generates XML in the form of:
 * 
 * <pre>
 * <?xml version='1.0'?>
 * <ModuleList>
 * 		<Module name='this driver name' />
 * 		<Module name='another driver name' />
 * </ModuleList>
 * </pre>
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * @author Christopher Hagler <haglerchristopher@gmail.com>
 * 
 */
public class RunningDriversHandler implements HttpHandler {
	private static final String TAG = "RunningDriversHandler";
	private final AbstractWebServer webserver;

	public RunningDriversHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	private final String xmlVersion = "<?xml version='1.0'?>";
	private final String header = "<ModuleList>";
	private final String footer = "</ModuleList>";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Log.d(TAG,
				"received request from " + exchange.getRemoteAddress() + " "
						+ exchange.getRequestMethod() + ": '" + exchange.getRequestURI() + "'");
		// HashMap<String, String> queryTags =
		// SimpleHttpServer.processQueryString(exchange.getRequestURI().getQuery());
		// Log.d(TAG, "value of 'foo': " + queryTags.get("foo"));
		byte[] response = getResponse();
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	private byte[] getResponse() {
		String xml = xmlVersion + header;
		ArrayList<String> runningDrivers = Coordinator.getLoadedDrivers();
		for (String s : runningDrivers) {
			xml += "<Module name='" + s + "' />";
		}
		xml += footer;
		return xml.getBytes();
	}
}
