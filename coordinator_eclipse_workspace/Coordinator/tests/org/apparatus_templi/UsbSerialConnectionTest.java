package org.apparatus_templi;

import org.junit.After;
import org.junit.Before;

/**
 * Tests public methods in UsbSerialConnection
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class UsbSerialConnectionTest {

	// how do we test hardware here?

	@Before
	public void createCoordinator() {
		System.out.println("#################     BEGIN     #################");
	}

	@After
	public void after() {
		System.out.println("-----------------      END      -----------------\n\n");
	}
}
