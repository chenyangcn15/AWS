package com.ccproject.whatsaround.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by lei on 4/25/2018.
 */

public class ImageLoader {
    public static void loadImage(String url, ImageView iv){
        Picasso.get().load(url).into(iv);
    }

    public static void downloadBitmap(String url, final Callback callback){
        Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callback.call(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                callback.call(null);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public interface Callback{
        public void call(Bitmap bmp);
    }

}
