package com.kingnez.watermark.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
    }
}
