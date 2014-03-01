package org.apparatus_templi;

public class ServerDriverTest {

	public static void main(String[] args) {
		SimpleHttpServer server = new SimpleHttpServer(80, true);
		server.run();

	}

}
