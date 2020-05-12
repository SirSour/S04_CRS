package com.eltech.CRS.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eltech.CRS.R;
import com.eltech.CRS.activities.MainActivity;
import com.eltech.CRS.utils.CameraService;
import com.eltech.ImageProcessor.Recognition;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Engine;
import com.otaliastudios.cameraview.controls.Mode;

import java.io.ByteArrayInputStream;
import java.util.List;

public class CameraFragment extends Fragment {
    private CameraView camera;
    private MainActivity mainActivity;

    private CameraFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static CameraFragment newInstance(MainActivity mainActivity) {
        return new CameraFragment(mainActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        camera = view.findViewById(R.id.cameraView);
        Log.i(MainActivity.LOG_TAG, "current camera engine is " + camera.getEngine());
        camera.setMode(Mode.PICTURE);
        camera.setLifecycleOwner(getViewLifecycleOwner());
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                result.toBitmap(bitmap -> mainActivity.onImageChosen(bitmap));
            }
        });
        camera.setFrameProcessingFormat(ImageFormat.JPEG);
        /*camera.addFrameProcessor(frame -> {
            byte[] data;
            Log.i(MainActivity.LOG_TAG, "current camera engine is " + camera.getEngine());
            Log.i(MainActivity.LOG_TAG, "frame format " + frame.getFormat());
            if (frame.getDataClass() == Image.class) {
                Log.i(MainActivity.LOG_TAG, "it's android.media.image");
                Image image = frame.getData();
                data = image.getPlanes()[0]
                        .getBuffer()
                        .array();
                Log.i(MainActivity.LOG_TAG, "image planes - " + image.getPlanes().length);
            } else if (frame.getDataClass() == byte[].class) {
                Log.i(MainActivity.LOG_TAG, "it's byte[]");
                data = frame.getData();
            } else {
                return;
            }
            Log.i(MainActivity.LOG_TAG, "data.length - " + data.length);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap resultBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Log.i(MainActivity.LOG_TAG, "result bitmap - " + resultBitmap);
            List<Recognition> recognitions = mainActivity.getRecognizer()
                                                         .recognizeImage(resultBitmap);
            Log.i(MainActivity.LOG_TAG, "recognition size - " + recognitions.size());
        });*/

        ImageButton shotButton = view.findViewById(R.id.shotButton);
        shotButton.setOnClickListener(v -> camera.takePicture());
    }

    public interface OnFragmentInteractionListener {
        CameraService getBackCamera();
    }
}
