package org.apparatus_templi.xml;

/**
 * An interface for all objects which can automatically generate their own XML representation data.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface ElementInterface {
	/**
	 * Returns the name of this Element.
	 * 
	 */
	public String getName();

	/**
	 * Generate an XML representation of this Element.
	 */
	public String getXml();
}