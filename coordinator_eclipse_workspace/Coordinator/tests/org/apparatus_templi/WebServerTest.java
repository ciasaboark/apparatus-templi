package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

import org.apparatus_templi.web.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests public methods in WebServer
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class WebServerTest {
	private Prefs preferences;

	@Before
	public void before() {
		// System.out.println("#################     BEGIN     #################");
		preferences = new Prefs();
		// use the default preferences for all tests
		HashMap<String, String> defPrefs = preferences.getDefPreferencesMap();
		for (String key : defPrefs.keySet()) {
			preferences.putPreference(key, defPrefs.get(key));
		}
	}

	@After
	public void after() {
		// System.out.println("-----------------      END      -----------------\n\n");
	}

	// @Test
	// public void startLoopBackServer() throws Exception {
	// System.out.println("Starting loopback server on port 8004");
	// WebServer s = new WebServer();
	// assertTrue(s.getServerLocation() != null);
	// assertTrue(s.getPort() != 0);
	// assertTrue(s.getSocket().getAddress().isLoopbackAddress());
	// s.terminate();
	// }

	@Test(expected = SocketException.class)
	public void startLoopBackServerLowPort() throws Exception {
		System.out.println("Starting loopback server on port 80");
		new WebServer(new InetSocketAddress(80));
	}

	// @Test
	// public void startLoopBackServerWithIncPort() throws Exception {
	// System.out.println("Starting loopback server on port 80 (or next avaiable port)");
	// WebServer s = new WebServer();
	// assertTrue(s.getServerLocation() != null);
	// assertTrue(s.getPort() != 0);
	// s.terminate();
	// }

	@Test
	public void startLocalHostServerWithIncPort() throws Exception {
		System.out.println("Starting localhost server on port 8000 or next available port");
		WebServer s = new WebServer(new InetSocketAddress(InetAddress.getLocalHost(), 0));
		assertTrue(s.getServerLocation() != null);
		assertTrue(s.getSocket().getAddress().isSiteLocalAddress());
		s.terminate();
	}

	// @Test(expected = IllegalArgumentException.class)
	// public void setNullResourceFolder() throws Exception {
	// System.out.println("Setting null resource folder");
	// WebServer s = new WebServer();
	// s.setResourceFolder(null);
	// s.terminate();
	// }

	// @Test(expected = IllegalArgumentException.class)
	// public void setFileAsResourceFolder() throws Exception {
	// System.out.println("Setting file as resource folder");
	// WebServer s = new WebServer();
	// s.setResourceFolder("coordinator.conf");
	// s.terminate();
	// }

	// @Test(expected = IllegalArgumentException.class)
	// public void setNonExistantResourceFolder() throws Exception {
	// System.out.println("Setting non-existant folder as resource folder");
	// WebServer s = new WebServer();
	// s.setResourceFolder("z:\\t:\\foo\\"); // if thats a real path...
	// s.terminate();
	// }

	@SuppressWarnings("unused")
	@Test(expected = SocketException.class)
	public void startServerOnOccupiedPort() throws Exception {
		System.out.println("Starting two servers on same port number and interface");
		WebServer s1 = new WebServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 9999));
		WebServer s2 = new WebServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 9999));
	}
}
