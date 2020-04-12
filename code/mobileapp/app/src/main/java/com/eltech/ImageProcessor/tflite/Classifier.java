package com.eltech.ImageProcessor.tflite;

import android.graphics.Bitmap;

import com.eltech.ImageProcessor.Recognition;

import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {
    List<Recognition> recognizeImage(Bitmap bitmap);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();

    void setNumThreads(int num_threads);

    void setUseNNAPI(boolean isChecked);
}
