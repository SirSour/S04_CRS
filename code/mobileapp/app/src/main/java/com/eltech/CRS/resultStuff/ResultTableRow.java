package com.eltech.CRS.resultStuff;

import android.widget.ImageView;
import android.widget.TextView;

public class ResultTableRow {
    private ImageView carPicView;
    private ImageView carColorVisualView;
    private TextView carColorView;
    private TextView carMarkView;
    private TextView carGovNumView;

    public ResultTableRow(ImageView carPicView, ImageView carColorVisualView, TextView carColorView, TextView carMarkView, TextView carGovNumView) {
        this.carPicView = carPicView;
        this.carColorVisualView = carColorVisualView;
        this.carColorView = carColorView;
        this.carMarkView = carMarkView;
        this.carGovNumView = carGovNumView;
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

    public TextView getCarMarkView() {
        return carMarkView;
    }

    public TextView getCarGovNumView() {
        return carGovNumView;
    }
}
