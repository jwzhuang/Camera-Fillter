package com.kingnez.watermark.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity {

    private CameraPreview mPreview;
    private View mCameraSquare, mMenuTop, mMenuBtm;
    private Button mCameraShutter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraSquare = findViewById(R.id.camera_square);
        mMenuTop = findViewById(R.id.camera_menu_top);
        mMenuBtm = findViewById(R.id.camera_menu_btm);
        mCameraShutter = (Button) findViewById(R.id.camera_shutter);
        mCameraShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.takePicture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldLeft != 0 || oldTop != 0 || oldRight != 0 || oldBottom != 0) {
                    RelativeLayout.LayoutParams layoutParams =
                            (RelativeLayout.LayoutParams) mCameraSquare.getLayoutParams();
                    layoutParams.height = right - left;
                    mCameraSquare.setLayoutParams(layoutParams);
                    mCameraSquare.invalidate();
                }

            }
        });
    }
}
