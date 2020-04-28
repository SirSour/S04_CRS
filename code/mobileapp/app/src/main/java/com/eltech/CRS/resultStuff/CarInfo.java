package com.eltech.CRS.resultStuff;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class CarInfo {
    private Bitmap picture;
    private int color;
    private String colorText;
    private String mark;
    private String govNum;
    private RectF location;

    public CarInfo(Bitmap picture, int color, String colorText, String mark, String govNum, RectF location) {
        this.picture = picture;
        this.color = color;
        this.colorText = colorText;
        this.mark = mark;
        this.govNum = govNum;
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
    public String getMark() {
        return mark;
    }
    public String getGovNum() {
        return govNum;
    }
    public RectF getLocation() {
        return location;
    }
}
