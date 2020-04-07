package com.eltech.CRS.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.eltech.CRS.R;
import com.eltech.CRS.adapters.ResultTableBodyAdapter;
import com.eltech.CRS.resultStuff.CarInfo;

import java.util.List;

public class BrowseImageResultFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ResultTableBodyAdapter resultTableBodyAdapter;
    private Bitmap image;

    public static BrowseImageResultFragment newInstance(Context context, Bitmap image, List<CarInfo> carInfoList) {
        BrowseImageResultFragment browseImageResultFragment = new BrowseImageResultFragment();
        browseImageResultFragment.image = image;
        browseImageResultFragment.resultTableBodyAdapter = new ResultTableBodyAdapter(context, carInfoList);
        return browseImageResultFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_browse_image_result, container, false);

        ImageView imageView = root.findViewById(R.id.processedImageView);
        imageView.setImageBitmap(image);

        ListView tableBody = root.findViewById(R.id.tableBody);
        tableBody.setAdapter(resultTableBodyAdapter);

        return root;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setCarInfoList(List<CarInfo> carInfoList) {
        resultTableBodyAdapter.setCarInfoList(carInfoList);
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
