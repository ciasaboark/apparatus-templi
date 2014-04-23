package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.apparatus_templi.web.MultiThreadedHttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServerTest {

	@Before
	public void before() {
		System.out.println("#################     BEGIN     #################");
	}

	@After
	public void after() {
		System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test
	public void startLoopBackServer() throws Exception {
		System.out.println("Starting loopback server on port 8004");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer();
		assertTrue(s.getServerLocation() != null);
		assertTrue(s.getPort() != 0);
		assertTrue(s.getSocket().getAddress().isLoopbackAddress());
		s.terminate();
	}

	@Test(expected = SocketException.class)
	public void startLoopBackServerLowPort() throws Exception {
		System.out.println("Starting loopback server on port 80");
		new MultiThreadedHttpServer(new InetSocketAddress(80));
	}

	@Test
	public void startLoopBackServerWithIncPort() throws Exception {
		System.out.println("Starting loopback server on port 80 (or next avaiable port)");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer();
		assertTrue(s.getServerLocation() != null);
		assertTrue(s.getPort() != 0);
		s.terminate();
	}

	@Test
	public void startLocalHostServerWithIncPort() throws Exception {
		System.out.println("Starting localhost server on port 8000 or next available port");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer(new InetSocketAddress(
				InetAddress.getLocalHost(), 0));
		assertTrue(s.getServerLocation() != null);
		assertTrue(s.getSocket().getAddress().isSiteLocalAddress());
		s.terminate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setNullResourceFolder() throws Exception {
		System.out.println("Setting null resource folder");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer();
		s.setResourceFolder(null);
		s.terminate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setFileAsResourceFolder() throws Exception {
		System.out.println("Setting file as resource folder");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer();
		s.setResourceFolder("coordinator.conf");
		s.terminate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setNonExistantResourceFolder() throws Exception {
		System.out.println("Setting non-existant folder as resource folder");
		MultiThreadedHttpServer s = new MultiThreadedHttpServer();
		s.setResourceFolder("z:\\t:\\foo\\"); // if thats a real path...
		s.terminate();
	}

	@Test(expected = SocketException.class)
	public void startServerOnOccupiedPort() throws Exception {
		System.out.println("Starting two servers on same port number and interface");
		MultiThreadedHttpServer s1 = new MultiThreadedHttpServer(new InetSocketAddress(
				InetAddress.getLoopbackAddress(), 9999));
		MultiThreadedHttpServer s2 = new MultiThreadedHttpServer(new InetSocketAddress(
				InetAddress.getLoopbackAddress(), 9999));
	}
}
