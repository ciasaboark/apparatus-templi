package org.apparatus_templi;
import java.awt.HeadlessException;

import org.apparatus_templi.SysTray;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeadLessTest {

	@BeforeClass
	public static void before() {
		System.setProperty("java.awt.headless", "true");
	}

	@Test(expected = HeadlessException.class)
	public void a_testHeadless() {
		System.out.println("Creating systray while in headless mode");
		// we shouldn't be able to create a systray in headless mode
		SysTray sysTray = new SysTray();
	}

}