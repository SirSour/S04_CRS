package com.eltech.ImageProcessor;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.eltech.ImageProcessor.tflite.Classifier;
import com.eltech.ImageProcessor.tflite.TFLiteObjectDetectionAPIModel;
import com.eltech.ImageProcessor.util.ImageUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

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
                    String color = getMainColor(Bitmap.createBitmap(croppedBitmap, (int) location.left, (int) location.top, (int) location.width(), (int) location.height()));
                    result.setColor(color);
                    cropToFrameTransform.mapRect(location);

                    result.setLocation(location);
                    result.setImagePart(Bitmap.createBitmap(image, (int) location.left, (int) location.top, (int) location.width(), (int) location.height()));
                    mappedRecognitions.add(result);
                }
            }
        }
        return mappedRecognitions;
    }

    private String getMainColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Map<String, Integer> colors = new HashMap<>();

        for (int i = (int) (width * 0.3); i < width * 0.7; i++) {
            for (int j = (int) (height * 0.3); j < (int) (height * 0.7); j++) {
                int pixel = bitmap.getPixel(i, j);
                Color c = Color.valueOf(pixel);
                String col = null;
                /*float hsb[] = {0,0,0};
                Color.RGBToHSV((int)(c.red()*255),(int)(c.green()*255), (int)(c.blue()*255), hsb);
                if      (hsb[1] < 0.1 && hsb[2] > 0.9) col = "White";
                else if (hsb[2] < 0.1) col = "Black";
                else {
                    float deg = hsb[0];
                    if      (deg >=   0 && deg <  30) col = "Red";
                    else if (deg >=  30 && deg <  90) col = "Yellow";
                    else if (deg >=  90 && deg < 150) col = "Green";
                    else if (deg >= 150 && deg < 210) col = "Cyan";
                    else if (deg >= 210 && deg < 270) col = "Blue";
                    else if (deg >= 270 && deg < 330) col = "Magenta";
                    else col = "Red";
                }*/
                int red = (int) (c.red() * 255);
                int green = (int) (c.green() * 255);
                int blue = (int) (c.blue() * 255);
                //0 128 192 255
                if (red <= 96) { //00
                    if (green <= 96) { //00
                        if (blue <= 96) { //00
                            col = "Black";
                        } else if (blue > 96 && blue < 170) { // 80
                            col = "Navy";
                        } else if (blue > 170) { // ff
                            col = "Blue";
                        }
                    } else if (green > 96 && green < 170) { // 80
                        if (blue <= 96) { //00
                            col = "Green";
                        }
                    } else if (green > 210) { // ff
                        if (blue <= 96) { //00
                            col = "Lime";
                        }  else if (blue > 210) { // ff
                            col = "Aqua";
                        }
                    }
                } else if (red > 96 && red < 170) { // 80
                    if (green <= 96) { //00
                        if (blue <= 96) { //00
                            col = "Maroon";
                        } else if (blue > 96 && blue < 170) { // 80
                            col = "Purple";
                        }
                    } else if (green > 96 && green < 170) { // 80
                        if (blue <= 96) { //00
                            col = "Olive";
                        } else if (blue > 96 && blue < 170) { // 80
                            col = "Gray";
                        }
                    }
                } else if (red > 210) { // ff
                    if (green <= 96) { //00
                        if (blue <= 96) { //00
                            col = "Red";
                        }  else if (blue > 210) { // ff
                            col = "Fuchsia";
                        }
                    } else if (green > 210) { // ff
                        if (blue <= 96) { //00
                            col = "Yellow";
                        } else if (blue > 210) { // ff
                            col = "White";
                        }
                    }
                } else {  //c0
                    if (green < 210 && green > 170) {  //c0
                        if (blue > 170 && blue < 210){  //c0
                            col = "Silver";
                        }
                    }
                }

                if (isNull(col)) {
                    if (c.red() > 0.5) {
                        if (c.green() > 0.5) {
                            if (c.blue() > 0.5) col = "White";    //111
                            else col = "Yellow";                  //110
                        } else {
                            if (c.blue() > 0.5) col = "Magenta";  //101
                            else col = "Red";                     //100
                        }
                    } else {
                        if (c.green() > 0.5) {
                            if (c.blue() > 0.5) col = "Cyan";     //011
                            else col = "Green";                   //010
                        } else {
                            if (c.blue() > 0.5) col = "Blue";     //001
                            else col = "Black";                   //000
                        }
                    }
//                    if (c.green() < 0.6 && c.green() > 0.4 && c.blue() > 0.4 && c.blue() < 0.6 && c.red() > 0.4 && c.red() < 0.6)
//                    col = "Grey";
                }
                if (colors.containsKey(col)) {
                    colors.put(col, colors.get(col) + 1);
                } else {
                    colors.put(col, 1);
                }
            }
        }
        Optional<Map.Entry<String, Integer>> reduce = colors.entrySet().stream().reduce((a, b) -> a.getValue() >= b.getValue() ? a : b);
        if (reduce.isPresent()) {
            return reduce.get().getKey();
        } else {
            return "???";
        }
    }

    private enum DetectorMode {
        TF_OD_API;
    }
}
