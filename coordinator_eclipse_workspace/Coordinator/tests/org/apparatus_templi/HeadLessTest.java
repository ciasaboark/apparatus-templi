package org.apparatus_templi;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the SysTray in a headless environment.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class HeadLessTest {

	@BeforeClass
	public static void before() {
		System.setProperty("java.awt.headless", "true");
	}

	// @Test(expected = HeadlessException.class)
	// public void a_testHeadless() {
	// System.out.println("Creating systray while in headless mode");
	// // we shouldn't be able to create a systray in headless mode
	// SysTray sysTray = new SysTray();
	// }

	@Test
	public void todo() {
		// stub method so that junit considers this a valid test
	}

}
