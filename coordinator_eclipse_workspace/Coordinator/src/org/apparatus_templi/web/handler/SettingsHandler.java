package org.apparatus_templi.web.handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;
import org.apparatus_templi.web.AbstractWebServer;
import org.apparatus_templi.web.EncryptedWebServer;
import org.apparatus_templi.web.HttpHelper;
import org.apparatus_templi.web.generator.PageGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler to process requests for "/settings.html". This virtual document is populated with the
 * current preference values, and contains a form to submit preferences back for saving.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SettingsHandler implements HttpHandler {
	private static final String TAG = "SettingsHandler";
	private final AbstractWebServer webserver;

	public SettingsHandler(AbstractWebServer server) {
		this.webserver = server;
	}

	private final String ENC_WARNING = "<i class=\"fa fa-exclamation-triangle\"></i>&nbsp;&nbsp;";

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
		if (templateBytes != null) {
			String template = new String(templateBytes);

			StringBuilder html = new StringBuilder();
			HashMap<String, String> prefs = Coordinator.getPrefs().getPreferencesMap();
			// remove any preferences that should be hidden from the frontend
			prefs.remove(Prefs.Keys.userName);
			prefs.remove(Prefs.Keys.userPass);

			String configFile = prefs.get(Prefs.Keys.configFile);

			if (webserver instanceof EncryptedWebServer) {
				if (!Prefs.isCredentialsSet()) {
					html.append("<div class='info-box' style='width:600px; display: block; margin-right: auto; margin-left: auto; "
							+ "cursor: pointer; font-size: smaller; text-align: center; padding: 10px'><p>No password has been set. "
							+ "Access will be unrestricted until you <a onclick='window.open(\"/set_password\", \"password_change\", "
							+ "\"toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=750,height=550\");'>"
							+ "set a password</a>.</p></div>");
				}
			}

			// TODO update to a form so that the settings can be sent back in a POST request
			html.append("<div id=\"prefs_form\">");

			// Buttons
			html.append("<div id='settings-buttons'>");
			// TODO what modules can be restarted?
			html.append("<span id=\"restart_all_button\" class=\"btn-group closed\" >"
					+ "<a class=\"btn btn-danger\" href=\"/restart_module?module=all\" title='Restarting the service will re-read "
					+ "preferences from config file, restart all driver, and re-initialize the web server and the serial connection.'>"
					+ "<i class=\"fa fa-refresh fa-fw\"></i> &nbsp;&nbsp;Restart Service</a>"
					+ "<a class=\"btn btn-danger dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">"
					+ "<span class=\"fa fa-caret-down\"></span></a>"
					+ "<ul class=\"dropdown-menu\">"
					+ "<li><a href=\"/restart_module?module=drivers\" title='Ask all drivers to terminate, then re-initialize all "
					+ "drivers.  Only drivers specified in the driver list will be started.'><i class=\"fa fa-refresh fa-fw\"></i> "
					+ "Restart Drivers</a></li><li><a href=\"/restart_module?module=web\" title='Restart the web server. This will "
					+ "bind the web server to a new address and port number if those settings have been changed.'><i class=\"fa "
					+ "fa-refresh fa-fw\"></i> Restart Web Server</a></li><li><a href=\"/restart_module?module=serial\" "
					+ "title='Re-initialize the serial connection, discarding all partial messages'><i class=\"fa fa-refresh "
					+ "fa-fw\"></i> Serial Connection</a></li></ul> </span>");

			// Save preferences button
			// if the config file is the default then we want the save preferences button to be
			// disabled until updated via javascript
			html.append("<span id='form_submit' ");
			if (configFile.equals(Coordinator.getPrefs().getDefPreference(Prefs.Keys.configFile))) {
				html.append("class='btn btn-success disabled'>");
			} else {
				html.append("class ='btn btn-success' ");
				html.append("form=\"prefs\" ");
				html.append("onclick = \"document.getElementById('prefs').submit()\" >");
			}
			html.append("<i class=\"fa fa-save\"></i>&nbsp;&nbsp;"
					+ "Save Preferences to <span id='btn_conf_file'>" + configFile
					+ "</span></div>");

			// end submit span
			html.append("</span>");

			// end buttons div
			// html.append("</div>");

			// clear the elements
			// html.append("<div class=\"clear\"></div>");

			html.append("<form name='prefs' id='prefs' action=\"update_settings\" "
					+ "method=\"POST\" >\n");

			// settings boxes div
			html.append("<div id='settings_boxes'>");

			// Preferences for the main section
			html.append("<div id='prefs_section_main' class='prefs_section info-box'><div class='title'>"
					+ "<i  class=\"fa fa-code-fork\"></i>&nbsp;Main" + "</div>");
			html.append("<div class='content'>");
			html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
					+ "<i class=\"fa fa-question-circle\" "
					+ "title=\""
					+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs().getPreferenceDesc(
							Prefs.Keys.configFile))
					+ "\"></i>&nbsp;"
					+ Prefs.Keys.configFile
					+ "</span><span "
					+ "class=\"pref_value\"><input id='f_config_file' type=\"text\" name=\""
					+ Prefs.Keys.configFile
					+ "\" value=\""
					+ prefs.get(Prefs.Keys.configFile)
					+ "\" onChange='updateConfigFile()' onkeypress='updateConfigFile()' onkeyup='updateConfigFile()' "
					+ "onBlur='updateConfigFile()' /></span></div><br />\n");
			prefs.remove(Prefs.Keys.configFile);
			for (String key : new String[] { Prefs.Keys.serialPort, Prefs.Keys.driverList,
					Prefs.Keys.logFile, Prefs.Keys.emailList, Prefs.Keys.debugLevel }) {
				String value = prefs.get(key);
				// the serial port name can be a null value, but writing a null string
				// + will print "null" (a non-existent serial port). Write "" instead.
				if (key.equals(Prefs.Keys.serialPort) && value == null) {
					value = "";
				}
				html.append("<div class=\"pref_input\"><span class=\"pref_key \">"
						+ "<i class=\"fa fa-question-circle\" "
						+ "title=\""
						+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs().getPreferenceDesc(
								key)) + "\"></i>&nbsp;"
						+ Coordinator.getPrefs().getPreferenceName(key) + "</span><span "
						+ "class=\"pref_value\"><input "
						+ ((key == Prefs.Keys.userPass) ? " type='password' " : " type='text'")
						+ " name=\"" + key + "\" value=\"" + value + "\" /></span></div><br />\n");
				prefs.remove(key);
			}
			if (Prefs.isCredentialsSet() && webserver instanceof EncryptedWebServer) {
				html.append("<div style='margin-right: auto; margin-left: auto; cursor: pointer; text-align: center'><p><a "
						+ "onclick='window.open(\"/set_password\", \"password_change\", \"toolbar=no,location=no,status=no,"
						+ "menubar=no,scrollbars=yes,resizable=yes,width=750,height=550\");'>Change password</a></p></div>");
			}
			html.append("</div></div>");

			// Preferences for web server
			html.append("<div id='prefs_section_webserver'  class='prefs_section info-box'><div class='title'>"
					+ "<i class=\"fa fa-cloud\"></i>&nbsp;Web Server" + "</div>");
			html.append("<div class='content'>");
			for (String key : new String[] { Prefs.Keys.portNum, Prefs.Keys.serverBindLocalhost,
					Prefs.Keys.encryptServer, Prefs.Keys.webResourceFolder }) {
				String value = prefs.get(key);
				html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
						+ "<i class=\"fa fa-question-circle \" "
						+ "title=\""
						+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs().getPreferenceDesc(
								key)) + "\"></i>&nbsp;"
						+ Coordinator.getPrefs().getPreferenceName(key) + "</span><span "
						+ "class=\"pref_value\"><input "
						+ (key.equals((Prefs.Keys.portNum)) ? " type='number' " : "")
						+ " type=\"text\" name=\"" + key + "\" value=\"" + value
						+ "\" /></span></div><br />\n");
				prefs.remove(key);
			}
			html.append("</div></div>");

			// Preferences for the Twitter service
			html.append("<div id='prefs_section_twitter' class='prefs_section info-box'><div class='title'>"
					+ "<i  class=\"fa fa-twitter\"></i>&nbsp;Twitter Service" + "</div>");
			html.append("<div class='content'>");
			for (String key : new String[] { Prefs.Keys.twtrAccess, Prefs.Keys.twtrAccessKey }) {
				html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
						+ "<i class=\"fa fa-question-circle \" "
						+ "title=\""
						+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs().getPreferenceDesc(
								key)) + "\"></i>&nbsp;"
						+ Coordinator.getPrefs().getPreferenceName(key) + "</span><span "
						+ "class=\"pref_value\"><input " + " type=\"password\" name=\"" + key
						+ "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
				prefs.remove(key);
			}
			html.append("</div>");
			html.append("<div class='warning'><p class=''>" + ENC_WARNING
					+ "All passwords are stored in plaintext.</div>");
			html.append("</div>");

			// Preferences for the email service
			html.append("<div id='prefs_section_email' class='info-box prefs_section'><div class='title'>"
					+ "<i  class=\"fa fa-envelope\"></i>&nbsp;Email Service" + "</div>");
			html.append("<div class='content'>");
			for (String key : new String[] { Prefs.Keys.emailAddress, Prefs.Keys.emailUsername,
					Prefs.Keys.emailPassword, Prefs.Keys.emailServer, Prefs.Keys.emailPort }) {
				html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
						+ "<i class=\"fa fa-question-circle \" "
						+ "title=\""
						+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs().getPreferenceDesc(
								key))
						+ "\"></i>&nbsp;"
						+ Coordinator.getPrefs().getPreferenceName(key)
						+ "</span><span "
						+ "class=\"pref_value\"><input "
						+ ((key == Prefs.Keys.emailPassword) ? "type = 'password' "
								: " type=\"text\"") + " name=\"" + key + "\" value=\""
						+ prefs.get(key) + "\" /></span></div><br />\n");
				prefs.remove(key);
			}
			html.append("</div>");
			html.append("<div class='warning'><p class=''>" + ENC_WARNING
					+ "All passwords are stored in plaintext.</div>");
			html.append("</div>");

			// Any remaining unclassified preferences
			if (!prefs.isEmpty()) {
				html.append("<div id='prefs_section_unknown' class='info-box prefs_section'><div class='title'>"
						+ "<i  class=\"fa fa-question\"></i>&nbsp;Uncategorized" + "</div>");
				html.append("<div class='content'>");
				for (String key : prefs.keySet()) {
					html.append("<div class=\"pref_input\"><span class=\"pref_key\">"
							+ "<i class=\"fa fa-question-circle \" "
							+ "title=\""
							+ StringEscapeUtils.escapeHtml4(Coordinator.getPrefs()
									.getPreferenceDesc(key)) + "\"></i>&nbsp;"
							+ Coordinator.getPrefs().getPreferenceName(key) + "</span><span "
							+ "class=\"pref_value\"><input " + " type=\"text\" name=\"" + key
							+ "\" value=\"" + prefs.get(key) + "\" /></span></div><br />\n");
				}
				html.append("</div></div>");
			}
			// end settings boxes div
			html.append("</div>");
			html.append("</form>");
			// clear the elements
			html.append("<div class=\"clear\"></div>");
			// html.append("<hr class=\"fancy-line\"></hr>");

			// buttons div
			html.append("<div id=\"settings_buttons_div\">");

			html.append("</div></div>");
			template = template.replace("!PROTOCOL!", webserver.getProtocol());
			template = template.replace("!MAIN_CONTENT!", html.toString());
			returnBytes = template.getBytes();
		}
		return returnBytes;
	}
}
