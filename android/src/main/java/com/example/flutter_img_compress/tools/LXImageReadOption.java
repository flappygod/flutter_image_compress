package com.example.flutter_img_compress.tools;

import android.graphics.Bitmap;


//@author flappygo
public class LXImageReadOption {

    //max width
    private int maxWidth;
    //max height
    private int maxHeight;
    //max height
    private int maxKbSize;
    //scale fill
    private boolean scaleFill = false;
    //radius
    private RadiusOption radiusOption;
    //read bitmap
    private Bitmap.Config inPreferredConfig;


    public LXImageReadOption(int maxWidth, int maxHeight, boolean scaleFill) {
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scaleFill = scaleFill;
    }


    public LXImageReadOption(int maxWidth, int maxHeight, int maxKbSize) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxKbSize = maxKbSize;
    }

    public LXImageReadOption(int maxWidth, int maxHeight, boolean scaleFill, Bitmap.Config config) {
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scaleFill = scaleFill;
        this.inPreferredConfig = config;
    }


    public LXImageReadOption(int maxWidth, int maxHeight, boolean scaleFill, Bitmap.Config config, RadiusOption radiusOption) {
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scaleFill = scaleFill;
        this.inPreferredConfig = config;
        this.radiusOption = radiusOption;
    }

    public LXImageReadOption(int maxWidth, int maxHeight, int maxKbSize, boolean scaleFill, RadiusOption radiusOption, Bitmap.Config inPreferredConfig) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxKbSize = maxKbSize;
        this.scaleFill = scaleFill;
        this.radiusOption = radiusOption;
        this.inPreferredConfig = inPreferredConfig;
    }

    public int getMaxKbSize() {
        return maxKbSize;
    }

    public void setMaxKbSize(int maxKbSize) {
        this.maxKbSize = maxKbSize;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public boolean isScaleFill() {
        return scaleFill;
    }

    public void setScaleFill(boolean scaleFill) {
        this.scaleFill = scaleFill;
    }

    public Bitmap.Config getInPreferredConfig() {
        return inPreferredConfig;
    }

    public void setInPreferredConfig(Bitmap.Config inPreferredConfig) {
        this.inPreferredConfig = inPreferredConfig;
    }


    public RadiusOption getRadiusOption() {
        return radiusOption;
    }

    public void setRadiusOption(RadiusOption radiusOption) {
        this.radiusOption = radiusOption;
    }


    public String getOptionAdditional() {
        String str = "";
        if (maxHeight > 0 || maxWidth > 0) {
            str = str + getMaxWidth() + "*" + getMaxHeight();
        }
        if (inPreferredConfig != null) {
            str = str + "$" + inPreferredConfig;
        }
        if (radiusOption != null) {
            str = str + "#" + radiusOption.getRadian() + "|" + radiusOption.getScaleType();
        }
        return str;
    }

}
