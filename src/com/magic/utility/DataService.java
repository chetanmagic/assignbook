package com.magic.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DataService {

	private static Connection con;
	private static final String Driver = "com.mysql.jdbc.Driver";

	/**
	 * create Database object
	 */
	public DataService() {
	}

	/**
	 * to load the database base driver
	 * 
	 * @param properties
	 * 
	 * @return a database connection
	 * @throws SQLException
	 *             throws an exception if an error occurs
	 */
	public static Connection loadDriver(Properties properties) throws SQLException {
		try {
			Class.forName(Driver);
		} catch (ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		con = DriverManager.getConnection(properties.getProperty("connectionstring"), properties.getProperty("user"),
				properties.getProperty("pwd"));
		return con;
	}

	/**
	 * to get a result set of a query
	 * 
	 * @param query
	 *            custom query
	 * @return a result set of custom query
	 * @throws SQLException
	 *             throws an exception if an error occurs
	 */
	public static ResultSet getResultSet(String query, Connection con) throws SQLException {
		ResultSet rs;
		PreparedStatement st = con.prepareStatement(query);
		rs = st.executeQuery();
		return rs;
	}

}
