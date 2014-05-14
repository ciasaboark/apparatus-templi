/*
 * Copyright (c) 2014, Jonathan Nelson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.apparatus_templi;

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

	/*
	 * These tests are disabled for now. Before they can be re-enabled we need some way to pass
	 * preferences to the web server (and have it read from that preferences object instead of
	 * trying to pull from Coordinator).
	 */

	// @Test
	// public void startLoopBackServerWithIncPort() throws Exception {
	// System.out.println("Starting loopback server on port 80 (or next avaiable port)");
	// WebServer s = new WebServer();
	// assertTrue(s.getServerLocation() != null);
	// assertTrue(s.getPort() != 0);
	// s.terminate();
	// }

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
