package com.eltech.CRS.fragments;

import android.graphics.ImageFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eltech.CRS.R;
import com.eltech.CRS.activities.MainActivity;
import com.eltech.CRS.utils.CameraService;
import com.eltech.CRS.utils.OwnFrameProcessor;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Mode;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class CameraFragment extends Fragment {
    private CameraView camera;
    private MainActivity mainActivity;
    private OwnFrameProcessor frameProcessor;
    private Lock mutex;
    private float currentZoom;

    private CameraFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mutex = new ReentrantLock(true);
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
                mutex.unlock();
                camera.addFrameProcessor(frameProcessor);
            }
        });
        camera.setFrameProcessingFormat(ImageFormat.YUV_420_888);
        ImageView overlay = view.findViewById(R.id.overlay);
//        overlay.setScaleType(ImageView.ScaleType.MATRIX);
        frameProcessor = new OwnFrameProcessor(mainActivity, overlay, mutex);
        camera.addFrameProcessor(frameProcessor);
        currentZoom = camera.getZoom();

        ImageButton shotButton = view.findViewById(R.id.shotButton);
        shotButton.setOnClickListener(v -> {
            camera.clearFrameProcessors();
            mutex.lock();
            camera.takePicture();
        });

        ImageButton incZoomButton = view.findViewById(R.id.incZoom);
        incZoomButton.setOnClickListener(v -> incrementZoom());
        ImageButton decZoomButton = view.findViewById(R.id.decZoom);
        decZoomButton.setOnClickListener(v -> decrementZoom());
    }

    private void incrementZoom() {
        if(currentZoom + 0.1 > 1) return;
        currentZoom += 0.1;
        camera.setZoom(currentZoom);
    }

    private void decrementZoom() {
        if(currentZoom - 0.1 < 0) return;
        currentZoom -= 0.1;
        camera.setZoom(currentZoom);
    }

    public interface OnFragmentInteractionListener {
        CameraService getBackCamera();
    }
}
