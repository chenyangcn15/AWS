package com.ccproject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ccproject.mode.ImageInfo;
import com.google.gson.JsonArray;

@WebServlet(
		name = "GetNearby",
		urlPatterns = {"/nearby"}
		)
@SuppressWarnings("serial")
public class GetNearby extends HttpServlet {
	
	private Connection mConnection;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
		String lat = req.getParameter("lat");
		String lon = req.getParameter("lon");
		
		if (lat == null || lon == null) {
			resp.getWriter().print("You should provide lat and lon"+ "\r\n");
			return;
		}
		double latitude = Double.valueOf(lat);
		double longitude = Double.valueOf(lon);
		try {
			final double distance = 30; // km
			List<ImageInfo> result = CloudSql.query(latitude, longitude, distance, mConnection);
			if (result == null) {
				resp.getWriter().print("no result nearby"+ "\r\n");
			} else {
//				resp.getWriter().print("result");
				String json = toJsonString(result);
				resp.getWriter().print(json);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			resp.getWriter().print(e.getMessage());
		}
	}
	
	private String toJsonString(List<ImageInfo> data) {
		JsonArray jArray = new JsonArray();
		for(ImageInfo info : data) {
			jArray.add(info.toJSON());
		}
		return jArray.toString();
	}
	
	@Override
	public void init() throws ServletException {
		try {
			mConnection = Utils.connectToDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void destroy() {
		Utils.closeConnection(mConnection);
	}
	
}
