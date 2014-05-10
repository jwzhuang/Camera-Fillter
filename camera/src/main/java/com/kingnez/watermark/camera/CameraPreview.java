package com.kingnez.watermark.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by kingnez on 5/10/14.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Activity mActivity;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private SurfaceHolder mHolder;
    private Camera.Size mPreviewSize;

    public CameraPreview(Activity activity, Camera camera) {
        super(activity);
        mActivity = activity;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mCamera != null) {
            List<Camera.Size> supportedPreviewSize = mCamera.getParameters().getSupportedPreviewSizes();
            mPreviewSize = getOptimalPreviewSize(supportedPreviewSize, width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initialCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }

        mCamera.stopPreview();

        Camera.Parameters parameters = mCamera.getParameters();
        if (mPreviewSize != null) {
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
        }
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        mCamera.setDisplayOrientation(getCameraDisplayOrientation(mActivity, mCameraInfo));

        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private static int getCameraDisplayOrientation(Activity activity, Camera.CameraInfo cameraInfo) {
        int degrees = 0;
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:    degrees = 0;    break;
            case Surface.ROTATION_90:   degrees = 90;   break;
            case Surface.ROTATION_180:  degrees = 180;  break;
            case Surface.ROTATION_270:  degrees = 270;  break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null) {
            return null;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        Camera.Size optimalSize = null;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private void initialCamera() {
        releaseCamera();
        int cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo != null && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(cameraId);
                    mCameraInfo = cameraInfo;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
