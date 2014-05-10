package com.kingnez.watermark.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((FrameLayout) findViewById(R.id.camera_preview))
                .addView(new CameraPreview(this, mCamera));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
