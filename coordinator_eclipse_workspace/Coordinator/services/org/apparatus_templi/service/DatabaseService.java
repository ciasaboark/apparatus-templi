package org.apparatus_templi.service;

import java.util.ArrayList;

/**
 * An abstract service to provide access to persistent storage. The storage mechanism used is
 * undefined until implemented in extending classes.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public abstract class DatabaseService implements ServiceInterface {
	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver name as well
	 * as a data tag.
	 * 
	 * @param driverName
	 *            the name of the driver to store the data under
	 * @param dataTag
	 *            a tag to assign to this data. This tag should be specific for each data block that
	 *            your driver stores. If there already exits data for the given dataTag the old data
	 *            will be overwritten.
	 * @param data
	 *            the text data to be stored. The data must not be null.
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written
	 *         successfully. 0 if the data could not be written or if the given data was null.
	 */
	abstract public int storeTextData(String driverName, String dataTag, String data);

	/**
	 * Stores the given data to persistent storage. Data is stored based off the given driverName
	 * and dataTag.
	 * 
	 * @param driverName
	 *            the name of the driver to store the data under
	 * @param dataTag
	 *            a unique tag to assign to this data. This tag should be specific for each data
	 *            block that will be stored. If data has already been stored with the same
	 *            driverName and dataTag the old data will be overwritten.
	 * @param data
	 *            the data to be stored. The data must not be null.
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written
	 *         successfully. 0 if the data could not be written or if the given data was null.
	 */
	abstract public int storeBinData(String driverName, String dataTag, byte[] data);

	/**
	 * Returns text data previously stored under the given module name and tag.
	 * 
	 * @param driverName
	 *            the name of the calling driver
	 * @param dataTag
	 *            the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver
	 *         name and tag.
	 */
	abstract public String readTextData(String driverName, String dataTag);

	/**
	 * Returns binary data previously stored under the given module name and tag.
	 * 
	 * @param driverName
	 *            the name of the calling driver
	 * @param dataTag
	 *            the tag to uniquely identify the data
	 * @return the stored binary data, or null if no data has been stored under the given driver
	 *         name and tag.
	 */
	abstract public byte[] readBinData(String driverName, String dataTag);

	/**
	 * Returns an ArrayList of Strings representing the dataTags that have been used to store text
	 * data.
	 * 
	 * @return an ArrayList of String tags. If no text data has been stored then returns an empty
	 *         list.
	 */
	abstract public ArrayList<String> getTextTags();

	/**
	 * Returns an ArrayList of Strings representing the dataTags that have been used to store binary
	 * data.
	 * 
	 * @return an ArrayList of String tags. If no binary data has been stored then returns an empty
	 *         list.
	 */
	abstract public ArrayList<String> getBinTags();
}
