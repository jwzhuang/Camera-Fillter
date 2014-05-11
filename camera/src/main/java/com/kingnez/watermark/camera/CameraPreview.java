package com.kingnez.watermark.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

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

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
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
                mCamera.setDisplayOrientation(getCameraDisplayOrientation(mActivity, mCameraInfo));
            } else {
                mCamera.setDisplayOrientation(90);
            }

            Camera.Parameters parameters = mCamera.getParameters();

            Camera.Size previewSize = parameters.getPreviewSize();
            final double aspectRatio = (double) previewSize.width / previewSize.height;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            int surfaceWidth = getWidth();
            int surfaceHeight = (int) (surfaceWidth * aspectRatio);
            layoutParams.height = surfaceHeight;
            setLayoutParams(layoutParams);

            previewSize = getOptimalSize(mCamera.getParameters().getSupportedPreviewSizes(), aspectRatio);
            if (previewSize != null) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            Camera.Size pictureSize = getOptimalSize(mCamera.getParameters().getSupportedPictureSizes(), aspectRatio);
            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
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

    private static int getCameraDisplayOrientation(Activity activity, Camera.CameraInfo cameraInfo) {
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

    private static Camera.Size getOptimalSize(final List<Camera.Size> sizes, final double targetRatio) {
        Camera.Size optimalSize = null;

        if (sizes != null) {
            final double ASPECT_TOLERANCE = 0.1;

            // Try to find an size match aspect ratio and size
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                    if (optimalSize == null || optimalSize.height < size.height) {
                        optimalSize = size;
                    }
                }
            }
        }

        return optimalSize;
    }

}
