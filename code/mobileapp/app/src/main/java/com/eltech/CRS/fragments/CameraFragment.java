package com.eltech.CRS.fragments;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eltech.CRS.R;
import com.eltech.CRS.utils.CameraService;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {
    private OnFragmentInteractionListener listener;
    private CameraService backCamera;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backCamera = listener.getBackCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        TextureView textureView = root.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        backCamera.setImageView(textureView);

        ImageButton shotButton = root.findViewById(R.id.shotButton);
        shotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backCamera.makePhoto();
            }
        });

        listener.requestStartBackgroundThread();
        return root;
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        backCamera.openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public interface OnFragmentInteractionListener {
        CameraService getBackCamera();
        void requestStartBackgroundThread();
        void requestStopBackgroundThread();
    }
}
