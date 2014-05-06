package org.apparatus_templi;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the SysTray in a headless environment.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class HeadlessTest {

	@BeforeClass
	public static void before() {
		//
	}

	@Test
	public void a_testHeadless() {
		System.out.println("Creating systray while in headless mode");
		// running in a headless environment should only generate warnings
		SysTray sysTray = new SysTray();
		sysTray.setStatus(SysTray.Status.WAITING);
	}

}
