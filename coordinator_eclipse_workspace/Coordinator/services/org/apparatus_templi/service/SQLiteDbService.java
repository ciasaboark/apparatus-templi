package org.apparatus_templi.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apparatus_templi.Log;

/**
 * A singleton database service using SQLite as the backend.
 * 
 * @author Kimberly Riley <riley_kimberly@columbusstate.edu>
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SQLiteDbService extends DatabaseService implements ServiceInterface {
	private static final String TAG = "DatabaseService";
	private static SQLiteDbService instance = null;

	private SQLiteDbService() {
		if (!tableExists("coordinator", "DRIVERTEXT")) {
			createTableText();
		}

		if (!tableExists("coordinator", "DRIVERBIN")) {
			createTableBin();
		}
	}

	private boolean tableExists(String dbName, String tbName) {
		boolean tableExists = false;
		Connection myConn = null;
		try {
			myConn = SharedConnection.openConnection();
			DatabaseMetaData meta = myConn.getMetaData();
			ResultSet res = meta.getTables(null, null, tbName, new String[] { "TABLE" });
			while (res.next()) {
				if (tbName.equals(res.getString("TABLE_NAME"))) {
					tableExists = true;
					break;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			Log.e(TAG, "connection: " + myConn);
			Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
			e.printStackTrace();
		} finally {
			SharedConnection.closeConnection();
		}

		return tableExists;
	}

	private void createTableText() {
		// Log.d(TAG, "creating table for text data");
		Connection myConn = null;
		Statement stmt = null;
		String sql = null;
		try {
			myConn = SharedConnection.openConnection();
			stmt = myConn.createStatement();
			sql = "CREATE TABLE DRIVERTEXT" + "(NAME TEXT NOT NULL, TAG TEXT NOT NULL, "
					+ "DATA TEXT NOT NULL);";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			Log.e(TAG, "connection: " + myConn);
			Log.e(TAG, "statement: " + sql);
			Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
			e.printStackTrace();
		} finally {
			SharedConnection.closeConnection();
		}
	}

	private void createTableBin() {
		// Log.d(TAG, "creating table for binary data");
		String sql = null;
		Connection myConn = null;
		Statement stmt = null;
		try {
			myConn = SharedConnection.openConnection();
			stmt = myConn.createStatement();
			sql = "CREATE TABLE DRIVERBIN" + "(NAME	TEXT	NOT NULL, " + " TAG		TEXT	NOT NULL, "
					+ " DATA	BYTES[]	NOT NULL);";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			Log.e(TAG, "connection: " + myConn);
			Log.e(TAG, "statement: " + sql);
			Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
			e.printStackTrace();
		} finally {
			SharedConnection.closeConnection();
		}
	}

	/**
	 * Returns the SQLiteDbService instance
	 */
	public static SQLiteDbService getInstance() {
		if (instance == null) {
			instance = new SQLiteDbService();
		}
		return instance;
	}

	@Override
	public int storeTextData(String driverName, String dataTag, String data)
			throws IllegalArgumentException {
		// Log.d(TAG, "storing text data");
		assert tableExists("coordinator", "DRIVERTEXT") : "database should already exist before storing data";
		int returnCode = 0;

		if (data == null || driverName == null || dataTag == null) {
			Log.w(TAG, "database can not store null values");
		} else {

			PreparedStatement stmt = null;
			String sql = null;
			Connection myConn = null;
			try {
				myConn = SharedConnection.openConnection();
				// myConn.setAutoCommit(false);
				if (readTextData(driverName, dataTag) == null) // no previous data
																// stored
				{
					sql = "INSERT INTO DRIVERTEXT VALUES (?, ?, ?);";
					stmt = myConn.prepareStatement(sql);
					stmt.setString(1, driverName);
					stmt.setString(2, dataTag);
					stmt.setString(3, data);
					returnCode = 1;
				} else {
					sql = "UPDATE DRIVERTEXT SET DATA = ? WHERE NAME = ? AND TAG = ?;";
					stmt = myConn.prepareStatement(sql);
					stmt.setString(1, data);
					stmt.setString(2, driverName);
					stmt.setString(3, dataTag);
					returnCode = -1;
				}
				stmt.executeUpdate();
				stmt.close();
				// myConn.commit();

			} catch (Exception e) {
				Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
				Log.e(TAG, "connection: " + myConn);
				Log.e(TAG, "statement: " + sql);
				Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
				e.printStackTrace();
				returnCode = 0;
			} finally {
				SharedConnection.closeConnection();
			}
		}

		return returnCode;
	}

	@Override
	public int storeBinData(String driverName, String dataTag, byte[] data)
			throws IllegalArgumentException {
		// Log.d(TAG, "storing binary data");
		assert tableExists("coordinator", "DRIVERBIN") : "database should already exist before storing data";
		int returnCode = 0;

		if (data == null) {
			Log.w(TAG, "database can not store null values");
		} else {
			PreparedStatement stmt = null;
			String sql = null;
			Connection myConn = null;
			try {
				myConn = SharedConnection.openConnection();
				// myConn.setAutoCommit(false);
				if (readBinData(driverName, dataTag) == null) {
					sql = "INSERT INTO DRIVERBIN" + " VALUES (?,?,?);";
					stmt = myConn.prepareStatement(sql);
					stmt.setString(1, driverName);
					stmt.setString(2, dataTag);
					stmt.setBytes(3, data);
					returnCode = 1;
				} else {
					sql = "UPDATE DRIVERBIN SET DATA = ? WHERE NAME = ? AND TAG = ?;";
					stmt = myConn.prepareStatement(sql);
					stmt.setBytes(1, data);
					stmt.setString(2, driverName);
					stmt.setString(3, dataTag);
					returnCode = -1;
				}
				stmt.executeUpdate();
				stmt.close();
				// myConn.commit();
			} catch (Exception e) {
				Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
				Log.e(TAG, "connection: " + myConn);
				Log.e(TAG, "statement: " + sql);
				Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
				e.printStackTrace();
				returnCode = 0;
			} finally {
				SharedConnection.closeConnection();
			}
		}
		return returnCode;
	}

	@Override
	public String readTextData(String driverName, String dataTag) {
		// Log.d(TAG, "reading text data");
		Connection myConn = null;
		Statement stmt = null;
		String data = null;
		String sql = null;
		try {
			myConn = SharedConnection.openConnection();
			// myConn.setAutoCommit(false);
			stmt = myConn.createStatement();
			sql = "SELECT * FROM DRIVERTEXT;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName) && rs.getString("TAG").equals(dataTag)) {
					data = rs.getString("DATA");
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			Log.e(TAG, "connection: " + myConn);
			Log.e(TAG, "statement: " + sql);
			Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
			e.printStackTrace();
		} finally {
			SharedConnection.closeConnection();
		}

		return data;
	}

	@Override
	public byte[] readBinData(String driverName, String dataTag) {
		// Log.d(TAG, "reading binary data");

		Statement stmt = null;
		byte[] data = null;
		Connection myConn = null;
		String sql = null;
		try {
			myConn = SharedConnection.openConnection();
			// myConn.setAutoCommit(false);
			stmt = myConn.createStatement();
			sql = "SELECT * FROM DRIVERBIN;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName) && rs.getString("TAG").equals(dataTag)) {
					data = rs.getBytes("DATA");
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			Log.e(TAG, "connection: " + myConn);
			Log.e(TAG, "statement: " + sql);
			Log.e(TAG, "open connections: " + SharedConnection.getOpenConnections());
			e.printStackTrace();
		} finally {
			SharedConnection.closeConnection();
		}

		return data;
	}

	@Override
	public synchronized void preferencesChanged() {
		// nothing to do
	}

	@Override
	public synchronized void restartService() {
		// nothing to do
	}

	@Override
	public synchronized void stopService() {
		// nothing to do
	}

	@Override
	public ArrayList<String> getTextTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getBinTags() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class SharedConnection {
		// shared connection
		private static Connection conn = null;
		// semaphore to keep track of the number of open connections
		private static int openConnections = 0;
		private static final String TAG = "SQLiteDbService.SharedConnection";

		/**
		 * Opens a connection to the database if the connection is not already open.
		 * 
		 * @return the database connection.
		 * @throws SQLException
		 * @throws ClassNotFoundException
		 */
		public static synchronized Connection openConnection() throws SQLException,
				ClassNotFoundException {
			// Log.d(TAG, "openConnection(), current open connections: " + openConnections);
			openConnections++;
			Class.forName("org.sqlite.JDBC");
			if (openConnections == 1) {
				// Log.d(TAG, "first accessor opening connection to database");
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			return conn;
		}

		/**
		 * Closes the connection to the database if there are no other open connections.
		 */
		public static synchronized void closeConnection() {
			// Log.d(TAG, "closeConnection(), current open connections: " + openConnections);
			openConnections--;
			if (openConnections == 0) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				conn = null;
			}
		}

		public static int getOpenConnections() {
			return openConnections;
		}
	}
}
