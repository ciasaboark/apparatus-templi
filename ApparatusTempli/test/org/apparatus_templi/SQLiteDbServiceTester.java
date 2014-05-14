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

import java.util.ArrayList;
import java.util.Arrays;

import org.apparatus_templi.service.SQLiteDbService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SQLiteDbServiceTester {
	private final String TAG = "SQLiteDbServiceTester";

	@Before
	public void begin() {
		// System.out.println("#################     BEGIN     #################");
	}

	@After
	public void after() {
		// System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test
	public void testTextTagSet() {
		System.out
				.println("write a number of text entries to the database and then attempt to read the tag set");
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("1");
		tags.add("2");
		tags.add("3");
		tags.add("4");
		for (String tag : tags) {
			Coordinator.storeTextData(TAG, tag, "Testing");
		}

		// read a list of all tags
		SQLiteDbService db = SQLiteDbService.getInstance();
		ArrayList<String> writtenTags = db.getTextTags(TAG);
		assertTrue(writtenTags.containsAll(tags));
		System.out.println("Test tags were contained in the tag set");
	}

	@Test
	public void testBinTagSet() {
		System.out
				.println("write a number of binary entries to the database and then attempt to read the tag set");
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("1");
		tags.add("2");
		tags.add("3");
		tags.add("4");
		for (String tag : tags) {
			Coordinator.storeBinData(TAG, tag, "Testing".getBytes());
		}

		// read a list of all tags
		SQLiteDbService db = SQLiteDbService.getInstance();
		ArrayList<String> writtenTags = db.getBinTags(TAG);
		assertTrue(writtenTags.containsAll(tags));
		System.out.println("Test tags were contained in the tag set");
	}

	@Test
	public void testTextDataStoreRetrieve() {
		System.out.println("Writing text data to the database and reading it back");
		String data = "FOOBAR";
		Coordinator.storeTextData(TAG, "testTextDataStoreRetrieve", data);
		assertTrue(data.equals(Coordinator.readTextData(TAG, "testTextDataStoreRetrieve")));
		System.out.println("Text data stored and retrieved correctly");
	}

	@Test
	public void testBinDataStoreRetrieve() {
		System.out.println("Writing bin data to the database and reading it back");
		byte[] data = "FOOBAR".getBytes();
		Coordinator.storeBinData(TAG, "testBinDataStoreRetrieve", data);
		byte[] newData = Coordinator.readBinData(TAG, "testBinDataStoreRetrieve");
		assertTrue(Arrays.equals(data, newData));
		System.out.println("Binary data stored and retrieved correctly");
	}
}
