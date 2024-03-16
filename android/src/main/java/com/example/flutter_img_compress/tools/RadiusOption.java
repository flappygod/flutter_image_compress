package com.example.flutter_img_compress.tools;

/**
 * Created by flappygo on 2017/5/27.
 */

public class RadiusOption {

    //corner type
    private  ScaleType  scaleType;
    private  float      radian;


    public RadiusOption(float radian){
        this.radian=radian;
        this.scaleType = ScaleType.RADIUS_CENTER_CROP;
    }

    public RadiusOption(float radian, ScaleType type){
        this.radian=radian;
        this.scaleType=type;
    }

    public enum ScaleType {
        RADIUS_CENTER_CROP(1),
        RADIUS_WIDTH(2),
        RADIUS_HEIGHT(3),
        RADIUS_ELLIPSE(4);
        ScaleType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }


    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public float getRadian() {
        return radian;
    }

    public void setRadian(float radian) {
        this.radian = radian;
    }
}
