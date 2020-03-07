package com.ccproject.whatsaround.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.ccproject.whatsaround.util.Utils;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lei on 4/25/2018.
 */

public class LocationWorker {
    private Context mContext;
    private static LocationWorker sLocationWorker;
    private Location mLastLocation;
    private boolean mNetworkEnabled = false;
    private boolean mGPSEnabled = false;
    private LocationManager mLocationManager;
    private final String KEY_LATITUDE = "latitude";
    private final String KEY_LONGITUDE = "longitude";

    private LocationWorker(Context context) {
        mContext = context.getApplicationContext();
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationWorker init(Context context) {
        if (sLocationWorker == null) {
            synchronized (LocationWorker.class) {
                if (sLocationWorker == null) {
                    sLocationWorker = new LocationWorker(context);
                }
            }
        }
        return sLocationWorker;
    }

    public static LocationWorker getInstance() {
        if (sLocationWorker == null) {
            throw new IllegalStateException("Must call init(context) to initialize.");
        }
        return sLocationWorker;
    }

    public LatLng getLastLocation() {
        if(mLastLocation != null){
            return new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }else{
            return readLocation();
        }
    }

    /**
     * Must check if the application has the location permission before calling this function
     * @param callback
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        if(mGPSEnabled){
            final LocationListener listener = new MyLocationListener(callback);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }

        if(mNetworkEnabled){
            final LocationListener listener = new MyLocationListener(callback);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        final int TWO_MINUTES = 1000 * 60 * 2;
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public interface LocationCallback{
        public void call(LatLng location);
    }

    public boolean checkPermission(Activity activity){
        boolean hasFineLocation = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(hasFineLocation){
            mGPSEnabled = true;
            return true;
        }else{
            mGPSEnabled = false;
        }

        boolean hasCoarseLocation = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(hasCoarseLocation){
            mNetworkEnabled = true;
            return true;
        }else{
            mNetworkEnabled = false;
        }

        return false;
    }

    public static void requestPermission(Activity activity, int requestCode){
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private void saveLocation(Location location){
        Utils.saveSharedPrefDouble(mContext, KEY_LATITUDE, location.getLatitude());
        Utils.saveSharedPrefDouble(mContext, KEY_LONGITUDE, location.getLongitude());
    }

    private LatLng readLocation(){
        if(Utils.sharedPrefContainsKey(mContext, KEY_LATITUDE)){
            double latitude = Utils.getSharedPrefDouble(mContext, KEY_LATITUDE, 0.0);
            double longitude = Utils.getSharedPrefDouble(mContext, KEY_LONGITUDE, 0.0);
            return new LatLng(latitude, longitude);
        }
        return null;
    }

    class MyLocationListener implements LocationListener{
        private LocationCallback mLocationCallback;

        MyLocationListener(LocationCallback callback){
            mLocationCallback = callback;
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocationManager.removeUpdates(this);
            verifyAndCallback(location, mLocationCallback);
        }

        private void verifyAndCallback(Location location, LocationCallback callback){
            if(mLastLocation == null || isBetterLocation(location, mLastLocation)){
                mLastLocation = location;
                saveLocation(location);
            }
            callback.call(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
