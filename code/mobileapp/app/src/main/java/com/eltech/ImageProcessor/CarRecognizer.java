package com.eltech.ImageProcessor;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.eltech.ImageProcessor.util.ImageUtils;
import com.eltech.ImageProcessor.tflite.Classifier;
import com.eltech.ImageProcessor.tflite.TFLiteObjectDetectionAPIModel;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CarRecognizer extends Activity {
    protected int previewWidth = 0;
    protected int previewHeight = 0;

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.625f;
    private static final boolean MAINTAIN_ASPECT = false;
    private Integer sensorOrientation;

    private Classifier detector;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;


    public List<Recognition> recognizeImage(Bitmap image, AssetManager assetManager) {
        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            assetManager,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
        }

        previewWidth = image.getWidth();
        previewHeight = image.getHeight();

        sensorOrientation = 0;

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(image, frameToCropTransform, null);

        final List<Recognition> results = detector.recognizeImage(croppedBitmap);

        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        switch (MODE) {
            case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
        }

        List<Recognition> mappedRecognitions =
                new LinkedList<Recognition>();

        for (Recognition result : results) {
            if ("car".equals(result.getTitle())) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= minimumConfidence) {

                    cropToFrameTransform.mapRect(location);

                    result.setLocation(location);
                    result.setImagePart(Bitmap.createBitmap(image, (int)location.left, (int)location.top, (int)location.width(), (int)location.height()));
                    mappedRecognitions.add(result);
                }
            }
        }
        return mappedRecognitions;
    }

    private enum DetectorMode {
        TF_OD_API;
    }
}
