package com.eltech.CRS.utils;

import android.graphics.*;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.renderscript.RenderScript;
import com.eltech.CRS.activities.MainActivity;
import com.eltech.ImageProcessor.Recognition;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import io.github.silvaren.easyrs.tools.Nv21Image;

import java.util.List;
import java.util.concurrent.locks.Lock;

public class OwnFrameProcessor implements FrameProcessor {
    private RenderScript rs;
    private MainActivity mainActivity;
    private ImageView overlay;
    private Bitmap overlayBitmap;
    private int currentOverlayBitmapOrientation;
    private List<Recognition> recognitions;
    private Lock mutex;

    public OwnFrameProcessor(MainActivity mainActivity, ImageView overlay, Lock mutex) {
        this.mainActivity = mainActivity;
        this.overlay = overlay;
        this.mutex = mutex;
        rs = RenderScript.create(mainActivity);
    }

    @Override
    public void process(@NonNull Frame frame) {
        mutex.lock();
        try {
//            Log.i(MainActivity.LOG_TAG, "frame format " + frame.getFormat());
            Log.i(MainActivity.LOG_TAG, "rotation to view: " + frame.getRotationToView());
            if (overlayBitmap == null) {
                currentOverlayBitmapOrientation = frame.getRotationToUser();
                overlayBitmap = initOverlayBitmap(frame.getSize().getHeight(), frame.getSize().getWidth());
            }

            recognitions = mainActivity.getRecognizer()
                                       .recognizeImage(rotateBitmap(obtainBitmap(frame), frame.getRotationToUser()));
//            Log.i(MainActivity.LOG_TAG, "recognition size - " + recognitions.size());


            overlayBitmap.eraseColor(Color.TRANSPARENT);
//            rotateOverlayBitmap(frame.getRotationToUser());
//            int rotationToView = frame.getRotationToView();
            mainActivity.getRecognizer()
                        .drawRectsOnImage(overlayBitmap, recognitions);
            mainActivity.runOnUiThread(() -> {
                overlay.setImageBitmap(overlayBitmap);
//                Matrix matrix = new Matrix();
//                matrix.postRotate(rotationToView,
//                                  overlay.getDrawable().getBounds().width()/2,
//                                  overlay.getDrawable().getBounds().height()/2);
//                overlay.setImageMatrix(matrix);
            });
        } finally {
            mutex.unlock();
        }
    }

    private Bitmap obtainBitmap(Frame frame) {
        Bitmap resultBitmap;
        byte[] data = obtainByteArray(frame);

        if (frame.getFormat() == ImageFormat.NV21) {
            resultBitmap = Nv21Image.nv21ToBitmap(rs, data, frame.getSize().getWidth(), frame.getSize().getHeight());
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            resultBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }

        return resultBitmap;
    }

    private byte[] obtainByteArray(Frame frame) {
        byte[] result;
        if (frame.getDataClass() == Image.class) {
            Log.i(MainActivity.LOG_TAG, "it's android.media.image");
            Image image = frame.getData();
            result = image.getPlanes()[0]
                    .getBuffer()
                    .array();
            Log.i(MainActivity.LOG_TAG, "image planes - " + image.getPlanes().length);
        } else if (frame.getDataClass() == byte[].class) {
            Log.i(MainActivity.LOG_TAG, "it's byte[]");
            result = frame.getData();
        } else {
            throw new RuntimeException("Unknown image format");
        }

        return result;
    }

    private Bitmap initOverlayBitmap(int width, int height) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private Bitmap rotateBitmap(Bitmap original, float degrees) {
        if(degrees < 1) return original;
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

    private void rotateOverlayBitmap(int orientation) {
        int delta = orientation-currentOverlayBitmapOrientation;
        delta = delta < 0 ? 360 + delta : delta;
        Log.i(MainActivity.LOG_TAG, "cur: " + currentOverlayBitmapOrientation +
                ", ori: " + orientation +
                ", delta: " + delta);
        if(currentOverlayBitmapOrientation == orientation) return;

        overlayBitmap = rotateBitmap(overlayBitmap, delta);
        currentOverlayBitmapOrientation = orientation;
    }
}
