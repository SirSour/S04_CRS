package com.eltech.CRS.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.eltech.CRS.activities.MainActivity;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static com.eltech.CRS.activities.MainActivity.LOG_TAG;

public class CameraService {
    private final int CAMERA_WIDTH = 1600;
    private final int CAMERA_HEIGHT = 1200;
    private final CameraManager mCameraManager;
    private File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "test1.jpg");;
    private String mCameraID;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private TextureView mImageView;
    private ImageReader mImageReader;
    private Handler mBackgroundHandler;
    private MainActivity mainActivity;
    private int deviceOrientation;
    private int sensorOrientation;

    private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            Log.i(LOG_TAG, "Open camera  with id:"+mCameraDevice.getId());
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
            Log.i(LOG_TAG, "disconnect camera  with id:"+mCameraDevice.getId());
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.i(LOG_TAG, "error! camera id:"+camera.getId()+" error:"+error);
        }
    };
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
//            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Image image = mImageReader.acquireNextImage();

                    ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    mImageReader.close();
                    createCameraPreviewSession();

                    mainActivity.onImageChosen(imageBitmap);
                }
            });
        }
    };

    @SuppressWarnings("ConstantConditions")
    public CameraService(CameraManager cameraManager, String cameraID, MainActivity mainActivity) {
        mCameraManager = cameraManager;
        mCameraID = cameraID;
        this.mainActivity = mainActivity;
        try {
            sensorOrientation = mCameraManager.getCameraCharacteristics(mCameraID)
                                              .get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void makePhoto (){
        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    mCaptureSession.close();
                }
            };

            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean isOpen() {
        return mCameraDevice != null;
    }

    public void openCamera() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mainActivity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager.openCamera(mCameraID, mCameraCallback, mBackgroundHandler);
                }
            }
        } catch (CameraAccessException e) {
            Log.i(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void setBackgroundHandler(Handler backgroundHandler) {
        mBackgroundHandler = backgroundHandler;
    }
    public void setImageView(TextureView imageView) {
        mImageView = imageView;
    }
    public void setDeviceOrientation(int orientation) {
        deviceOrientation = orientation;
    }

    private int getOrientation() {
        return (sensorOrientation + roundUpDeviceOrientation()) % 360;
    }
    private int roundUpDeviceOrientation() {
        return (((deviceOrientation + 45) % 360) / 90) * 90;
    }

    private void createCameraPreviewSession() {
        mImageReader = ImageReader.newInstance(CAMERA_WIDTH, CAMERA_HEIGHT, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

        SurfaceTexture texture = mImageView.getSurfaceTexture();
        texture.setDefaultBufferSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        Surface surface = new Surface(texture);

        try {
            final CaptureRequest.Builder builder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                                               new CameraCaptureSession.StateCallback() {
                                                   @Override
                                                   public void onConfigured(@NonNull CameraCaptureSession session) {
                                                       mCaptureSession = session;
                                                       try {
                                                           mCaptureSession.setRepeatingRequest(builder.build(),
                                                                                               null,
                                                                                               mBackgroundHandler);
                                                       } catch (CameraAccessException e) {
                                                           e.printStackTrace();
                                                       }
                                                   }

                                                   @Override
                                                   public void onConfigureFailed(@NonNull CameraCaptureSession session) { }
                                               },
                                               mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}