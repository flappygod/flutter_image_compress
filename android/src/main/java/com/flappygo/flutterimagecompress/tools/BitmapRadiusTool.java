package com.flappygo.flutterimagecompress.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Administrator on 2017/5/27.
 */

public class BitmapRadiusTool {


    public static Bitmap toRoundCorner(Bitmap bitmap, RadiusOption option) {
        if (bitmap == null) {
            return null;
        }
        final int color = 0xff424242;
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        if (option.getScaleType() == RadiusOption.ScaleType.RADIUS_CENTER_CROP) {
            float whscale =  1.0f;
            float whbScale = (float) bitmap.getWidth() / (float) bitmap.getHeight();
            if (whscale > whbScale) {
                int top = (int) ((bitmap.getHeight() - bitmap.getWidth() / whscale) / 2);
                srcRect = new Rect(0, top, bitmap.getWidth(), bitmap.getHeight() - top);
                destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight() - 2 * top);

            } else {
                int left = (int) ((bitmap.getWidth() - bitmap.getHeight() * whscale) / 2);
                srcRect = new Rect(left, 0, bitmap.getWidth() - left, bitmap.getHeight());
                destRect = new Rect(0, 0, bitmap.getWidth() - 2 * left, bitmap.getHeight());
            }
            Bitmap output = Bitmap.createBitmap(destRect.width(), destRect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);
            //corner
            float roundPx = option.getRadian() * Math.min(bitmap.getHeight(), bitmap.getWidth());
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            //Rect
            RectF rectF = new RectF(destRect);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            return output;

        } else if (option.getScaleType() == RadiusOption.ScaleType.RADIUS_WIDTH) {
            Bitmap output = Bitmap.createBitmap(destRect.width(), destRect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);
            //corner
            float roundPx = option.getRadian() * bitmap.getWidth();
            //corner
            float roundPy = option.getRadian() * bitmap.getWidth();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            //Rect
            RectF rectF = new RectF(destRect);
            canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            return output;

        } else if (option.getScaleType() == RadiusOption.ScaleType.RADIUS_HEIGHT) {

            Bitmap output = Bitmap.createBitmap(destRect.width(), destRect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);
            //corner
            float roundPx = option.getRadian() * bitmap.getHeight();
            //corner
            float roundPy = option.getRadian() * bitmap.getHeight();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            //Rect
            RectF rectF = new RectF(destRect);
            canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            return output;
        } else if (option.getScaleType() == RadiusOption.ScaleType.RADIUS_ELLIPSE) {

            Bitmap output = Bitmap.createBitmap(destRect.width(), destRect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);
            //corner
            float roundPx = option.getRadian() * bitmap.getWidth();
            //corner
            float roundPy = option.getRadian() * bitmap.getHeight();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            RectF rectF = new RectF(destRect);
            canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            return output;
        }
        return null;
    }

}
