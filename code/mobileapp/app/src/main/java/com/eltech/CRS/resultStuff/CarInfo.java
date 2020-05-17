package com.eltech.CRS.resultStuff;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class CarInfo {
    private Bitmap picture;
    private int color;
    private String colorText;
    private RectF location;

    public CarInfo(Bitmap picture, int color, String colorText, RectF location) {
        this.picture = picture;
        this.color = color;
        this.colorText = colorText;
        this.location = location;
    }

    public Bitmap getPicture() {
        return picture;
    }
    public int getColor() {
        return color;
    }
    public String getColorText() {
        return colorText;
    }
    public RectF getLocation() {
        return location;
    }
}
