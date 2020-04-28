package com.eltech.ImageProcessor;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.eltech.ImageProcessor.tflite.Classifier;
import com.eltech.ImageProcessor.tflite.TFLiteObjectDetectionAPIModel;
import com.eltech.ImageProcessor.util.ImageUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private static final int[] recognitionColors = new int[]{
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.GRAY,
            Color.WHITE
    };

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    public CarRecognizer(AssetManager assetManager) {
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            assetManager,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public List<Recognition> recognizeImage(Bitmap image) {
        int cropSize = TF_OD_API_INPUT_SIZE;

        previewWidth = image.getWidth();
        previewHeight = image.getHeight();

        Bitmap croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        0, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(image, frameToCropTransform, null);

        final List<Recognition> results = detector.recognizeImage(croppedBitmap);

        List<Recognition> mappedRecognitions = new LinkedList<>();

        for (Recognition result : results) {
            if ("car".equals(result.getTitle())) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    Bitmap carPart = Bitmap.createBitmap(croppedBitmap, (int) location.left, (int) location.top, (int) location.width(), (int) location.height());
                    processColor(carPart, result);
                    cropToFrameTransform.mapRect(location);

                    result.setLocation(location);
                    result.setImagePart(Bitmap.createBitmap(image, (int) location.left, (int) location.top, (int) location.width(), (int) location.height()));
                    mappedRecognitions.add(result);
                }
            }
        }

        drawRectanglesOnSourceImage(image, mappedRecognitions);
        return mappedRecognitions;
    }

    private void drawRectanglesOnSourceImage(Bitmap image, List<Recognition> recognitions) {
        if (recognitions.isEmpty()) return;

        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int paintStrokeScale = imageHeight < imageWidth ? imageHeight / 100 : imageWidth / 100;
        Random random = new Random();
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(paintStrokeScale);
        for (Recognition recognition : recognitions) {
            paint.setColor(recognitionColors[random.nextInt(recognitionColors.length)]);
            canvas.drawRect(recognition.getLocation(), paint);
        }
    }

    private void processColor(Bitmap bitmap, Recognition result) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Map<String, Integer> colors = new HashMap<>();
        long redSum = 0;
        long greenSum = 0;
        long blueSum = 0;
        for (int i = (int) (width * 0.3); i < width * 0.7; i++) {
            for (int j = (int) (height * 0.3); j < (int) (height * 0.7); j++) {
                int pixel = bitmap.getPixel(i, j);
                int r = ((pixel >> 16) & 0xff);
                int g = ((pixel >>  8) & 0xff);
                int b = ((pixel      ) & 0xff);
                redSum += r;
                greenSum += g;
                blueSum += b;

                String col = null;

                //0 128 192 255
                if (r <= 96) { //00
                    if (g <= 96) { //00
                        if (b <= 96) { //00
                            col = "Black";
                        } else if (b > 96 && b < 170) { // 80
                            col = "Navy";
                        } else if (b > 170) { // ff
                            col = "Blue";
                        }
                    } else if (g > 96 && g < 170) { // 80
                        if (b <= 96) { //00
                            col = "Green";
                        }
                    } else if (g > 210) { // ff
                        if (b <= 96) { //00
                            col = "Lime";
                        } else if (b > 210) { // ff
                            col = "Aqua";
                        }
                    }
                } else if (r > 96 && r < 170) { // 80
                    if (g <= 96) { //00
                        if (b <= 96) { //00
                            col = "Maroon";
                        } else if (b > 96 && b < 170) { // 80
                            col = "Purple";
                        }
                    } else if (g > 96 && g < 170) { // 80
                        if (b <= 96) { //00
                            col = "Olive";
                        } else if (b > 96 && b < 170) { // 80
                            col = "Gray";
                        }
                    }
                } else if (r > 210) { // ff
                    if (g <= 96) { //00
                        if (b <= 96) { //00
                            col = "Red";
                        } else if (b > 210) { // ff
                            col = "Fuchsia";
                        }
                    } else if (g > 210) { // ff
                        if (b <= 96) { //00
                            col = "Yellow";
                        } else if (b > 210) { // ff
                            col = "White";
                        }
                    }
                } else {  //c0
                    if (g < 210 && g > 170) {  //c0
                        if (b > 170 && b < 210) {  //c0
                            col = "Silver";
                        }
                    }
                }

                if (col == null) {
                    if (r > 128) {
                        if (g > 128) {
                            if (b > 128) col = "White";    //111
                            else col = "Yellow";                  //110
                        } else {
                            if (b > 128) col = "Magenta";  //101
                            else col = "Red";                     //100
                        }
                    } else {
                        if (g > 128) {
                            if (b > 128) col = "Cyan";     //011
                            else col = "Green";                   //010
                        } else {
                            if (b > 128) col = "Blue";     //001
                            else col = "Black";                   //000
                        }
                    }
                }
                if (colors.containsKey(col)) {
                    colors.put(col, colors.get(col) + 1);
                } else {
                    colors.put(col, 1);
                }
            }
        }

        Map.Entry<String, Integer> res = null;
        for (Map.Entry<String, Integer> colo : colors.entrySet()){
            if (res == null) {
                res = colo;
            } else if (res.getValue() < colo.getValue()){
                res = colo;
            }
        }
        result.setColorName(res.getKey());
        result.setColor(Color.argb(255, (int)(redSum / (width * height * 0.16)), (int)(greenSum / (width * height * 0.16)), (int)(blueSum / (width * height * 0.16))));
    }

    private enum DetectorMode {
        TF_OD_API;
    }
}
