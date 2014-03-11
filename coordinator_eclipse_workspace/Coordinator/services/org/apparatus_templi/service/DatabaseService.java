package org.apparatus_templi.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apparatus_templi.Log;

public class DatabaseService implements ServiceInterface {
	private static final String TAG = "DatabaseService";
	private static DatabaseService instance = null;
	private DatabaseService() {
		if (!tableExists("coordinator", "DRIVERTEXT")) {
			createTableText();
		}
		
		if (!tableExists("coordinator", "DRIVERBIN")) {
			createTableBin();
		}
	}
	
	public static DatabaseService getInstance() {
		if (instance == null) {
			instance = new DatabaseService();
		}
		return instance;
	}

	/**
	 * Stores the given data to persistent storage. Data is tagged with both the driver
	 * 	name as well as a data tag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a tag to assign to this data.  This tag should be specific for each data block
	 * 	that your driver stores.  If there already exits data for the given dataTag the old data
	 * 	will be overwritten.
	 * @param data the text data to be stored.  The data must not be null.
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written or if the given data was null.
	 */
	public synchronized int storeTextData(String driverName, String dataTag, String data) throws IllegalArgumentException
	{
		Log.d(TAG, "storing text data");
		assert tableExists("coordinator", "DRIVERTEXT");
		
		if (data == null) {
			Log.w(TAG, "database can not store null values");
			return 0;
		}
		
		Connection c = null;
		PreparedStatement stmt = null;
		String sql;
		int returnCode = 0;
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			if(readTextData(driverName, dataTag) == null)	//no previous data stored
			{
				sql = "INSERT INTO DRIVERTEXT VALUES (?, ?, ?)";
				stmt = c.prepareStatement(sql);
				stmt.setString(1, driverName);
				stmt.setString(2, dataTag);
				stmt.setString(3, data);
				returnCode = 1;
			}
			else
			{
				sql = "UPDATE DRIVERTEXT SET DATA = ? WHERE NAME = ? AND TAG = ?";
				stmt = c.prepareStatement(sql);
				stmt.setString(1, data);
				stmt.setString(2, driverName);
				stmt.setString(3, dataTag);				
				returnCode = -1;
			}
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
		}
		return returnCode;
	}

	/**
	 * Stores the given data to persistent storage. Data is stored based off the given driverName
	 * 	and dataTag.
	 * @param driverName the name of the driver to store the data under
	 * @param dataTag a unique tag to assign to this data. This tag should be specific for each data
	 * 	block that will be stored. If data has already been stored with the same driverName and
	 * 	dataTag the old data will be overwritten.
	 * @param data the data to be stored.  The data must not be null.
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written or if the given data was null.
	 */
	public synchronized int storeBinData(String driverName, String dataTag, byte[] data) throws IllegalArgumentException {
		Log.d(TAG, "storing binary data");
		assert tableExists("coordinator", "DRIVERBIN");
		if (data == null) {
			Log.w(TAG, "database can not store null values");
			return 0;
		}
		
		Connection c = null;
		PreparedStatement stmt = null;
		String sql = null;
		int returnCode = 0;
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			if(readBinData(driverName, dataTag) == null)
			{
				sql = "INSERT INTO DRIVERBIN" + " VALUES (?,?,?)";
				stmt = c.prepareStatement(sql);
				stmt.setString(1, driverName);
				stmt.setString(2, dataTag);
				stmt.setBytes(3, data);
				returnCode = 1;
			}
			else
			{
				sql = "UPDATE DRIVERBIN SET DATA = ? WHERE NAME = ? AND TAG = ?";
				stmt = c.prepareStatement(sql);
				stmt.setBytes(1, data);
				stmt.setString(2, driverName);
				stmt.setString(3, dataTag);
				return -1;
			}
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "storeBinData()" + e.getClass().getName() + ": " + e.getMessage());
		}
		return returnCode;
	}

	/**
	 * Returns text data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored String data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	public synchronized String readTextData(String driverName, String dataTag) {
		Log.d(TAG, "reading text data");	
		Connection c = null;
		Statement stmt = null;
		String data = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DRIVERTEXT;");
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName)
						&& rs.getString("TAG").equals(dataTag)) {
					data = rs.getString("DATA");
				}
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "readTextData()" + e.getClass().getName() + ": " + e.getMessage());
		}
		return data;
	}

	/**
	 * Returns binary data previously stored under the given module name and tag.
	 * @param driverName the name of the calling driver
	 * @param dataTag the tag to uniquely identify the data
	 * @return the stored binary data, or null if no data has been stored under the given driver name
	 * 	and tag.
	 */
	public synchronized byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "reading binary data");
		Connection c = null;
		Statement stmt = null;
		byte[] data = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DRIVERBIN;");
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName)
						&& rs.getString("TAG").equals(dataTag)) {
					data = rs.getBytes("DATA");
				}
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "readBinData()" + e.getClass().getName() + ": " + e.getMessage());
		}
		return data;
	}
	
	private boolean tableExists(String dbName, String tbName) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager
					.getConnection("jdbc:sqlite:coordinator.db");
			DatabaseMetaData meta = c.getMetaData();
			ResultSet res = meta.getTables(null, null, null,
					new String[] { "TABLE" });
			while (res.next()) {
				if (res.getString("TABLE_NAME").equals(tbName)) {
					c.close();
					return true;
				}
			}
			c.close();
			return false;
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "checkTable()" +  e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	private void createTableText() {
		Log.d(TAG, "creating table for text data");
		Connection c = null;
		Statement stmt = null;
		try {
//			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			stmt = c.createStatement();
			String sql = "CREATE TABLE DRIVERTEXT"
					+ "(NAME	TEXT	NOT NULL, "
					+ " TAG		TEXT	NOT NULL, "
					+ " DATA	TEXT	NOT NULL)";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "createDriverText()" + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	private void createTableBin() {
		Log.d(TAG, "creating table for binary data");
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			stmt = c.createStatement();
			String sql = "CREATE TABLE DRIVERBIN"
					+ "(NAME	TEXT	NOT NULL, "
					+ " TAG		TEXT	NOT NULL, "
					+ " DATA	BYTES[]	NOT NULL)";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "createDriverBin()" +  e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void preferencesChanged() {
		//nothing to do
	}

	@Override
	public void restartService() {
		//nothing to do
	}

	@Override
	public void stopService() {
		//nothing to do
	}
}
