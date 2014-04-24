package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apparatus_templi.driver.BlankControllerDriver;
import org.apparatus_templi.driver.Driver;
import org.apparatus_templi.event.ProximityEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordinatorTest {

	private Coordinator coordinator = null;

	@Before
	public void before() {
		System.out.println("#################     BEGIN     #################");
		coordinator = new Coordinator();
	}

	@After
	public void after() {
		System.out.println("-----------------      END      -----------------\n\n");
	}

	/*
	 * Test Receiving Events
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveEventFromNullDriver() {
		System.out.println("Generating event for null driver");
		Coordinator.receiveEvent(null, new ProximityEvent());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveNullEventFromDriver() {
		System.out.println("Generating null event for driver");
		Coordinator.receiveEvent(new BlankControllerDriver(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveNullEventFromNullDriver() {
		System.out.println("Generating nullo event for null driver");
		Coordinator.receiveEvent(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveEventFromInvalidDriver() {
		System.out.println("Generating event for driver not EventGenerator");
		Coordinator.receiveEvent(new BlankControllerDriver(), new ProximityEvent());
	}

	/*
	 * Test sending messages from unloaded drivers
	 */
	@Test
	public void testSendNullCommandFromDriver() {
		// TODO expose method to set coordinator message center, load dummy serial connection
		// This test is succeeding only because the serial link is not ready, not because driver is
		// not loaded
		System.out.println("Sending null command from unloaded BlankControllerDriver");
		assertTrue(Coordinator.sendCommand(new BlankControllerDriver(), null) == false);
		assertTrue(Coordinator.sendBinary(new BlankControllerDriver(), null) == false);
	}

	@Test
	public void testSendCommandFromNullDriver() {
		System.out.println("Sending command from null driver");
		assertTrue(Coordinator.sendCommand(null, "some command") == false);
		assertTrue(Coordinator.sendBinary(null, new byte[] { 0x0d }) == false);
	}

	@Test
	public void testSendNullCommandFromNullDriver() {
		System.out.println("Sending null command from null driver");
		assertTrue(Coordinator.sendCommand(null, null) == false);
		assertTrue(Coordinator.sendBinary(null, null) == false);
	}

	/*
	 * Test private methods
	 */

	@Test(expected = IllegalArgumentException.class)
	public void routeNullMessage() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, InvocationTargetException {
		Method method;
		method = Coordinator.class.getDeclaredMethod("routeIncomingMessage", Message.class);
		method.setAccessible(true);
		try {
			method.invoke(Coordinator.class, new Object[] { null });
		} catch (IllegalArgumentException e) {

		}
	}

	/*
	 * Test sending messages from loaded drivers
	 */

	// TODO

	/*
	 * Test storing data to database
	 */

	@Test
	public void testStoreNullValuesToDatabase() {
		System.out.println("Storing null string to database");
		assertTrue(Coordinator.storeTextData("unloaded driver", "foo", null) == 0);
		System.out.println("Storing null byte array to database");
		assertTrue(Coordinator.storeBinData("unloaded driver", "foo", null) == 0);
	}

	@Test
	public void testStoreStringToDatabaseFromNullDriver() {
		System.out.println("Storing string to database with null driver name");
		assertTrue(Coordinator.storeTextData(null, "foo", "bar") == 0);
	}

	@Test
	public void testStoreByteArrayToDatabaseFromNullDriver() {
		System.out.println("Storing byte to database with null driver name");
		assertTrue(Coordinator.storeBinData(null, "foo", new byte[] { 0x0D }) == 0);
	}

	@Test
	public void testStoreStringWithNullTagToDatabase() {
		System.out.println("Storing string to database with null tag");
		assertTrue(Coordinator.storeTextData("unloaded driver", null, "bar") == 0);
		System.out.println("Storing byte array to database with null tag");
		assertTrue(Coordinator.storeBinData("unloaded driver", null, new byte[] { 0x0d }) == 0);
	}

	@Test
	public void testStoreByteArrayToDatabase() {
		System.out.println("Storing byte array to database");
		int returnCode = Coordinator.storeBinData("some driver", "foo", new byte[] { 0x0d });
		assertTrue(returnCode == 1 || returnCode == -1);
	}

	@Test
	public void testStoreStringToDatabase() {
		System.out.println("Storing string to database");
		int returnCode = Coordinator.storeTextData("some driver", "foo", "bar");
		assertTrue(returnCode == 1 || returnCode == -1);
	}

	@Test
	public void testOverwriteStringToDatabase() {
		System.out.println("Writing string to database twice");
		Coordinator.storeTextData("some driver", "foo", "bar");
		int returnCode = Coordinator.storeTextData("some driver", "foo", "bar");
		assertTrue(returnCode == -1);
	}

	@Test
	public void testOverwriteByteToDatabase() {
		System.out.println("Writing byte array to database twice");
		Coordinator.storeBinData("some driver", "foo", new byte[] { 0x0D });
		int returnCode = Coordinator.storeBinData("some driver", "foo", new byte[] { 0x0D });
		assertTrue(returnCode == -1);
	}

	/*
	 * Test waking drivers
	 */
	@Test(expected = IllegalArgumentException.class)
	public void wakeNullDriver() {
		System.out.println("Waking null driver");
		Coordinator.wakeSelf(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wakeDriverBeforeLoad() {
		System.out.println("Waking unloaded driver");
		Coordinator.wakeSelf(new BlankControllerDriver());
	}

	@Test
	public void wakeDriverAfterLoadBeforeSleep() {
		System.out.println("Waking loaded driver that is not sleeping");
		// TODO load driver
	}

	@Test
	public void wakeDriverAfterLoadAfterSleep() {
		System.out.println("Waking loaded driver that is sleeping");
		// TODO
	}

	/*
	 * Test getting thread IDs
	 */

	@Test
	public void getThreadIdNullDriver() {
		System.out.println("Getting thread id of null driver");
		assertTrue(Coordinator.getDriverThreadId(null) == -1);
	}

	@Test
	public void getThreadIdUnloadedDriver() {
		System.out.println("Getting thread id of unloaded driver");
		assertTrue(Coordinator.getDriverThreadId(new BlankControllerDriver()) == -1);
	}

	@Test
	public void getThreadIdLoadedNotStartedDriver() {
		System.out.println("Getting thread id of loaded, but not started, driver");
		Driver d = new BlankControllerDriver();
		// TODO load driver
		assertTrue(Coordinator.getDriverThreadId(d) == -1);
	}

	@Test
	public void getThreadIdLoadedRunningDriver() {
		System.out.println("Getting thread id of loaded and running driver");
		// TODO load and start driver
		Driver d = new BlankControllerDriver();
		assertTrue(Coordinator.getDriverThreadId(d) != -1);
	}

	/**************************************************
	 * Unfinished tests
	 */
	@Test
	public void testRouteIncommingNullMessage() {
		assertTrue(coordinator != null);
	}

	@Test
	public void testCoordinatorSetup() {

	}

	@Test
	public void testLoadNullDriver() {

	}

	@Test
	public void testLoadInvalidDriver() {

	}

	@Test
	public void testWakeNullDriver() {

	}

	@Test
	public void testWakeNonExistantDriver() {

	}

	@Test
	public void testParseCommandLineOptions() {

	}

	@Test
	public void testOpenSerialConnection() {

	}

	@Test
	public void testStartWebServer() {

	}

	@Test
	public void testStopWebServer() {

	}

	@Test
	public void testStartDriverS() {

	}

	@Test
	public void testStopDrivers() {

	}

	@Test
	public void testRestartSerialConnection() {

	}

	@Test
	public void testExitWithReason() {

	}

	@Test
	public void testSendCommand() {

	}

	@Test
	public void testSendCommandAndWait() {

	}

	@Test
	public void testSendBinary() {

	}

	@Test
	public void testStoreTextData() {

	}

	@Test
	public void testStoreBinData() {

	}

	@Test
	public void testReadTextData() {

	}

	@Test
	public void testReadBinData() {

	}

	@Test
	public void testScheduleSleep() {

	}

	@Test
	public void testPassCommand() {

	}

	@Test
	public void testRequestWidgetXml() {

	}

	@Test
	public void testRequestFullPageXml() {

	}

	@Test
	public void testIsModulePresent() {

	}

	@Test
	public void testGetLoadedDrivers() {

	}

	@Test
	public void testGetAvailableDrivers() {

	}

	@Test
	public void testWakeSelf() {

	}

	@Test
	public void testReceiveEvent() {

	}

	@Test
	public void testRegisterEventWatch() {

	}

	@Test
	public void testRemoveEventWatch() {

	}

	@Test
	public void testGetDriverThreadId() {

	}

	@Test
	public void testGetServerAddress() {

	}

	@Test
	public void testGetUptime() {

	}

}
