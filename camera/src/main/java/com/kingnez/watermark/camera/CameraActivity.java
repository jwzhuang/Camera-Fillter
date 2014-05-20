package com.kingnez.watermark.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity {

    public static final String IMAGE_PATH = "IMAGE_PATH";

    private static final int SELECT_PICTURE = 1;

    private CameraPreview mPreview;
    private View mCameraSquare, mMenuTop, mMenuBtm;
    private Button mCameraShutter;
    private ImageButton mSelectPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraSquare = findViewById(R.id.camera_square);
        mMenuTop = findViewById(R.id.camera_menu_top);
        mMenuBtm = findViewById(R.id.camera_menu_btm);
        mSelectPhoto = (ImageButton) findViewById(R.id.btn_select);
        mSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
//                intent.putExtra(IMAGE_PATH, "assets://img.jpg");
//                startActivity(intent);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });
        mCameraShutter = (Button) findViewById(R.id.camera_shutter);
        mCameraShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.takePicture();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                String imagePath = getImagePath(data.getData());
                if (imagePath != null) {
                    Intent intent = new Intent(this, PreviewActivity.class);
                    intent.putExtra(IMAGE_PATH, imagePath);
                    startActivity(intent);
                }
            }
        }
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

    private String getImagePath(Uri uri) {
        String imagePath = null;
        if (uri != null) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                imagePath = cursor.getString(column_index);
            }
        }
        return imagePath;
    }
}
