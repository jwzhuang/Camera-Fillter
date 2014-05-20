package com.kingnez.watermark.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;


public class PreviewActivity extends Activity {

    private ImageView mImageView;
    private WebView mWatermarkView;
    private Button mLeftRotation, mRightRotation;
    private ImageButton mWatermarkSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra(CameraActivity.IMAGE_PATH);

        RelativeLayout imagePreview = (RelativeLayout) findViewById(R.id.image_preview);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imagePreview.getLayoutParams();
        layoutParams.height = layoutParams.width;
        imagePreview.setLayoutParams(layoutParams);

        mImageView = (ImageView) findViewById(R.id.image_view);
        ImageLoader.getInstance().displayImage(imagePath, mImageView);

        mWatermarkView = (WebView) findViewById(R.id.watermark);
        mWatermarkView.setBackgroundColor(0x00000000);

        mLeftRotation = (Button) findViewById(R.id.rotation_left);
        mLeftRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(-90);
            }
        });

        mRightRotation = (Button) findViewById(R.id.rotation_right);
        mRightRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(90);
            }
        });

        mWatermarkSelect = (ImageButton) findViewById(R.id.watermark_select);
        mWatermarkSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWatermarkView.loadUrl("http://mipush.kingnez.im/watermark.html");
            }
        });
    }

    private void rotateImage(int degrees) {
        Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
