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
import org.apparatus_templi.web.PasswordHash;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UpdatePasswordHandler implements HttpHandler {
	private static final String TAG = "SetFirstPasswordHandler";
	AbstractWebServer webserver;

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
		boolean credentialsSetBegin = Prefs.isCredentialsSet();

		InputStream in = exchange.getRequestBody();
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte buf[] = new byte[4096];
		for (int n = in.read(buf); n > 0; n = in.read(buf)) {
			bao.write(buf, 0, n);
		}
		String query = new String(bao.toByteArray());
		query = java.net.URLDecoder.decode(query, "UTF-8");

		byte[] response = {};

		// the password should only be set if one was not already set.
		final String username = Coordinator.readTextData("SYSTEM", "USERNAME");
		final String passHash = Coordinator.readTextData("SYSTEM", "PASSWORD");
		boolean passwordChanged = false;
		HashMap<String, String> credentials = new HashMap<String, String>();
		if (query != null) {
			credentials = HttpHelper.processQueryString(query);
		}
		String newUser = credentials.get("newuser");
		if (newUser == null) {
			newUser = "";
		}
		String newPass = credentials.get("newpass");
		if (newPass == null) {
			newPass = "";
		}

		// if there was no query string attached to the url then just read in the updatepass.html
		// file
		if (query.isEmpty()) {
			byte[] returnBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
					+ "inc/updatepass.html");

			if (returnBytes != null) {
				String templateHtml = new String(returnBytes);
				// add a form to input current user/pass if one has been set
				if (Prefs.isCredentialsSet()) {
					templateHtml = templateHtml
							.replace(
									"!OLDPASS!",
									"<div>Current Username <input id=\"olduser\" "
											+ "type=\"text\" name=\"olduser\"></div><div>Current Password <input id=\"oldpass\" "
											+ "type=\"password\" name=\"oldpass\"></div><hr />");
				} else {
					templateHtml = templateHtml.replace("!OLDPASS!", "");
				}
				templateHtml = templateHtml.replace("!WARNING!", "");
				returnBytes = templateHtml.getBytes();
			}

			response = returnBytes;
		}
		// a query string was attached
		else {

			// if a username and password have already been set then we need to validate
			if (Prefs.isCredentialsSet()) {
				// change the password and reset the web server
				try {
					if (query != null && !query.equals("")) {
						String oldUser = credentials.get("olduser");
						String oldPass = credentials.get("oldpass");

						boolean goodUser = (oldUser != null && oldUser.equals(username));
						boolean goodPass = (oldPass != null && PasswordHash.validatePassword(
								oldPass, passHash));

						if (goodUser && goodPass) {
							Coordinator.storeTextData("SYSTEM", "USERNAME", newUser);
							String hash;
							if (newPass.equals("")) {
								hash = "";
							} else {
								hash = PasswordHash.createHash(newPass);
							}
							Coordinator.storeTextData("SYSTEM", "PASSWORD", hash);
							response = getResponse(true);
							passwordChanged = true;
						}
					}
				} catch (Exception e) {
					Log.e(TAG, "Unable to generate new password hash: " + e.getMessage());
				}
			} else if ((username == null || "".equals(username))
					|| (passHash == null || "".equals(passHash))) {
				// if the system does not have a username or password then we can write the
				// credentials
				// that we were given. If a blank password was given then we should store it as an
				// empty
				// string, and not generate a hash
				Coordinator.storeTextData("SYSTEM", "USERNAME", newUser);
				String hash = "";
				if (!newPass.equals("")) {
					try {
						hash = PasswordHash.createHash(newPass);
					} catch (Exception e) {
						// if the hash generation fails then an empty string will be written to the
						// password field.
						Log.e(TAG, "Unable to generate new password hash: " + e.getMessage());
					}
				}
				Coordinator.storeTextData("SYSTEM", "PASSWORD", hash);
				response = getResponse(true);
				passwordChanged = true;

			}

			if (!passwordChanged) {
				response = getResponse(false);
			}
		}

		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();

		// if a password was removed or added then the web server needs to be restarted so that the
		// authenticators can be removed or added as needed. If the user changed an already set
		// password then nothing needs to be done.
		boolean credentialsSetEnd = Prefs.isCredentialsSet();
		if (credentialsSetBegin != credentialsSetEnd) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				//
			}
			Coordinator.restartModule("web");
		}
	}

	private byte[] getResponse(boolean pass) {
		byte[] templateBytes;

		if (pass) {
			templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
					+ "inc/save_password.html");
		} else {
			templateBytes = HttpHelper.getFileBytes(webserver.getResourceFolder()
					+ "inc/updatepass.html");
			String templateHtml = new String(templateBytes);
			if (Prefs.isCredentialsSet()) {
				templateHtml = templateHtml
						.replace(
								"!OLDPASS!",
								"<div>Current Username <input id=\"olduser\" "
										+ "type=\"text\" name=\"olduser\"></div><div>Current Password <input id=\"oldpass\" "
										+ "type=\"password\" name=\"oldpass\"></div><hr />");
			} else {
				templateHtml = templateHtml.replace("!OLDPASS!", "");
			}
			templateHtml = templateHtml
					.replace(
							"!WARNING!",
							"<div class='warning'><p class=''><i class=\"fa fa-exclamation-triangle\"></i>&nbsp;&nbsp;Incorrect username or password.</div>");
			templateBytes = templateHtml.getBytes();
		}

		// String templateHtml = new String(templateBytes);
		return templateBytes;
	}
}
