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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apparatus_templi.driver.BlankControllerDriver;
import org.apparatus_templi.xml.Button;
import org.apparatus_templi.xml.Controller;
import org.apparatus_templi.xml.InputType;
import org.apparatus_templi.xml.Pre;
import org.apparatus_templi.xml.Sensor;
import org.apparatus_templi.xml.TextArea;
import org.apparatus_templi.xml.XmlFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Tests public methods in XmlFormatter
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class XmlFormatterTest {

	private XmlFormatter xml;

	@Before
	public void before() {
		// System.out.println("#################     BEGIN     #################");
		xml = new XmlFormatter(new BlankControllerDriver(), "long name");
	}

	@After
	public void after() {
		// System.out.println("-----------------      END      -----------------\n\n");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addNullElement() {
		xml.addElement(null);
	}

	// @Test(expected = IllegalArgumentException.class)
	// public void addBadElement() {
	// xml.addElement(new Object());
	// }

	@Test
	public void xmlWithNoElements() {
		assertTrue(xml.generateXml() != null);
	}

	@Test
	public void xmlWithElement() {
		xml.addElement(new Button("test button"));
		assertTrue(xml.generateXml() != null);
	}

	// TODO temporarily disabled until the schema can be packaged so that it can be referenced
	// during testing
	@Test
	public void validateXmlWithNoElements() {
		System.out.println("Validating xml with no elements");
		Source schemaFile = new StreamSource(new File("./test-files/module-schema.xsd"));
		Source xmlFile = new StreamSource(new StringReader(xml.generateXml()));
		String xmlString = xml.generateXml();
		// Source xmlFile = new StreamSource(new File("test_xml.xml"));

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(schemaFile);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail();
		}
		Validator validator = schema.newValidator();
		try {
			validator.validate(xmlFile);
			System.out.println("generated xml is valid");
		} catch (SAXException | IOException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
			fail();
		}
	}

	// TODO temporarily disabled until the schema can be packaged so that it can be referenced
	// during testing
	@Test
	public void validateXmlWithManyElements() {
		System.out.println("Validating xml with many elements");
		xml.addElement(new Button("button1").setAction("foo").setDescription("a button")
				.setIcon("not a real icon").setInputType(InputType.NONE));
		xml.addElement(new Button("button1").setAction("foo").setDescription("a button")
				.setIcon("not a real icon").setInputType(InputType.TEXT));
		xml.addElement(new Button("button1").setAction("foo").setDescription("a button")
				.setIcon("not a real icon").setInputType(InputType.NUM));

		xml.addElement(new Pre("pre1", "<html></html>"));
		xml.addElement(new Controller("controller1"));
		xml.addElement(new Sensor("sensor1"));
		xml.addElement(new TextArea("area1", "Some text"));

		Source schemaFile = new StreamSource(new File("test-files/module-schema.xsd"));
		Source xmlFile = new StreamSource(new StringReader(xml.generateXml()));
		// Source xmlFile = new StreamSource(new File("test_xml.xml"));

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(schemaFile);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail("Xml schema file could not be opened");
		}
		Validator validator = schema.newValidator();

		try {
			validator.validate(xmlFile);
			System.out.println("generated xml is valid is valid");
		} catch (SAXException | IOException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
			fail("Xml not validated");
		}
	}

}
