package com.ccproject.whatsaround;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccproject.whatsaround.data.NearbyDataParser;
import com.ccproject.whatsaround.data.mode.ImageInfo;
import com.ccproject.whatsaround.http.HttpLoader;
import com.ccproject.whatsaround.http.ICallback;
import com.ccproject.whatsaround.imageloader.ImageLoader;
import com.ccproject.whatsaround.location.LocationWorker;
import com.ccproject.whatsaround.util.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String BASE_URL = "https://cloudsql-dot-ccproj2gcloud.appspot.com";
    private static final String GET_NEARBY = "/nearby";
    private static final String UPLOAD = "/upload";

    private final String TAG = "MapsActivity";
    private static final int LOCATION_PERMISSION_REQ_CODE = 1;
    private static final int TAKE_PICTURE_PERMISSION_REQ_CODE = 2;
    private static final int REQ_CODE_TAKE_PICTURE = 1001;
    private GoogleMap mMap;
    private String mCurrentPhotoPath;
    private String[] mTakePicturePermissions = {Manifest.permission.CAMERA};
    private AlertDialog mMessageDialog;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUp();
        Log.d(TAG, "OnCreate() called");
    }

    private void setUp(){
        findViewById(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddClicked();
            }
        });
        locate();
    }

    private void locate(){
        if(Utils.needRequestPermission()){
            boolean hasPermission = LocationWorker.getInstance().checkPermission(this);
            if(hasPermission){
                getCurrentLocation();
            }else{
                LocationWorker.requestPermission(this, LOCATION_PERMISSION_REQ_CODE);
            }
        }else{
            getCurrentLocation();
        }
    }

    private void onAddClicked(){
        if(Utils.needRequestPermission()){
            String[] permissions = mTakePicturePermissions;
            if(!Utils.checkPermissions(this, permissions)){
                Utils.requestPermissions(this, permissions, TAKE_PICTURE_PERMISSION_REQ_CODE);
            }else{
                dispatchTakeImageIntent();
            }
        }else{
            dispatchTakeImageIntent();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_TAKE_PICTURE){
            if(resultCode == Activity.RESULT_OK){
                // upload the image
//                showToast("Picture taken.");
                Log.d(TAG, "The current photo path: " + mCurrentPhotoPath);
                new ResizeImageTask().execute(mCurrentPhotoPath, "dummy param");
            }else{
                showToast("Cancelled.");
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyymmdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "The image path: " + mCurrentPhotoPath);
        return image;
    }

    private void dispatchTakeImageIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e){
                e.printStackTrace();
            }

            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.ccproject.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                Log.d(TAG, "The file uri: " + photoUri.toString());
                startActivityForResult(intent, REQ_CODE_TAKE_PICTURE);
            }
        }
    }

    /**
     * Must check or request location permission first;
     */
    private void getCurrentLocation(){
        LocationWorker.getInstance().getCurrentLocation(new LocationWorker.LocationCallback() {
            @Override
            public void call(LatLng location) {
                if(location != null){
                    Log.d(TAG, String.format("minZoom: %f, maxZoom: %f", mMap.getMinZoomLevel(), mMap.getMaxZoomLevel()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));
                    loadNearbyData(location.latitude, location.longitude);
                    Log.d(TAG, String.format("The lat: %.8f, The lng: %.8f", location.latitude, location.longitude));
                }else{
                    Log.d(TAG, "Can't locate");
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        // Add a marker in Sydney and move the camera
        LatLng asu = new LatLng(33.4242865,-111.9295875);
//        mMap.addMarker(new MarkerOptions().position(asu).title("Marker in ASU"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(asu, 12.0f));
    }

    private void loadNearbyData(double latitude, double longitude){
        String url = BASE_URL + GET_NEARBY;
        HttpLoader.getInstance().getNearby(url, latitude, longitude,
                new NearbyDataParser(), new ICallback<List<ImageInfo>>() {
            @Override
            public void callback(List<ImageInfo> data) {
                if(data != null){
                    Log.d(TAG, "Load data: " + data.size());
                    for(final ImageInfo info : data){
                        LatLng postion = new LatLng(info.getLatitude(), info.getLongitude());
                        MarkerOptions mo = new MarkerOptions()
                                .position(postion)
                                .title(info.getLabel());
                        Marker marker = mMap.addMarker(mo);
                        marker.setTag(info);
                        marker.showInfoWindow();
                    }
                }else{
                    Log.d(TAG, "Load data: null");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_PERMISSION_REQ_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }else{
                    showToast("You must grant the location permission.");
                }
                break;
            case TAKE_PICTURE_PERMISSION_REQ_CODE:
                if(grantResults.length == mTakePicturePermissions.length && allGranted(grantResults)){
                    dispatchTakeImageIntent();
                }else{
                    showToast("You must grant the camera and storage permissions");
                }
                break;
        }
    }

    private boolean allGranted(int[] grantResult){
        for(int result : grantResult){
            if(result != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideAlertDialog();
    }

    private void showToast(String msg){
        hideToast();
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void hideToast(){
        if(mToast != null){
            mToast.cancel();
            mToast = null;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        ImageInfo info = (ImageInfo)marker.getTag();
        showDetails(info);
    }

    private void showDetails(ImageInfo info){
        // TODO: show iamge details page
    }

    private void showAlertDialog(String msg){
        hideAlertDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        mMessageDialog = builder.create();
        mMessageDialog.show();
    }

    private void hideAlertDialog(){
        if(mMessageDialog != null){
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        @Override
        public View getInfoWindow(Marker marker) {
            View v = getLayoutInflater().inflate(R.layout.info_window, null);
            ImageInfo info = (ImageInfo)marker.getTag();
            TextView tvTitle = (TextView)v.findViewById(R.id.tv_tile);
            ImageView ivPicture = (ImageView)v.findViewById(R.id.iv_picture);
            tvTitle.setText(info.getLabel());
            ImageLoader.loadImage(info.getSmallUrl(), ivPicture);
//            marker.hideInfoWindow();
            return v;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    class ResizeImageTask extends AsyncTask<String, Integer, String>{

        private ResizeImageTask(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showAlertDialog("Processing the picture.");
        }

        @Override
        protected String doInBackground(String... strings) {
            String imagePath = strings[0];
            if(imagePath != null){
                return resizeImage(imagePath);
            }
            Log.d(TAG, "The image path is null.");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            hideAlertDialog();
            if(s != null){
                uploadImageToServer(s);
            }else {
                showToast("Somthing wrong during resize the image.");
            }
        }

        private String resizeImage(String imagePath){
            File file = new File(imagePath);
            if(!file.exists() || !file.isFile()){
                return null;
            }
            final int TARGET_WIDTH = 1080;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            int width = options.outWidth;
            if(width > TARGET_WIDTH){
                int scaleFactor = width / TARGET_WIDTH;
                Log.d(TAG, "scal factor: " + scaleFactor);
                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;
                Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
                File newFile = new File(file.getParent(), "Resized_"+file.getName());
                if(saveBitmap(newFile, bmp)){
                    Log.d(TAG, "bmp saved to: " + newFile.getAbsolutePath());
                    return newFile.getAbsolutePath();
                }
            }
            return imagePath;
        }

        private boolean saveBitmap(File file, Bitmap bmp){
            FileOutputStream fos = null;
            try{
                fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return false;
        }

        private void uploadImageToServer(String imagePath){
            LatLng location = LocationWorker.getInstance().getLastLocation();
            if(location != null){
                showAlertDialog("Recognizing the picture.");
                String url = BASE_URL + UPLOAD;
                HttpLoader.getInstance().upload(url, imagePath,
                        location.latitude, location.longitude,
                        "No Description", new ICallback<String>() {
                    @Override
                    public void callback(String data) {
                        Log.d(TAG, "The response for upload: " + data);
                        hideAlertDialog();
                    }
                });
            }else{
                showToast("Can't get your location.");
                Log.d(TAG, "upload: No location.");
            }
        }

    }
}
