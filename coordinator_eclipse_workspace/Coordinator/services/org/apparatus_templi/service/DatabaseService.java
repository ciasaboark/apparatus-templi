package org.apparatus_templi.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apparatus_templi.Log;

public class DatabaseService extends org.apparatus_templi.service.Service {
	public DatabaseService() {
		
	}
	
	private static final String TAG = "DatabaseService";

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
	static synchronized int storeTextData(String driverName, String dataTag, String data) 
	{
		Log.d(TAG, "storing text data");
		Connection c = null;
		PreparedStatement stmt = null;
		String sql;
		int returnCode = 0;
		
		if (!checkTable("coordinator", "DRIVERTEXT")) {
			createDriverText();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			if(readTextData(driverName, dataTag).equals(""))
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
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
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
	 * @param data the data to be stored
	 * @return -1 if data overwrote information from a previous dataTag. 1 if data was written successfully.
	 * 	0 if the data could not be written.
	 */
	static synchronized int storeBinData(String driverName, String dataTag, byte[] data) {
		Log.d(TAG, "storing binary data");
		Connection c = null;
		PreparedStatement stmt = null;
		String sql = null;
		int returnCode = 0;
		String s = null;
		
		if (!checkTable("coordinator", "DRIVERBIN")) {
			createDriverBin();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			c.setAutoCommit(false);
			s = new String(readBinData(driverName, dataTag));
			if(s.equals("error"))
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
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
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
	static synchronized String readTextData(String driverName, String dataTag) {
		Log.d(TAG, "reading text data");	
		Connection c = null;
		Statement stmt = null;
		String data = "error";
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
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
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
	static synchronized byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "reading binary data");
		Connection c = null;
		Statement stmt = null;
		byte[] data = "error".getBytes();
		
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
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return data;
	}
	
	private static boolean checkTable(String dbName, String tbName) {
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
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
			return false;
		}
	}

	private static void createDriverText() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			stmt = c.createStatement();
			String sql = "CREATE TABLE DRIVERTEXT"
					+ "(NAME           TEXT    NOT NULL, "
					+ " TAG            TEXT     NOT NULL, "
					+ " DATA	       TEXT)";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	private static void createDriverBin() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:coordinator.db");
			stmt = c.createStatement();
			String sql = "CREATE TABLE DRIVERBIN"
					+ "(NAME           TEXT    NOT NULL, "
					+ " TAG            TEXT     NOT NULL, "
					+ " DATA	        BYTES[])";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
}
