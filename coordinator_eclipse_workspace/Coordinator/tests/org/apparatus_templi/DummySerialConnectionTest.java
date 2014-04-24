package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DummySerialConnectionTest {

	private DummySerialConnection dc;

	@Before
	public void before() {
		System.out.println("#################     BEGIN     #################");
		dc = new DummySerialConnection();
	}

	@After
	public void after() {
		System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test
	public void isConnected() {
		System.out.println("Testing dummy serial connection is connected");
		assertTrue(dc.isConnected());
	}

	@Test
	public void close() {
		System.out.println("Closing dummy serial connection");
		dc.close();
		assertTrue(!dc.isConnected());
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendNullData() {
		System.out.println("Sending null data");
		dc.writeData(null);
	}

	@Test
	public void sendByteData() {
		System.out.println("Sending data bytes");
		assertTrue(dc.writeData(new byte[] { 0x0d }));
	}

}
