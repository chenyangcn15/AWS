package com.ccproject.whatsaround.data.mode;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lei on 4/25/2018.
 */

public class ImageInfo {
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

    public static ImageInfo fromJSON(JSONObject jsonObject) {
        try {
            int id = jsonObject.getInt("id");
            String label = jsonObject.getString("label");
            String lurl = jsonObject.getString("lurl");
            String surl = jsonObject.getString("surl");
            String desp = jsonObject.getString("desp");
            double latitude = jsonObject.getDouble("lat");
            double longitude = jsonObject.getDouble("lng");
            return new ImageInfo(id, label, lurl, surl, desp, latitude, longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch(NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }

}
