package com.kingnez.watermark.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kingnez on 5/10/14.
 */
public class CameraPreview extends SurfaceView
        implements SurfaceHolder.Callback, View.OnTouchListener, Camera.AutoFocusCallback {

    private static final String DateTimeFormat = "yyyyMMdd-HHmmss";

    private Activity mActivity;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private SurfaceHolder mHolder;
    private Camera.Size mPictureSize;
    private int mRotateDegrees;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        setKeepScreenOn(true);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
//        setOnTouchListener(this);
    }

    public void takePicture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            mCamera.stopPreview();
                            Matrix matrix = new Matrix();
                            matrix.postRotate((float)mRotateDegrees);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                            Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 640, 640 * rotatedBitmap.getHeight() / rotatedBitmap.getWidth(), true);
                            Bitmap squareBitmap = Bitmap.createBitmap(sizeBitmap, 0, 0, 640, 640);
                            File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/ShiSe");
                            if (!imageDir.exists()) {
                                imageDir.mkdir();
                            }
                            if (imageDir.isDirectory()) {
                                File pictureFile = new File(imageDir.getPath(),
                                        new SimpleDateFormat(DateTimeFormat).format(new Date()) + ".jpg");
                                if (pictureFile != null) {
                                    boolean saved = false;
                                    try {
                                        FileOutputStream fos = new FileOutputStream(pictureFile);
                                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                                        squareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                                        bos.flush();
                                        bos.close();
                                        saved = true;
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (saved) {
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        Uri uri = Uri.fromFile(pictureFile);
                                        intent.setData(uri);
                                        mActivity.sendBroadcast(intent);
                                    }
                                }
                            }
                            mCamera.startPreview();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void openCamera() {
        releaseCamera();
        int cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo != null && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(cameraId);
                    mCameraInfo = cameraInfo;
                    mCamera.setPreviewDisplay(mHolder);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    releaseCamera();
                }
                break;
            }
        }
    }

    private void initCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();

            if (mActivity != null) {
                mRotateDegrees = getCameraDisplayOrientation(mActivity, mCameraInfo);
                mCamera.setDisplayOrientation(mRotateDegrees);
            }

            Camera.Parameters parameters = mCamera.getParameters();

            // reset preview layout height
            Camera.Size previewSize = parameters.getPreviewSize();
            final double aspectRatio = (double) previewSize.width / previewSize.height;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            int surfaceWidth = getWidth();
            int surfaceHeight = (int) (surfaceWidth * aspectRatio);
            layoutParams.height = surfaceHeight;
            setLayoutParams(layoutParams);

            // get best preview size
            previewSize = getOptimalSize(
                    mCamera.getParameters().getSupportedPreviewSizes(), aspectRatio);
            if (previewSize != null) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            // get best picture size
            mPictureSize = getOptimalSize(
                    mCamera.getParameters().getSupportedPictureSizes(), aspectRatio);
            if (mPictureSize != null) {
                parameters.setPictureSize(mPictureSize.width, mPictureSize.height);
            }

            // set focus mode
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            try {
                mCamera.setParameters(parameters);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private static int getCameraDisplayOrientation(Activity activity,
                                                   Camera.CameraInfo cameraInfo) {
        int result;

        int degrees = 0;
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:    degrees = 0;    break;
            case Surface.ROTATION_90:   degrees = 90;   break;
            case Surface.ROTATION_180:  degrees = 180;  break;
            case Surface.ROTATION_270:  degrees = 270;  break;
        }

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private static Camera.Size getOptimalSize(final List<Camera.Size> sizes,
                                              final double targetRatio) {
        Camera.Size optimalSize = null;
        int side = 640;

        if (sizes != null) {
            final double ASPECT_TOLERANCE = 0.1;

            // Try to find an size match aspect ratio and size
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                    if (optimalSize == null || (optimalSize.height < side && optimalSize.height < size.height)
                            || (size.height > side && size.height < optimalSize.height)) {
                        optimalSize = size;
                    }
                }
            }
        }

        return optimalSize;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        focusOnTouch(event);
        return true;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    private void focusOnTouch(MotionEvent event) {
        Camera.Parameters parameters = mCamera.getParameters();

        Rect focusRect = calculateTapArea(parameters.getPreviewSize(),
                event.getRawX(), event.getRawY(), 1f);
        Rect meteringRect = calculateTapArea(parameters.getPreviewSize(),
                event.getRawX(), event.getRawY(), 1.5f);

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));

            parameters.setFocusAreas(focusAreas);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        mCamera.setParameters(parameters);
        mCamera.autoFocus(this);
    }

    private static Rect calculateTapArea(Camera.Size size, float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int focusAreaRange = 1000;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x / size.width - focusAreaRange);
        int centerY = (int) (y / size.height - focusAreaRange);

        int left = clamp(centerX - areaSize / 2, -focusAreaRange, focusAreaRange);
        int top = clamp(centerY - areaSize / 2, -focusAreaRange, focusAreaRange);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top),
                Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
