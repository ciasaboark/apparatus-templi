/*
 * Copyright (c) 2014, Jonathan Nelson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
