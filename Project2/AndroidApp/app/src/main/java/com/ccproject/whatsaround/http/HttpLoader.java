package com.ccproject.whatsaround.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by lei on 4/25/2018.
 */

public class HttpLoader {
    private static HttpLoader sLoader;
    private final OkHttpClient mClient;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private HttpLoader(){
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static HttpLoader getInstance(){
        if(sLoader == null){
            synchronized (HttpLoader.class){
                if (sLoader == null) {
                    sLoader = new HttpLoader();
                }
            }
        }
        return sLoader;
    }

    public void loadData(String url, final IParser parser, final ICallback callback){
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.callback(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                try{
                    responseBody = response.body();
                    String respStr = responseBody.string();
                    Log.d("HttpLoader", "The respStr: " + respStr);
                    final Object obj = parser.parse(respStr);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(obj);
                        }
                    });
                }catch (Exception e){
                    throw e;
                }finally {
                    if(responseBody != null){
                        responseBody.close();
                    }
                }
            }
        });
    }


    public void getNearby(String url, double latitude, double longitude, final IParser parser, final ICallback callback){
        url = url + String.format("?lat=%s&lon=%s", latitude, longitude);
        loadData(url, parser, callback);
    }

    public void upload(String url,
                       String filePath,
                       double latitude,
                       double longitude,
                       String description,
                       final ICallback callback){
        File file = new File(filePath);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                .addFormDataPart("lat", String.valueOf(latitude))
                .addFormDataPart("lon", String.valueOf(longitude))
                .addFormDataPart("desp", description)
                .build();
        Request request = new Request.Builder().url(url).post(body).build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.callback(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                try{
                    responseBody = response.body();
                    callback.callback(responseBody.string());
                }catch(Exception e){
                    e.printStackTrace();
                } finally {
                    if(responseBody != null){
                        responseBody.close();
                    }
                }
            }
        });
    }

}
