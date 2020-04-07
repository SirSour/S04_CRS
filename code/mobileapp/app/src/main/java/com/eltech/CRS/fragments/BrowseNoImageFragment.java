package com.eltech.CRS.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eltech.CRS.R;
import com.eltech.CRS.utils.OpenImageExternalActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class BrowseNoImageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public static BrowseNoImageFragment newInstance() {
        return new BrowseNoImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Fragment callingFragment = this;

        View root = inflater.inflate(R.layout.fragment_browse_no_image, container, false);

        ImageButton openImageButton = root.findViewById(R.id.openImageButton);
        openImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenImageExternalActivity.start(callingFragment);
            }
        });

        return root;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(OpenImageExternalActivity.isSucceed(requestCode, resultCode, data)) {
            Uri selectedFile = data.getData();
            mListener.onImageChosen(selectedFile);
        }
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    public interface OnFragmentInteractionListener {
        void onImageChosen(Uri imageUri);
    }
}