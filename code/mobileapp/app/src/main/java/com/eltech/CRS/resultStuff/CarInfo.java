package com.eltech.CRS.resultStuff;

public class CarInfo {
    private int picResId; //TODO for the first demo it uses pics in resource; change to bitmap/image/whatever on CRM ready
    private String color;
    private String mark;
    private String govNum;

    public CarInfo(int pic, String color, String mark, String govNum) {
        this.picResId = pic;
        this.color = color;
        this.mark = mark;
        this.govNum = govNum;
    }

    public int getPicResId() {
        return picResId;
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
}
