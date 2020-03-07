package com.ccproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import com.google.apphosting.api.ApiProxy;

public class Utils {

	public static Connection connectToDatabase() throws SQLException {
		try {
			ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
			Map<String, Object> attr = env.getAttributes();
			String hostname = (String) attr.get("com.google.appengine.runtime.default_version_hostname");

			String url = hostname.contains("localhost:") ? System.getProperty("cloudsql-local")
					: System.getProperty("cloudsql");
			try {
				Connection conn = DriverManager.getConnection(url);
				return conn;
			} catch (SQLException e) {
				throw e;
			}

		} finally {
			// Nothing really to do here.
		}
	}

	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
