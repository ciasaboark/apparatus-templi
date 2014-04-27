package org.apparatus_templi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests public methods in the SysTray. Methods that might fail due to running in a headless
 * environment are tested in HeadLessTest.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SysTrayTest {
	private SysTray sysTray;

	@Before
	public void before() {
		System.out.println("#################     BEGIN     #################");
		sysTray = new SysTray();
	}

	@After
	public void after() {
		System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setInvalidStatus() {
		System.out.println("Setting invalid status");
		sysTray.setStatus(-999);
	}

	@Test
	public void setValidStatus() {
		System.out.println("Setting valid status");
		sysTray.setStatus(SysTray.Status.RUNNING);
		sysTray.setStatus(SysTray.Status.TERM);
		sysTray.setStatus(SysTray.Status.WAITING);
	}

}
