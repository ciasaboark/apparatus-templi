package org.apparatus_templi.web.handler;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.HttpHelper;

import com.sun.management.OperatingSystemMXBean;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles requests for system level information. Looks for requests matching
 * 
 * <pre>
 * /system_status?status=X
 * </pre>
 * 
 * , where X can be one of 'uptime', 'freedisk', or 'loadavg'.
 * <p>
 * uptime: returns the number of seconds that the service has been up
 * </p>
 * <p>
 * freedisk: returns the number of free MB of disk space (space is calculated on the partition
 * holding the current working directory).
 * </p>
 * <p>
 * loadavg: retuns a floating point representation of the current load average of the system. A
 * value of 0.0 is 0% CPU usage, while a value of 1.0 represents 100% CPU usage.
 * </p>
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public class GetSysStatusHandler implements HttpHandler {
	private static final String TAG = "GetSysStatusHandler";
	private final AbstractWebServer webserver;

	public GetSysStatusHandler(AbstractWebServer server) {
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
		byte[] response = {};

		switch (queryTags.get("status")) {
		case "uptime":
			response = String.valueOf(Coordinator.getUptime()).getBytes();
			break;
		case "freedisk":
			response = String.valueOf(new File(".").getFreeSpace() / (1024 * 1024)).getBytes();
			break;
		case "loadavg":
			OperatingSystemMXBean osBean = ManagementFactory
					.getPlatformMXBean(OperatingSystemMXBean.class);
			response = String.valueOf(osBean.getSystemCpuLoad()).getBytes();
			break;
		case "modules":
			ArrayList<String> moduleList = Coordinator.getKnownModules();
			StringBuilder sb = new StringBuilder();
			for (String module : moduleList) {
				sb.append("<p>" + module + "</p>");
			}
			response = sb.toString().getBytes();
			break;
		}
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}
}
