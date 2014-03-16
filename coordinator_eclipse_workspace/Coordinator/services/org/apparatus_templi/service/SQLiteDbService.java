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
	// shared connection
	private static Connection conn = null;
	// semaphore to keep track of the number of open connections
	private static int openConnections = 0;

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
		openConnections++;
		boolean tableExists = false;
		try {
			Class.forName("org.sqlite.JDBC");
			if (openConnections == 1) {
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, tbName, new String[] { "TABLE" });
			while (res.next()) {
				if (tbName.equals(res.getString("TABLE_NAME"))) {
					tableExists = true;
					break;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "checkTable()" + e.getClass().getName() + ": " + e.getMessage());
		} finally {
			openConnections--;
		}

		// if this was the last access then we need to close the SQL connection
		if (openConnections == 0) {
			try {
				Log.d(TAG, "last accessor closing SQL connection");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				Log.e(TAG, "could not close sql connection");
				e.printStackTrace();
			}
		}

		return tableExists;
	}

	private void createTableText() {
		Log.d(TAG, "creating table for text data");
		openConnections++;

		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			// if this was the first access then we need to open a connection to the DB
			if (openConnections == 1) {
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			stmt = conn.createStatement();
			String sql = "CREATE TABLE DRIVERTEXT" + "(NAME	TEXT	NOT NULL, "
					+ " TAG		TEXT	NOT NULL, " + " DATA	TEXT	NOT NULL)";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "createDriverText()" + e.getClass().getName() + ": " + e.getMessage());
		} finally {
			openConnections--;
		}

		// if this was the last access then we need to close the SQL connection
		if (openConnections == 0) {
			try {
				Log.d(TAG, "last accessor closing SQL connection");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				Log.e(TAG, "could not close sql connection");
				e.printStackTrace();
			}
		}

	}

	private void createTableBin() {
		Log.d(TAG, "creating table for binary data");
		openConnections++;

		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			// if this was the first access then we need to open a connection to the DB
			if (openConnections == 1) {
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			stmt = conn.createStatement();
			String sql = "CREATE TABLE DRIVERBIN" + "(NAME	TEXT	NOT NULL, "
					+ " TAG		TEXT	NOT NULL, " + " DATA	BYTES[]	NOT NULL)";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "createDriverBin()" + e.getClass().getName() + ": " + e.getMessage());
		} finally {
			openConnections--;
		}

		// if this was the last access then we need to close the SQL connection
		if (openConnections == 0) {
			try {
				Log.d(TAG, "last accessor closing SQL connection");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				Log.e(TAG, "could not close sql connection");
				e.printStackTrace();
			}
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
	public synchronized int storeTextData(String driverName, String dataTag, String data)
			throws IllegalArgumentException {
		Log.d(TAG, "storing text data");
		assert tableExists("coordinator", "DRIVERTEXT") : "database should already exist before storing data";
		int returnCode = 0;

		if (data == null) {
			Log.w(TAG, "database can not store null values");
		} else {
			openConnections++;

			PreparedStatement stmt = null;
			String sql;

			try {
				Class.forName("org.sqlite.JDBC");
				// if this was the first access then we need to open a connection to the DB
				if (openConnections == 1) {
					conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
				}
				conn.setAutoCommit(false);
				if (readTextData(driverName, dataTag) == null) // no previous data
																// stored
				{
					sql = "INSERT INTO DRIVERTEXT VALUES (?, ?, ?)";
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, driverName);
					stmt.setString(2, dataTag);
					stmt.setString(3, data);
					returnCode = 1;
				} else {
					sql = "UPDATE DRIVERTEXT SET DATA = ? WHERE NAME = ? AND TAG = ?";
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, data);
					stmt.setString(2, driverName);
					stmt.setString(3, dataTag);
					returnCode = -1;
				}
				stmt.executeUpdate();
				stmt.close();
				conn.commit();

			} catch (Exception e) {
				Log.e(TAG, "storeTextData()" + e.getClass().getName() + ": " + e.getMessage());
			} finally {
				openConnections--;
			}

			// if this was the last access then we need to close the SQL connection
			if (openConnections == 0) {
				try {
					Log.d(TAG, "last accessor closing SQL connection");
					conn.close();
					conn = null;
				} catch (SQLException e) {
					Log.e(TAG, "could not close sql connection");
					e.printStackTrace();
				}
			}
		}

		return returnCode;
	}

	@Override
	public synchronized int storeBinData(String driverName, String dataTag, byte[] data)
			throws IllegalArgumentException {
		Log.d(TAG, "storing binary data");
		assert tableExists("coordinator", "DRIVERBIN") : "database should already exist before storing data";
		int returnCode = 0;

		if (data == null) {
			Log.w(TAG, "database can not store null values");
		} else {
			openConnections++;

			PreparedStatement stmt = null;
			String sql = null;

			try {
				Class.forName("org.sqlite.JDBC");
				// if this was the first access then we need to open a connection to the DB
				if (openConnections == 1) {
					conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
				}
				conn.setAutoCommit(false);
				if (readBinData(driverName, dataTag) == null) {
					sql = "INSERT INTO DRIVERBIN" + " VALUES (?,?,?)";
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, driverName);
					stmt.setString(2, dataTag);
					stmt.setBytes(3, data);
					returnCode = 1;
				} else {
					sql = "UPDATE DRIVERBIN SET DATA = ? WHERE NAME = ? AND TAG = ?";
					stmt = conn.prepareStatement(sql);
					stmt.setBytes(1, data);
					stmt.setString(2, driverName);
					stmt.setString(3, dataTag);
					returnCode = -1;
				}
				stmt.executeUpdate();
				stmt.close();
				conn.commit();
			} catch (Exception e) {
				Log.e(TAG, "storeBinData()" + e.getClass().getName() + ": " + e.getMessage());
			} finally {
				openConnections--;
			}

			// if this was the last access then we need to close the SQL connection
			if (openConnections == 0) {
				try {
					Log.d(TAG, "last accessor closing SQL connection");
					conn.close();
					conn = null;
				} catch (SQLException e) {
					Log.e(TAG, "could not close sql connection");
					e.printStackTrace();
				}
			}
		}
		return returnCode;
	}

	@Override
	public synchronized String readTextData(String driverName, String dataTag) {
		Log.d(TAG, "reading text data");
		openConnections++;

		Statement stmt = null;
		String data = null;
		try {
			Class.forName("org.sqlite.JDBC");
			// if this was the first access then we need to open a connection to the DB
			if (openConnections == 1) {
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DRIVERTEXT;");
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName) && rs.getString("TAG").equals(dataTag)) {
					data = rs.getString("DATA");
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "readTextData()" + e.getClass().getName() + ": " + e.getMessage());
		} finally {
			openConnections--;
		}

		// if this was the last access then we need to close the SQL connection
		if (openConnections == 0) {
			try {
				Log.d(TAG, "last accessor closing SQL connection");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				Log.e(TAG, "could not close sql connection");
				e.printStackTrace();
			}
		}

		return data;
	}

	@Override
	public synchronized byte[] readBinData(String driverName, String dataTag) {
		Log.d(TAG, "reading binary data");

		openConnections++;
		Statement stmt = null;
		byte[] data = null;

		try {
			Class.forName("org.sqlite.JDBC");
			// if this was the first access then we need to open a connection to the DB
			if (openConnections == 1) {
				conn = DriverManager.getConnection("jdbc:sqlite:coordinator.sqlite");
			}
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DRIVERBIN;");
			while (rs.next()) {
				if (rs.getString("NAME").equals(driverName) && rs.getString("TAG").equals(dataTag)) {
					data = rs.getBytes("DATA");
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			Log.e(TAG, "readBinData()" + e.getClass().getName() + ": " + e.getMessage());
		} finally {
			openConnections--;
		}

		// if this was the last access then we need to close the SQL connection
		if (openConnections == 0) {
			try {
				Log.d(TAG, "last accessor closing SQL connection");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				Log.e(TAG, "could not close sql connection");
				e.printStackTrace();
			}
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
}
