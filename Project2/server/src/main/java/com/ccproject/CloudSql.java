package com.ccproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ccproject.mode.ImageInfo;

public class CloudSql {
	private static final String TABLE_NAME = "image_info";
	
	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME
			+ " (label, lurl, surl, desp, lat, lng) VALUES (?, ?, ?, ?, ?, ?)"; 
	
//	private static final String QUERY_SQL = "set @lat=%.8f;" + 
//			"set @lng=%.8f;" + 
//			"set @dist=%f;";// + 
//			"SELECT *, 111.045 * DEGREES(ACOS(COS(RADIANS(@lat))" + 
//			" * COS(RADIANS(lat))" + 
//			" * COS(RADIANS(lng) - RADIANS(@lng))" + 
//			" + SIN(RADIANS(@lat))" + 
//			" * SIN(RADIANS(lat))))" + 
//			" AS distance_in_km" + 
//			" FROM image_info" + 
//			" HAVING distance_in_km < @dist" + 
//			" ORDER BY distance_in_km ASC" + 
//			" LIMIT 500;";
	
	private static final String QUERY_SQL = "SELECT *, 111.045 * DEGREES(ACOS(COS(RADIANS(%.8f))" + 
	" * COS(RADIANS(lat))" + 
	" * COS(RADIANS(lng) - RADIANS(%.8f))" + 
	" + SIN(RADIANS(%.8f))" + 
	" * SIN(RADIANS(lat))))" + 
	" AS distance_in_km" + 
	" FROM image_info" + 
	" HAVING distance_in_km < %.6f" + 
	" ORDER BY distance_in_km ASC" + 
	" LIMIT 500;";
	
	/**
	 * Create the tale for the information of images
	 * @param conn
	 * @throws SQLException
	 */
	public static void createTable(Connection conn) throws SQLException {
	    final String createTableSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME 
	    		+ " ( id INT NOT NULL AUTO_INCREMENT,"
	            + " label VARCHAR(100) NOT NULL,"
	            + " lurl VARCHAR(100) NOT NULL,"
	            + " surl VARCHAR(100) NOT NULL,"
	            + " desp TEXT,"
	            + " lat DOUBLE NOT NULL,"
	            + " lng DOUBLE NOT NULL,"
	            + " PRIMARY KEY (id) )";
	    Statement statement = conn.createStatement();
	    statement.executeUpdate(createTableSql);
	    statement.close();
	}
	
	/**
	 * Insert the information of an image into database
	 * @param label
	 * @param largeImgUrl
	 * @param smallImgUrl
	 * @param desp
	 * @param longitude
	 * @param latitude
	 * @throws SQLException 
	 */
	public static void insert(String label, 
			String largeImgUrl, 
			String smallImgUrl, 
			String desp,
			double latitude,
			double longitude, 
			Connection conn) throws SQLException {
		// The PreparedStatement need to be released as soon as possible.
		try(PreparedStatement statement = conn.prepareStatement(INSERT_SQL)){
			statement.setString(1, label);
			statement.setString(2, largeImgUrl);
			statement.setString(3, smallImgUrl);
			statement.setString(4, desp);
			statement.setDouble(5, latitude);
			statement.setDouble(6, longitude);
			statement.executeUpdate();
		}catch(SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Query the neighbors with a distance.
	 * @param queryLat the latitude of the query point
	 * @param queryLng the longitude of the query point
	 * @param distance the distance in kilometer
	 * @param conn
	 * @return the images within the distance (distance) of the point (queryLat, queryLng)
	 * @throws SQLException
	 */
	public static List<ImageInfo> query(double queryLat, double queryLng, double distance, Connection conn)
			throws SQLException {
		String selectSql = String.format(QUERY_SQL, queryLat, queryLng, queryLat, distance);
		List<ImageInfo> list = new ArrayList<>();
		try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
			while (rs.next()) {
				int id = rs.getInt("id");
				String label = rs.getString("label");
				String largeUrl = rs.getString("lurl");
				String smallUrl = rs.getString("surl");
				String description = rs.getString("desp");
				double latitude = rs.getDouble("lat");
				double longitude = rs.getDouble("lng");
				ImageInfo info = new ImageInfo(id, label, largeUrl, smallUrl, description, latitude, longitude);
				list.add(info);
			}
			return list;
		} catch (SQLException e) {
			throw e;
		}
	}

}