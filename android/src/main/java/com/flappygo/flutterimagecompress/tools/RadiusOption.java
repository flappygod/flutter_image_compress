package com.flappygo.flutterimagecompress.tools;

/**
 * Created by Administrator on 2017/5/27.
 */

public class RadiusOption {

    //corner的类型
    private  ScaleType  scaleType;
    //截取的弧度
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
        //图片先进行切割，保证宽高一致
        RADIUS_CENTER_CROP(1),
        //以宽度作为圆角基础
        RADIUS_WIDTH(2),
        //以宽度作为圆角基础
        RADIUS_HEIGHT(3),
        //以宽度作为圆角基础
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
