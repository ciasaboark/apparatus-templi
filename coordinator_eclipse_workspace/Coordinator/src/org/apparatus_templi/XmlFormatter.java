package org.apparatus_templi;

import java.util.ArrayList;

import org.apparatus_templi.driver.Driver;

public class XmlFormatter {
	private static final double VERSION = 1.0;
	private ArrayList<Element> elements;
	private String name;
	private String shortName;
	
	public XmlFormatter(Driver d, String longName) {
		elements = new ArrayList<Element>();
		this.name = longName;
		this.shortName = d.getName();
	}
	
	public void clearElements() {
		elements = new ArrayList<Element>();
	}
	
	public void addElement(Element e) {
		elements.add(e);
	}
	
	public void removeElement(Element e) {
		elements.remove(e);
	}
	
	public String generateWidgetXml() {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		xml.append("<widget version=\"" + XmlFormatter.VERSION + "\" name=\"" +
				this.name + "\" driver=\"" + shortName + "\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:noNamespaceSchemaLocation=\"resource?file=xml/widget-schema.xsd\" " + " >");
		for (Element e: elements) {
			xml.append(e.getXml());
		}
		xml.append("</widget>");
		return xml.toString();
	}
	
	public String generateFullXml() {
		//TODO
		return null;
	}
	
	
	private interface Element {
		public String getName();
		public String getXml();
	}
	
	public static class Controller implements Element {
		private String name;
		private String icon = null;
		private String status = null;
		
		public Controller(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		public String getIcon() {
			return icon;
		}
		
		public void setIcon(String icon) {
			this.icon = icon;
		}
		
		public void setStatus(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return status;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<controller name=\"" + this.name + "\" ");
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(">");
			
			sb.append("<status>" + status + "</status>");
			sb.append("</controller>");
			
			return sb.toString();
		}
	}
	
	public static class Sensor implements Element {
		private String name;
		private String icon = null;
		private String value = null;
		
		public Sensor(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		public String getIcon() {
			return icon;
		}
		
		public void setIcon(String icon) {
			this.icon = icon;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<sensor name=\"" + this.name + "\" ");
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(">");
			
			sb.append("<value>" + this.value + "</value>");
			sb.append("</sensor>");
			
			return sb.toString();
		}
	}
	
	public static class Button implements Element {
		private String name;
		private String action = null;
		private String inputType = null;
		private String inputVal = null;
		private String icon = null;
		
		public Button(String name) {
			this.name = name;
		}
		
		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		@Override
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getInputType() {
			return inputType;
		}

		public void setInputType(String inputType) {
			this.inputType = inputType;
		}

		public String getInputVal() {
			return inputVal;
		}

		public void setInputVal(String inputVal) {
			this.inputVal = inputVal;
		}

		@Override
		public String getXml() {
			StringBuilder sb = new StringBuilder();
			sb.append("<button title=\"" + this.name + "\" ");
			sb.append("action=\"" + this.action + "\" ");
			if (this.inputType == null) {	
				sb.append("input=\"" + XmlFormatter.InputType.NONE + "\" ");
			} else {
				sb.append("input=\"" + this.inputType + "\" ");
			}
			if (this.inputVal != null) {
				sb.append("inputVal=\"" + this.inputVal + "\" ");
			}
			if (this.icon != null) {
				sb.append("icon=\"" + this.icon + "\" ");
			}
			sb.append(" />");
			
			return sb.toString();
		}
	}
	
	public class InputType {
		public static final String TEXT = "text";
		public static final String NUM  = "numeric";
		public static final String NONE = "none";
	}
}
