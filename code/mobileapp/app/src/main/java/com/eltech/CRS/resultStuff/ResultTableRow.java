package com.eltech.CRS.resultStuff;

import android.widget.ImageView;
import android.widget.TextView;

public class ResultTableRow {
    private ImageView carPicView;
    private TextView carColorView;
    private TextView carMarkView;
    private TextView carGovNumView;

    public ResultTableRow(ImageView carPicView, TextView carColorView, TextView carMarkView, TextView carGovNumView) {
        this.carPicView = carPicView;
        this.carColorView = carColorView;
        this.carMarkView = carMarkView;
        this.carGovNumView = carGovNumView;
    }

    public ImageView getCarPicView() {
        return carPicView;
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
