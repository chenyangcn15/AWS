package com.ccproject.mode;

import com.google.gson.JsonObject;

public class ImageInfo {
	private static final String BUCKET_URL_PREFIX = "http://storage.googleapis.com/";
    private int id;
    private String label;
    // bucket name + image name
    private String largeUrl;
    private String smallUrl;
    private String description;
    private double latitude;
    private double longitude;
    
    public ImageInfo(int id,
    		String label,
    		String largeUrl,
    		String smallUrl,
    		String descrption,
    		double latitude,
    		double longitude) {
    	this.setId(id);
    	this.setLabel(label);
    	this.setLargeUrl(largeUrl);
    	this.setSmallUrl(smallUrl);
    	this.setDescription(descrption);
    	this.setLatitude(latitude);
    	this.setLongitude(longitude);
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLargeUrl() {
		return largeUrl;
	}

	public void setLargeUrl(String largeUrl) {
		this.largeUrl = largeUrl;
	}

	public String getSmallUrl() {
		return smallUrl;
	}

	public void setSmallUrl(String smallUrl) {
		this.smallUrl = smallUrl;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
    
	public JsonObject toJSON() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("id", id);
		jObject.addProperty("label", label);
		// convert to generic http url
		jObject.addProperty("lurl", BUCKET_URL_PREFIX + largeUrl);
		jObject.addProperty("surl", BUCKET_URL_PREFIX + smallUrl);
		jObject.addProperty("desp", description);
		jObject.addProperty("lat", latitude);
		jObject.addProperty("lng", longitude);
		return jObject;
	}
    
}
