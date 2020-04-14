package com.eltech.CRS.resultStuff;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class CarInfo {
    private Bitmap picture;
    private String color;
    private String mark;
    private String govNum;
    private RectF location;

    public CarInfo(Bitmap picture, String color, String mark, String govNum, RectF location) {
        this.picture = picture;
        this.color = color;
        this.mark = mark;
        this.govNum = govNum;
        this.location = location;
    }

    public Bitmap getPicture() {
        return picture;
    }
    public String getColor() {
        return color;
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
