package org.apparatus_templi;

public class DatabaseInterface {
	private static final String TAG = "DatabaseInterface";
	
	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver
	 * 	name as well as a data tag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a tag to assign to this data.  This tag should be specific for each data block
	 * 	that your driver stores.  If there already exits data for the given dataTag the old data
	 * 	will be overwritten.
	 * @param data the string of data to store
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	static synchronized int storeTextData(String driverName, String dataTag, String data) {
		Log.d(TAG, "storing text data");
		return 0;
	}
	
	/**
	 * Stores the given data to persistent storage. Data is stored based off the given driverName
	 * 	and dataTag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a unique tag to assign to this data. This tag should be specific for each data
	 * 	block that will be stored. If data has already been stored with the same driverName and
	 * 	dataTag the old data will be overwritten.
	 * @param data the data to be stored
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	static synchronized int storeBinData(String driverName, String dataTag, byte[] data) {
		Log.d(TAG, "storing binary data");
		return 0;
	}
	
	/**
	 * Returns text data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	static synchronized String readTextData(String driverName, String dataTag) {
		Log.d(TAG, "reading text data");
		return null;
	}
	
	/**
	 * Returns binary data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored binary data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	static synchronized Byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "reading binary data");
		return null;
	}
	

}
