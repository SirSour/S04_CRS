package com.eltech.CRS.resultStuff;

import android.widget.ImageView;
import android.widget.TextView;

public class ResultTableRow {
    private ImageView carPicView;
    private ImageView carColorVisualView;
    private TextView carColorView;

    public ResultTableRow(ImageView carPicView, ImageView carColorVisualView, TextView carColorView) {
        this.carPicView = carPicView;
        this.carColorVisualView = carColorVisualView;
        this.carColorView = carColorView;
    }

    public ImageView getCarPicView() {
        return carPicView;
    }
    public ImageView getCarColorVisualView() {
        return carColorVisualView;
    }
    public TextView getCarColorView() {
        return carColorView;
    }
}
