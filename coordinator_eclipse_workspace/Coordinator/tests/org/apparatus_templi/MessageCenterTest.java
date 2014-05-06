package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests public methods in MessageCenter
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class MessageCenterTest {
	private MessageCenter mc;

	@BeforeClass
	public static void beforeClass() {
		System.out
				.println("Some test on message center can take a few minutes to complete.  Timeouts have been set to 20 minutes");
	}

	@Before
	public void begin() {
		// System.out.println("#################     BEGIN     #################");
		mc = MessageCenter.getInstance();
		Log.setLogLevel(Log.LEVEL_ERR);
	}

	@After
	public void after() {
		mc.stopReadingMessges();
		mc.flushMessages();
		mc.setSerialConnection(null);
		// System.out.println("-----------------      END      -----------------\n\n");
	}

	/*
	 * Test serial Connections
	 */

	@Test
	public void setNullSerialConnection() {
		System.out.println("Setting null serial connection");
		mc.setSerialConnection(null);
	}

	@Test
	public void setDummySerialConnection() {
		System.out.println("Setting dummy serial Connection");
		mc.setSerialConnection(new DummySerialConnection());
	}

	@Test
	public void setUsbSerialConnection() {
		System.out.println("Setting usb serial connection");
		// mc.setSerialConnection(new UsbSerialConnection());
	}

	@Test
	public void begingReadingMessagesWithNoConnection() {
		System.out.println("Begin reading messages with no serial connection");
		mc.setSerialConnection(null);
		mc.beginReadingMessages();
	}

	/*
	 * Test sending Messages
	 */

	@Test
	public void sendTextCommandWithNoConnection() {
		System.out.println("Sending text command with no serial connection");
		mc.setSerialConnection(null);
		assertTrue(mc.sendCommand("foo", "bar") == false);
	}

	@Test
	public void sendBinCommandWithNoConnection() {
		System.out.println("Sending binary command with no serial connection");
		mc.setSerialConnection(null);
		assertTrue(mc.sendBinary("foo", new byte[] { 0x0d }) == false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendTextCommandWithNullDestination() {
		System.out.println("Sending text message with null destination and dummy connection");
		mc.setSerialConnection(new DummySerialConnection());
		mc.sendCommand(null, "bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendTextCommandWithNullCommand() {
		System.out.println("Sending text message with null command and dummy connection");
		mc.setSerialConnection(new DummySerialConnection());
		mc.sendCommand("foo", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendBinCommandWithNullDestination() {
		System.out.println("Sending bin message with null destination and dummy connection");
		mc.setSerialConnection(new DummySerialConnection());
		mc.sendBinary(null, new byte[] { 0x0d });
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendBinCommandWithNullCommand() {
		System.out.println("Sending binary message with null command and dummy connection");
		mc.setSerialConnection(new DummySerialConnection());
		mc.sendCommand("foo", null);
	}

	@Test
	public void sendTextCommandWithDummyDriver() {
		System.out.println("Sending text command with dummy serial driver");
		mc.setSerialConnection(new DummySerialConnection());
		assertTrue(mc.sendCommand("foo", "bar") == true);
	}

	@Test
	public void sendBinCommandWithDummyDriver() {
		System.out.println("Sending bin command with dummy serial driver");
		mc.setSerialConnection(new DummySerialConnection());
		assertTrue(mc.sendBinary("foo", new byte[] { 0x0D }) == true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendVeryLargeBinCommandOverMaxSize() {
		System.out
				.println("Sending very large binary command that is over the max message size.  This test should fail fast.");
		mc.setSerialConnection(new DummySerialConnection());
		ArrayList<Byte> byteArrayList = new ArrayList<Byte>();
		while (byteArrayList.size() < (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
			byteArrayList.add((byte) 0x0D);
		}
		byteArrayList.add((byte) 0x0D);
		System.out.println("Created very large byte array of size: " + byteArrayList.size());
		System.out.println("This test should fail immediately");

		Byte[] byteObjectArray = byteArrayList.toArray(new Byte[] {});
		byte[] byteArray = new byte[byteObjectArray.length];
		int i = 0;
		for (Byte b : byteObjectArray) {
			byteArray[i] = b;
			i++;
		}

		mc.sendBinary("foo", byteArray);

	}

	@Test(expected = IllegalArgumentException.class)
	public void sendVeryLargeTextCommandOverMaxSize() {
		System.out
				.println("Sending very large text command that is over the max message size.  This test should fail fast.");
		mc.setSerialConnection(new DummySerialConnection());

		StringBuilder sb = new StringBuilder();
		// build a string that is 1 byte over of the max command size
		StringBuilder fragment = new StringBuilder();
		// create a fragment
		while (fragment.length() < Message.MAX_DATA_SIZE) {
			fragment.append("1");
		}
		// System.out.println("created fragment of " + fragment.length() + " characters");
		System.out.println("Filling command to max size");
		while (sb.length() < (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
			sb.append(fragment.toString());
			// System.out.print(".");
		}
		// tack on one additional byte of data to push the command over the size limit
		sb.append("1");

		System.out.println("\nCreated very large command of size: " + sb.length());
		System.out.println("This test should fail immediately");

		mc.sendCommand("foo", sb.toString());
	}

	@Test
	// set a timeout to 20 minutes
	public void sendVeryLargeBinCommandUnderMaxSize() {
		// System.out
		// .println("Sending very large binary command that is under the max message size.  Timeout of 20 minutes enabled.");
		// mc.setSerialConnection(new DummySerialConnection());
		// // byte[] byteSegment = new byte[69];
		//
		// ArrayList<Byte> byteArrayList = new ArrayList<Byte>();
		// while (byteArrayList.size() < (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
		// byteArrayList.add((byte) 0x0D);
		// }
		// System.out.println("Created very large byte array of size: " + byteArrayList.size());
		// System.out.println("This test will take a few minutes to complete");
		//
		// Byte[] byteObjectArray = byteArrayList.toArray(new Byte[] {});
		// byte[] byteArray = new byte[byteObjectArray.length];
		// int i = 0;
		// for (Byte b : byteObjectArray) {
		// byteArray[i] = b;
		// i++;
		// }
		//
		// assertTrue(mc.sendBinary("foo", byteArray) == true);

	}

	@Test
	public void sendVeryLargeTextCommandUnderMaxSize() {
		// System.out
		// .println("Sending very large text command that is under the max message size.  Timeout of 20 minutes enabled.");
		// StringBuilder sb = new StringBuilder();
		// // build a string that is 1 byte over of the max command size
		// StringBuilder fragment = new StringBuilder("1");
		// // create a fragment
		// while (fragment.length() < Message.MAX_DATA_SIZE) {
		// fragment.append("1");
		// // System.out.println(fragment.length());
		// }
		// System.out.println("created fragment of " + fragment.length() + " characters");
		// System.out.println("Filling command to max size");
		// while (sb.length() < (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
		// sb.append(fragment.toString());
		// // System.out.print(".");
		// }
		// System.out.println("\nCreated very large command of size: " + sb.length());
		// System.out.println("This test will take a few minutes to complete");
		// mc.setSerialConnection(new DummySerialConnection());
		// assertTrue(mc.sendCommand("foo", sb.toString()) == true);
	}

	/*
	 * Test reading messages
	 */

	@Test
	public void readMessageAfterFlushing() {
		System.out.println("read message after flushing");
		mc.stopReadingMessges();
		mc.flushMessages();
		assertTrue(!mc.isMessageAvailable());
		assertTrue(mc.getMessage() == null);
	}

}
