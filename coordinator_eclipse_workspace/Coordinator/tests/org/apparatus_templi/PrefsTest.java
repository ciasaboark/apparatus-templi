package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests public methods in Prefs
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class PrefsTest {
	private Prefs prefs;

	@Before
	public void before() {
		// System.out.println("#################     BEGIN     #################");
		prefs = new Prefs();

	}

	@After
	public void after() {
		// System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test
	public void testEmptyPreferences() {
		System.out.println("Reading preferences value before reading from file");
		assertTrue(prefs.getPreference(null) == null);
		System.out.println("null key has null value");
		assertTrue(prefs.getPreference(Prefs.Keys.portNum) == null);
		System.out.println("string key has null value");
		assertTrue(prefs.getPreferencesMap() != null);
		System.out.println("preferences map is not null");
		HashMap<String, String> prefMap = prefs.getPreferencesMap();
		assertTrue(prefMap.isEmpty());
		System.out.println("preferences map is empty");
	}

	@Test
	public void getPreferenceDescriptions() {
		System.out.println("Get preference description for null");
		assertTrue(prefs.getPreferenceDesc(null) == null);
		System.out.println("Get preference description for config file");
		assertTrue(prefs.getPreferenceDesc(Prefs.Keys.configFile) != null);

	}

	@Test
	public void setPreferences() {
		System.out.println("Storing a single preference and reading back");
		prefs.putPreference("foo", "bar");
		assertTrue(prefs.getPreference("foo") != null);
		assertTrue(prefs.getPreference("foo").equals("bar"));

		System.out.println("Storing null preference with key");
		prefs.putPreference("foo", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void storeNullPrefKey() {
		System.out.println("Storing null preference with null key");
		prefs.putPreference(null, null);
		System.out.println("Storing preference with null key");
		prefs.putPreference(null, "bar");
	}

	@Test(expected = NullPointerException.class)
	public void saveNullPreferences() {
		System.out.println("Saving preferences to null location");
		prefs.savePreferences(null);
	}

}
