package com.kingnez.watermark.camera;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by kingnez on 5/19/14.
 */
public class WatermarkCameraApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(getApplicationContext())
                        .build();
        ImageLoader.getInstance().init(config);
    }
}
