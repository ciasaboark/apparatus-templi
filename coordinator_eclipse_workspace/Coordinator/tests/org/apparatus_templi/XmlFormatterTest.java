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
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XmlFormatterTest {

	private XmlFormatter xml;

	@Before
	public void before() {
		xml = new XmlFormatter(new BlankControllerDriver(), "long name");
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

	@Test
	public void validateXmlWithNoElements() {
		Source schemaFile = new StreamSource(new File("website/xml/module-schema.xsd"));
		Source xmlFile = new StreamSource(new StringReader(xml.generateXml()));
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
			System.out.println(xmlFile.getSystemId() + " is valid");
		} catch (SAXException | IOException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
			fail();
		}
	}

	@Test
	public void validateXmlWithManyElements() {
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

		Source schemaFile = new StreamSource(new File("website/xml/module-schema.xsd"));
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
			System.out.println(xmlFile.getSystemId() + " is valid");
		} catch (SAXException | IOException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
			fail("Xml not validated");
		}
	}

}
